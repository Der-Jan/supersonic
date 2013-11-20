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
package net.sourceforge.subsonic.backend.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Redirects vanity URLs (such as http://sindre.subsonic.org).
 *
 * @author Sindre Mehus
 */
public class RedirectionController implements Controller {

//    private static final Logger LOG = Logger.getLogger(RedirectionController.class);
//    private RedirectionDao redirectionDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView(new RedirectView("http://nrk.no"));


//        String redirectFrom = getRedirectFrom(request);
//        if (RESERVED_REDIRECTS.containsKey(redirectFrom)) {
//            LOG.info("Reserved redirection: " + redirectFrom);
//            return new ModelAndView(new RedirectView(RESERVED_REDIRECTS.get(redirectFrom)));
//        }
//
//        Redirection redirection = redirectFrom == null ? null : redirectionDao.getRedirection(redirectFrom);
//
//        if (redirection == null) {
//            LOG.info("No redirection found: " + redirectFrom);
//            return new ModelAndView(new RedirectView("http://subsonic.org/pages"));
//        }
//
//        redirection.setLastRead(new Date());
//        redirection.setReadCount(redirection.getReadCount() + 1);
//        redirectionDao.updateRedirection(redirection);
//
//        // Check for trial expiration (unless called from REST client for which the Subsonic server manages trial expiry).
//        if (isTrialExpired(redirection) && !isREST(request)) {
//            LOG.info("Expired redirection: " + redirectFrom);
//            return new ModelAndView(new RedirectView("http://subsonic.org/pages/redirect-expired.jsp?redirectFrom=" +
//                    redirectFrom + "&expired=" + redirection.getTrialExpires().getTime()));
//        }
//
//        String requestUrl = getFullRequestURL(request);
//        String to = StringUtils.removeEnd(getRedirectTo(request, redirection), "/");
//        String redirectTo = requestUrl.replaceFirst("http://" + redirectFrom + "\\.subsonic\\.org", to);
//        LOG.info("Redirecting from " + requestUrl + " to " + redirectTo);
//
//        return new ModelAndView(new RedirectView(redirectTo));
    }

}
