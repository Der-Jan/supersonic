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
package net.sourceforge.subsonic.ajax;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.directwebremoting.WebContextFactory;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.domain.ArtistBio;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.MusicFolder;
import net.sourceforge.subsonic.service.LastFmService;
import net.sourceforge.subsonic.service.MediaFileService;
import net.sourceforge.subsonic.service.NetworkService;
import net.sourceforge.subsonic.service.SecurityService;
import net.sourceforge.subsonic.service.SettingsService;

/**
 * Provides miscellaneous AJAX-enabled services.
 * <p/>
 * This class is used by the DWR framework (http://getahead.ltd.uk/dwr/).
 *
 * @author Sindre Mehus
 */
public class MultiService {

    private static final Logger LOG = Logger.getLogger(MultiService.class);

    private NetworkService networkService;
    private MediaFileService mediaFileService;
    private LastFmService lastFmService;
    private SecurityService securityService;
    private SettingsService settingsService;

    /**
     * Returns status for port forwarding and URL redirection.
     */
    public NetworkStatus getNetworkStatus() {
        NetworkService.Status portForwardingStatus = networkService.getPortForwardingStatus();
        NetworkService.Status urlRedirectionStatus = networkService.getURLRedirecionStatus();
        return new NetworkStatus(portForwardingStatus.getText(),
                                 portForwardingStatus.getDate(),
                                 urlRedirectionStatus.getText(),
                                 urlRedirectionStatus.getDate());
    }

    public ArtistInfo getArtistInfo(int mediaFileId, int maxSimilarArtists) {
        MediaFile mediaFile = mediaFileService.getMediaFile(mediaFileId);
        List<SimilarArtist> similarArtists = getSimilarArtists(mediaFileId, maxSimilarArtists);
        ArtistBio artistBio = lastFmService.getArtistBio(mediaFile);
        return new ArtistInfo(similarArtists, artistBio);
    }

    private List<SimilarArtist> getSimilarArtists(int mediaFileId, int limit) {
        HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
        String username = securityService.getCurrentUsername(request);
        List<MusicFolder> musicFolders = settingsService.getMusicFoldersForUser(username);

        MediaFile artist = mediaFileService.getMediaFile(mediaFileId);
        List<MediaFile> similarArtists = lastFmService.getSimilarArtists(artist, limit, false, musicFolders);
        SimilarArtist[] result = new SimilarArtist[similarArtists.size()];
        for (int i = 0; i < result.length; i++) {
            MediaFile similarArtist = similarArtists.get(i);
            result[i] = new SimilarArtist(similarArtist.getId(), similarArtist.getName());
        }
        return Arrays.asList(result);
    }

    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    public void setLastFmService(LastFmService lastFmService) {
        this.lastFmService = lastFmService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
}