/*
 * This file is part of Subsonic.
 *
 *  Subsonic is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Subsonic is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright 2015 (C) Sindre Mehus
 */

package net.sourceforge.subsonic.service.sonos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.sonos.services._1.AbstractMedia;
import com.sonos.services._1.AlbumArtUrl;
import com.sonos.services._1.ItemType;
import com.sonos.services._1.MediaCollection;
import com.sonos.services._1.MediaMetadata;
import com.sonos.services._1.TrackMetadata;

import net.sourceforge.subsonic.controller.CoverArtController;
import net.sourceforge.subsonic.dao.MediaFileDao;
import net.sourceforge.subsonic.domain.CoverArtScheme;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.MusicFolder;
import net.sourceforge.subsonic.domain.MusicFolderContent;
import net.sourceforge.subsonic.domain.MusicIndex;
import net.sourceforge.subsonic.domain.Player;
import net.sourceforge.subsonic.domain.Playlist;
import net.sourceforge.subsonic.service.MediaFileService;
import net.sourceforge.subsonic.service.MusicIndexService;
import net.sourceforge.subsonic.service.PlayerService;
import net.sourceforge.subsonic.service.PlaylistService;
import net.sourceforge.subsonic.service.SettingsService;
import net.sourceforge.subsonic.service.SonosService;
import net.sourceforge.subsonic.service.TranscodingService;
import net.sourceforge.subsonic.util.StringUtil;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class SonosHelper {

    private MediaFileService mediaFileService;
    private PlaylistService playlistService;
    private PlayerService playerService;
    private TranscodingService transcodingService;
    private SettingsService settingsService;
    private MusicIndexService musicIndexService;
    private MediaFileDao mediaFileDao;

    public List<MediaCollection> forRoot() {
        MediaCollection library = new MediaCollection();
        library.setItemType(ItemType.COLLECTION);
        library.setId(SonosService.ID_LIBRARY);
        library.setTitle("Browse Library");

        MediaCollection playlists = new MediaCollection();
        playlists.setItemType(ItemType.COLLECTION);
        playlists.setId(SonosService.ID_PLAYLISTS);
        playlists.setTitle("Playlists");

        MediaCollection starred = new MediaCollection();
        starred.setItemType(ItemType.FAVORITES);
        starred.setId(SonosService.ID_STARRED);
        starred.setTitle("Starred");

        return Arrays.asList(library, playlists, starred);
    }

    public List<AbstractMedia> forLibrary() {
        try {
            List<AbstractMedia> result = new ArrayList<AbstractMedia>();
            List<MusicFolder> musicFolders = settingsService.getAllMusicFolders();
            MusicFolderContent musicFolderContent = null;
            musicFolderContent = musicIndexService.getMusicFolderContent(musicFolders, false);

            for (List<MusicIndex.SortableArtistWithMediaFiles> artists : musicFolderContent.getIndexedArtists().values()) {
                for (MusicIndex.SortableArtistWithMediaFiles artist : artists) {
                    for (MediaFile artistMediaFile : artist.getMediaFiles()) {
                        result.add(forDirectory(artistMediaFile));
                    }
                }
            }
            for (MediaFile song : musicFolderContent.getSingleSongs()) {
                if (song.isAudio()) {
                    result.add(forSong(song));
                }
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<AbstractMedia> forDirectoryContent(int mediaFileId) {
        List<AbstractMedia> result = new ArrayList<AbstractMedia>();
        MediaFile dir = mediaFileService.getMediaFile(mediaFileId);
        List<MediaFile> children = mediaFileService.getChildrenOf(dir, true, true, true);
        for (MediaFile child : children) {
            if (child.isDirectory()) {
                result.add(forDirectory(child));
            } else if (child.isAudio()) {
                result.add(forSong(child));
            }
        }
        return result;
    }

    private MediaCollection forDirectory(MediaFile dir) {
        MediaCollection mediaCollection = new MediaCollection();

        mediaCollection.setId(String.valueOf(dir.getId()));
        if (dir.isAlbum()) {
            mediaCollection.setItemType(ItemType.ALBUM);
            mediaCollection.setArtist(dir.getArtist());
            mediaCollection.setTitle(dir.getAlbumName());
            mediaCollection.setCanPlay(true);

            AlbumArtUrl albumArtURI = new AlbumArtUrl();
            albumArtURI.setValue(getCoverArtUrl(String.valueOf(dir.getId())));
            mediaCollection.setAlbumArtURI(albumArtURI);
        } else {
            mediaCollection.setItemType(ItemType.CONTAINER);
            mediaCollection.setTitle(dir.getName());
        }
        return mediaCollection;
    }

    public List<MediaCollection> forPlaylists(String username) {
        List<MediaCollection> result = new ArrayList<MediaCollection>();
        for (Playlist playlist : playlistService.getReadablePlaylistsForUser(username)) {
            MediaCollection mediaCollection = new MediaCollection();
            AlbumArtUrl albumArtURI = new AlbumArtUrl();
            albumArtURI.setValue(getCoverArtUrl(CoverArtController.PLAYLIST_COVERART_PREFIX + playlist.getId()));

            mediaCollection.setId(SonosService.ID_PLAYLIST_PREFIX + playlist.getId());
            mediaCollection.setCanPlay(true);
            mediaCollection.setItemType(ItemType.PLAYLIST);
            mediaCollection.setArtist(playlist.getUsername());
            mediaCollection.setTitle(playlist.getName());
            mediaCollection.setAlbumArtURI(albumArtURI);
            result.add(mediaCollection);
        }
        return result;
    }

    public List<MediaMetadata> forPlaylist(int playlistId) {
        List<MediaMetadata> result = new ArrayList<MediaMetadata>();
        for (MediaFile song : playlistService.getFilesInPlaylist(playlistId)) {
            if (song.isAudio()) {
                result.add(forSong(song));
            }
        }
        return result;
    }

    public List<MediaCollection> forStarred() {
        MediaCollection artists = new MediaCollection();
        artists.setItemType(ItemType.FAVORITES);
        artists.setId(SonosService.ID_STARRED_ARTISTS);
        artists.setTitle("Starred Artists");

        MediaCollection albums = new MediaCollection();
        albums.setItemType(ItemType.FAVORITES);
        albums.setId(SonosService.ID_STARRED_ALBUMS);
        albums.setTitle("Starred Albums");

        MediaCollection songs = new MediaCollection();
        songs.setItemType(ItemType.FAVORITES);
        songs.setId(SonosService.ID_STARRED_SONGS);
        songs.setCanPlay(true);
        songs.setTitle("Starred Songs");

        return Arrays.asList(artists, albums, songs);
    }

    public List<MediaCollection> forStarredArtists(String username) {
        List<MediaCollection> result = new ArrayList<MediaCollection>();
        for (MediaFile artist : mediaFileDao.getStarredDirectories(0, Integer.MAX_VALUE, username)) {
            MediaCollection mediaCollection = forDirectory(artist);
            mediaCollection.setItemType(ItemType.ARTIST);
            result.add(mediaCollection);
        }
        return result;
    }

    public List<MediaCollection> forStarredAlbums(String username) {
        List<MediaCollection> result = new ArrayList<MediaCollection>();
        for (MediaFile album : mediaFileDao.getStarredAlbums(0, Integer.MAX_VALUE, username, null)) {
            MediaCollection mediaCollection = forDirectory(album);
            mediaCollection.setItemType(ItemType.ALBUM);
            result.add(mediaCollection);
        }
        return result;
    }

    public List<MediaMetadata> forStarredSongs(String username) {
        List<MediaMetadata> result = new ArrayList<MediaMetadata>();
        for (MediaFile song : mediaFileDao.getStarredFiles(0, Integer.MAX_VALUE, username)) {
            if (song.isAudio()) {
                result.add(forSong(song));
            }
        }
        return result;
    }

    public MediaMetadata forSong(MediaFile song) {
        Player player = playerService.getGuestPlayer(null);
        String suffix = transcodingService.getSuffix(player, song, null);

        MediaMetadata result = new MediaMetadata();
        result.setId(String.valueOf(song.getId()));
        result.setItemType(ItemType.TRACK);
        result.setMimeType(StringUtil.getMimeType(suffix));
        result.setTitle(song.getTitle());
        result.setGenre(song.getGenre());
//        result.setDynamic();// TODO: For starred songs

        AlbumArtUrl albumArtURI = new AlbumArtUrl();
        albumArtURI.setValue(getCoverArtUrl(String.valueOf(song.getId())));

        TrackMetadata trackMetadata = new TrackMetadata();
        trackMetadata.setArtist(song.getArtist());
        trackMetadata.setAlbumArtist(song.getAlbumArtist());
        trackMetadata.setAlbum(song.getAlbumName());
        trackMetadata.setAlbumArtURI(albumArtURI);
        trackMetadata.setDuration(song.getDurationSeconds());
        trackMetadata.setCanSkip(false); // TODO, but probably ok since the whole song is loaded?

        result.setTrackMetadata(trackMetadata);

        return result;
    }

    private String getCoverArtUrl(String id) {
        return getBaseUrl() + "coverArt.view?id=" + id + "&size=" + CoverArtScheme.LARGE.getSize();
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setTranscodingService(TranscodingService transcodingService) {
        this.transcodingService = transcodingService;
    }

    public String getMediaURI(int mediaFileId) {
        MediaFile song = mediaFileService.getMediaFile(mediaFileId);
        Player player = playerService.getGuestPlayer(null);

        return getBaseUrl() + "stream?id=" + song.getId() + "&player=" + player.getId();
    }

    // TODO: Make it work with https?
    private String getBaseUrl() {
        int port = settingsService.getPort();
        String contextPath = settingsService.getUrlRedirectContextPath();

        // Note: Serving media and cover art with http (as opposed to https) works when using jetty and SubsonicDeployer.
        StringBuilder url = new StringBuilder("http://")
                .append(settingsService.getLocalIpAddress())
                .append(":")
                .append(port)
                .append("/");

        if (StringUtils.isNotEmpty(contextPath)) {
            url.append(contextPath).append("/");
        }
        return url.toString();
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setMusicIndexService(MusicIndexService musicIndexService) {
        this.musicIndexService = musicIndexService;
    }

    public void setMediaFileDao(MediaFileDao mediaFileDao) {
        this.mediaFileDao = mediaFileDao;
    }
}
