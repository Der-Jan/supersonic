/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.service.upnp;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.domain.CoverArtScheme;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.MediaLibraryStatistics;
import net.sourceforge.subsonic.domain.MusicFolder;
import net.sourceforge.subsonic.service.MediaFileService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.MusicAlbum;
import org.fourthline.cling.support.model.container.MusicArtist;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class FolderBasedContentDirectory extends SubsonicContentDirectory {

    private static final Logger LOG = Logger.getLogger(FolderBasedContentDirectory.class);

    private MediaFileService mediaFileService;

    @Override
    public BrowseResult browse(String objectId, BrowseFlag browseFlag, String filter, long firstResult,
                               long maxResults, SortCriterion[] orderby) throws ContentDirectoryException {

        LOG.info("UPnP request - objectId: " + objectId + ", browseFlag: " + browseFlag + ", firstResult: " +
                firstResult + ", maxResults: " + maxResults);

        // maxResult == 0 means all.
        if (maxResults == 0) {
            maxResults = Integer.MAX_VALUE;
        }

        try {
            if (ROOT_CONTAINER_ID.equals(objectId)) {
                return browseFlag == BrowseFlag.METADATA ? browseRootMetadata() : browseRoot(firstResult, maxResults);
            }
            MediaFile mediaFile = mediaFileService.getMediaFile(Integer.parseInt(objectId));
            return browseFlag == BrowseFlag.METADATA ? browseMediaFileMetadata(mediaFile) : browseMediaFile(mediaFile, firstResult, maxResults);

        } catch (Throwable x) {
            LOG.error("UPnP error: " + x, x);
            // TODO: Use different error codes.
            throw new ContentDirectoryException(
                    ContentDirectoryErrorCode.CANNOT_PROCESS,
                    x.toString()
            );
        }
    }

    private BrowseResult browseRootMetadata() throws Exception {
        StorageFolder root = new StorageFolder();
        root.setId(ROOT_CONTAINER_ID);
        root.setParentID("-1");

        MediaLibraryStatistics statistics = settingsService.getMediaLibraryStatistics();
        root.setStorageUsed(statistics == null ? 0 : statistics.getTotalLengthInBytes());
        root.setTitle("Subsonic Media");
        root.setRestricted(true);
        root.setSearchable(false);
        root.setWriteStatus(WriteStatus.NOT_WRITABLE);

        List<MusicFolder> musicFolders = settingsService.getAllMusicFolders();
        root.setChildCount(musicFolders.size());

        DIDLContent didl = new DIDLContent();
        didl.addContainer(root);
        return createBrowseResult(didl, 1, 1);
    }

    private BrowseResult browseRoot(long firstResult, long maxResults) throws Exception {
        // TODO: Add playlists
        DIDLContent didl = new DIDLContent();
        List<MusicFolder> allFolders = settingsService.getAllMusicFolders();
        List<MusicFolder> selectedFolders = subList(allFolders, firstResult, maxResults);
        for (MusicFolder folder : selectedFolders) {
            MediaFile mediaFile = mediaFileService.getMediaFile(folder.getPath());
            addContainerOrItem(didl, mediaFile);
        }
        return createBrowseResult(didl, selectedFolders.size(), allFolders.size());
    }

    private BrowseResult browseMediaFileMetadata(MediaFile mediaFile) throws Exception {
        DIDLContent didl = new DIDLContent();
        didl.addContainer(createContainer(mediaFile));
        return createBrowseResult(didl, 1, 1);
    }

    private BrowseResult browseMediaFile(MediaFile mediaFile, long firstResult, long maxResults) throws Exception {
        List<MediaFile> allChildren = mediaFileService.getChildrenOf(mediaFile, true, true, true);
        List<MediaFile> selectedChildren = subList(allChildren, firstResult, maxResults);

        DIDLContent didl = new DIDLContent();
        for (MediaFile child : selectedChildren) {
            addContainerOrItem(didl, child);
        }
        return createBrowseResult(didl, selectedChildren.size(), allChildren.size());
    }

    private <T> List<T> subList(List<T> list, long offset, long max) {
        return list.subList((int) offset, Math.min(list.size(), (int) (offset + max)));
    }

    private void addContainerOrItem(DIDLContent didl, MediaFile mediaFile) throws Exception {
        if (mediaFile.isFile()) {
            didl.addItem(createItem(mediaFile));
        } else {
            didl.addContainer(createContainer(mediaFile));
        }
    }

    private Item createItem(MediaFile song) {
        MediaFile parent = mediaFileService.getParentOf(song);
        MusicTrack item = new MusicTrack();
        item.setId(String.valueOf(song.getId()));
        item.setParentID(String.valueOf(parent.getId()));
        item.setTitle(song.getTitle());
        item.setAlbum(song.getAlbumName());
        if (song.getArtist() != null) {
            item.setArtists(new PersonWithRole[]{new PersonWithRole(song.getArtist())});
        }
        Integer year = song.getYear();
        if (year != null) {
            item.setDate(year + "-01-01");
        }
        item.setOriginalTrackNumber(song.getTrackNumber());
        if (song.getGenre() != null) {
            item.setGenres(new String[]{song.getGenre()});
        }
        item.setResources(Arrays.asList(createResourceForsong(song)));
        item.setDescription(song.getComment());
        return item;
    }

    private Container createContainer(MediaFile mediaFile) throws Exception {
        Container container = mediaFile.isAlbum() ? createAlbumContainer(mediaFile) : new MusicArtist();
        container.setId(String.valueOf(mediaFile.getId()));
        container.setTitle(mediaFile.getName());
        List<MediaFile> children = mediaFileService.getChildrenOf(mediaFile, true, true, false);
        container.setChildCount(children.size());

        container.setParentID(ROOT_CONTAINER_ID);
        if (!mediaFileService.isRoot(mediaFile)) {
            MediaFile parent = mediaFileService.getParentOf(mediaFile);
            if (parent != null) {
                container.setParentID(String.valueOf(parent.getId()));
            }
        }
        return container;
    }

    private Container createAlbumContainer(MediaFile album) throws Exception {
        MusicAlbum container = new MusicAlbum();
        String albumArtUrl = getBaseUrl() + "coverArt.view?id=" + album.getId() + "&size=" + CoverArtScheme.LARGE.getSize();
        container.setAlbumArtURIs(new URI[]{new URI(albumArtUrl)});

        // TODO: correct artist?
        if (album.getAlbumArtist() != null) {
            container.setArtists(new PersonWithRole[]{new PersonWithRole(album.getAlbumArtist())});
        }
        container.setDescription(album.getComment());

        return container;
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }
}
