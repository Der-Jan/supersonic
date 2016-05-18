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
package net.sourceforge.subsonic.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.Player;
import net.sourceforge.subsonic.domain.User;
import net.sourceforge.subsonic.domain.VideoConversion;
import net.sourceforge.subsonic.domain.VideoConversion.Status;
import net.sourceforge.subsonic.service.MediaFileService;
import net.sourceforge.subsonic.service.PlayerService;
import net.sourceforge.subsonic.service.SecurityService;
import net.sourceforge.subsonic.service.SettingsService;
import net.sourceforge.subsonic.service.VideoConversionService;
import net.sourceforge.subsonic.service.metadata.MetaData;
import net.sourceforge.subsonic.service.metadata.Track;
import net.sourceforge.subsonic.util.StringUtil;

/**
 * Controller for the page used to play videos.
 *
 * @author Sindre Mehus
 */
public class VideoPlayerController extends ParameterizableViewController {

    @Deprecated
    public static final int DEFAULT_BIT_RATE = 2000;

    private MediaFileService mediaFileService;
    private SettingsService settingsService;
    private PlayerService playerService;
    private SecurityService securityService;
    private VideoConversionService videoConversionService;
    private CaptionsController captionsController;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
        MediaFile file = mediaFileService.getMediaFile(id);

        User user = securityService.getCurrentUser(request);
        Map<String, Object> map = new HashMap<String, Object>();
        Integer position = ServletRequestUtils.getIntParameter(request, "position");
        mediaFileService.populateStarredDate(file, user.getUsername());

		boolean converted = isConverted(file);
		boolean streamable = (converted) || (videoConversionService.isStreamable(file));
		boolean castable = (converted) || (isCastable(file));
		Integer duration = file.getDurationSeconds();
        Player player = playerService.getPlayer(request, response);
		
		List<Track> audioTracks = Collections.emptyList();
		if (!streamable) {
			MetaData metaData = videoConversionService.getVideoMetaData(file);
			if (metaData != null) {
				audioTracks = metaData.getAudioTracks();
			}
		}
		
        String url = request.getRequestURL().toString();
		String baseUrl = url.replaceFirst("/videoPlayer.view.*", "/");
		
        // Rewrite URLs in case we're behind a proxy.
        if (settingsService.isRewriteUrlEnabled()) {
            String referer = request.getHeader("referer");
			baseUrl = StringUtil.rewriteUrl(baseUrl, referer);
        }

		String remoteBaseUrl = this.settingsService.rewriteRemoteUrl(baseUrl);

        map.put("video", file);
		map.put("converted", Boolean.valueOf(converted));
		map.put("streamable", Boolean.valueOf(streamable));
		map.put("castable", Boolean.valueOf(castable));
		map.put("contentType", (streamable) ? "video/mp4" : StringUtil.getMimeType(file.getFormat()));
		map.put("audioTracks", audioTracks);
		map.put("ancestors", mediaFileService.getAncestorsOf(file));
		map.put("musicFolder", settingsService.getMusicFolderByPath(file.getFolder()));
        map.put("hasCaptions", captionsController.findCaptionsVideo(file) != null);
        map.put("remoteBaseUrl", remoteBaseUrl);
        map.put("duration", duration);
        map.put("position", position);
        map.put("licenseInfo", settingsService.getLicenseInfo());
        map.put("user", user);
        map.put("player", player);

        ModelAndView result = super.handleRequestInternal(request, response);
        result.addObject("model", map);
        return result;
    }

	private boolean isCastable(MediaFile file) {
		return Arrays.asList(new String[] { "mp4", "m4v", "mkv" }).contains(StringUtils.lowerCase(file.getFormat()));
	}

	private boolean isConverted(MediaFile file) {
		VideoConversion conversion = videoConversionService.getVideoConversionForFile(file.getId());
		return (conversion != null) && (conversion.getStatus() == VideoConversion.Status.COMPLETED);
	}

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setCaptionsController(CaptionsController captionsController) {
        this.captionsController = captionsController;
    }

    public void setVideoConversionService(VideoConversionService videoConversionService) {
        this.videoConversionService = videoConversionService;
    }
}
