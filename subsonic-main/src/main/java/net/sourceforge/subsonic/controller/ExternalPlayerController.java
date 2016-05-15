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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.MusicFolder;
import net.sourceforge.subsonic.domain.Player;
import net.sourceforge.subsonic.domain.Share;
import net.sourceforge.subsonic.domain.VideoConversion;
import net.sourceforge.subsonic.domain.VideoConversion.Status;
import net.sourceforge.subsonic.service.MediaFileService;
import net.sourceforge.subsonic.service.PlayerService;
import net.sourceforge.subsonic.service.SettingsService;
import net.sourceforge.subsonic.service.ShareService;
import net.sourceforge.subsonic.service.TranscodingService;
import net.sourceforge.subsonic.service.VideoConversionService;
import net.sourceforge.subsonic.util.StringUtil;

/**
 * Controller for the page used to play shared music (Twitter, Facebook etc).
 *
 * @author Sindre Mehus
 */
public class ExternalPlayerController extends ParameterizableViewController {

    private SettingsService settingsService;
    private PlayerService playerService;
    private ShareService shareService;
    private MediaFileService mediaFileService;
    private TranscodingService transcodingService;
	private VideoConversionService videoConversionService;
	private CaptionsController captionsController;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || !pathInfo.startsWith("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        Share share = shareService.getShareByName(pathInfo.substring(1));

        if (share != null && share.getExpires() != null && share.getExpires().before(new Date())) {
            share = null;
        }

        if (share != null) {
            share.setLastVisited(new Date());
            share.setVisitCount(share.getVisitCount() + 1);
            shareService.updateShare(share);
        }

        Player player = playerService.getGuestPlayer(request);

        map.put("share", share);
        map.put("entries", getEntries(share, player));
        map.put("redirectUrl", settingsService.getUrlRedirectUrl());
        map.put("player", player.getId());

        ModelAndView result = super.handleRequestInternal(request, response);
        result.addObject("model", map);
        return result;
    }

    private List<Entry> getEntries(Share share, Player player) throws IOException {
        List<Entry> result = new ArrayList<Entry>();
        List<MusicFolder> musicFolders = settingsService.getMusicFoldersForUser(player.getUsername());

        if (share != null) {
            for (MediaFile file : shareService.getSharedFiles(share.getId(), musicFolders)) {
                if (file.getFile().exists()) {
                    if (file.isDirectory()) {
                        for (MediaFile child : mediaFileService.getChildrenOf(file, true, false, true)) {
                            result.add(createEntry(child, player));
                        }
                    } else {
                        result.add(createEntry(file, player));
                    }
                }
            }
        }
        return result;
    }

    private Entry createEntry(MediaFile file, Player player) {
		boolean converted = isConverted(file);
		boolean streamable = (converted) || (videoConversionService.isStreamable(file));
		String contentType;
		if (file.isVideo()) {
			contentType = streamable ? "video/mp4" : "application/x-mpegurl";
		} else {
			contentType = StringUtil.getMimeType(transcodingService.getSuffix(player, file, null));
		}
		boolean hasCaptions = (file.isVideo()) && (captionsController.findCaptionsVideo(file) != null);
		return new Entry(file, contentType, converted, streamable, hasCaptions);
	}

	private boolean isConverted(MediaFile file) {
		VideoConversion conversion = videoConversionService.getVideoConversionForFile(file.getId());
		return (conversion != null) && (conversion.getStatus() == VideoConversion.Status.COMPLETED);
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setShareService(ShareService shareService) {
        this.shareService = shareService;
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    public void setTranscodingService(TranscodingService transcodingService) {
        this.transcodingService = transcodingService;
    }
	
	public void setVideoConversionService(VideoConversionService videoConversionService) {
		this.videoConversionService = videoConversionService;
	}
	
	public void setCaptionsController(CaptionsController captionsController) {
		this.captionsController = captionsController;
	}
	
    public static class Entry {
        private final MediaFile file;
		private final String contentType;
		private final boolean converted;
		private final boolean streamable;
		private final boolean captions;

		public Entry(MediaFile file, String contentType, boolean converted, boolean streamable, boolean captions) {
            this.file = file;
			this.contentType = contentType;
			this.converted = converted;
			this.streamable = streamable;
			this.captions = captions;
        }

        public MediaFile getFile() {
            return file;
        }
		
		public String getContentType() {
			return contentType;
		}
		
		public boolean isConverted() {
			return converted;
		}
		
		public boolean isStreamable() {
			return streamable;
		}
		
		public boolean isCaptions() {
			return captions;
		}
    }
}