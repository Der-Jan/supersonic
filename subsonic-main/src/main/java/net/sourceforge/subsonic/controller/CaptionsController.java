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
 *  Copyright 2016 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.controller;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.google.common.io.Files;

import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.service.MediaFileService;
import net.sourceforge.subsonic.service.SecurityService;

/**
 * Controller for serving closed captions.
 *
 * @author Sindre Mehus
 */
public class CaptionsController implements Controller {

    private static final String[] CAPTIONS_FORMATS = {"vtt", "srt"};

    private MediaFileService mediaFileService;
    private SecurityService securityService;


    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
        MediaFile video = mediaFileService.getMediaFile(id);

        if (!securityService.isAuthenticated(video, request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access to file " + id + " is forbidden");
            return null;
        }

        File captionsFile = findCaptionsFile(video);
        if (captionsFile == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("text/plain");
        Files.copy(captionsFile, response.getOutputStream());

        return null;
    }

    public File findCaptionsFile(MediaFile video) {
        for (String captionsFormat : CAPTIONS_FORMATS) {
            File captionsFile = new File(video.getParentFile(),
                                         FilenameUtils.getBaseName(video.getFile().getName()) + "." + captionsFormat);
            if (captionsFile.exists() && captionsFile.isFile()) {
                return captionsFile;
            }
        }
        return null;
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}
