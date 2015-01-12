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

import com.sonos.services._1.ItemType;
import com.sonos.services._1.MediaCollection;
import com.sonos.services._1.MediaMetadata;
import com.sonos.services._1.TrackMetadata;

import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.Player;
import net.sourceforge.subsonic.domain.Playlist;
import net.sourceforge.subsonic.service.MediaFileService;
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

    public List<MediaCollection> forRoot() {
        MediaCollection browse = new MediaCollection();
        browse.setCanPlay(false);
        browse.setCanEnumerate(true);
        browse.setItemType(ItemType.COLLECTION);
        browse.setId(SonosService.ID_BROWSE);
        browse.setTitle("Browse library");

        MediaCollection playlists = new MediaCollection();
        playlists.setCanPlay(false);
        playlists.setCanEnumerate(true);
        playlists.setItemType(ItemType.COLLECTION);
        playlists.setId(SonosService.ID_PLAYLISTS);
        playlists.setTitle("Playlists");

        return Arrays.asList(browse, playlists);
    }

    public List<MediaCollection> forPlaylists() {
        List<MediaCollection> result = new ArrayList<MediaCollection>();
        for (Playlist playlist : playlistService.getAllPlaylists()) {
            MediaCollection mediaCollection = new MediaCollection();
            mediaCollection.setId(SonosService.ID_PLAYLIST_PREFIX + playlist.getId());
            mediaCollection.setCanEnumerate(true);
            mediaCollection.setCanPlay(true);
            mediaCollection.setItemType(ItemType.PLAYLIST);
            mediaCollection.setArtist(playlist.getUsername());
            mediaCollection.setTitle(playlist.getName());
//            mediaCollection.setAlbumArtURI();  TODO
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

        TrackMetadata trackMetadata = new TrackMetadata();
        trackMetadata.setArtist(song.getArtist());
        trackMetadata.setAlbumArtist(song.getAlbumArtist());
        trackMetadata.setAlbum(song.getAlbumName());
//        trackMetadata.setAlbumArtURI(); // TODO
        trackMetadata.setDuration(song.getDurationSeconds());
        trackMetadata.setCanSkip(false); // TODO, but probably ok since the whole song is loaded?

        result.setTrackMetadata(trackMetadata);

        return result;
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
}
