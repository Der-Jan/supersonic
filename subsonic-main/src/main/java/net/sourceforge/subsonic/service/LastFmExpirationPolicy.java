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

package net.sourceforge.subsonic.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.umass.lastfm.cache.ExpirationPolicy;

/**
 * Artist and album info is cached permanently. Everything else is cached one year.
 *
 * @author Sindre Mehus
 * @version $Id$
 */
public class LastFmExpirationPolicy implements ExpirationPolicy {

    private final static List<String> METHODS_TO_CACHE_PERMANENTLY = Arrays.asList("artist.getInfo", "album.getInfo");
    private final static long ONE_YEAR = 12 * 30 * 24 * 3600 * 1000L;

    @Override
    public long getExpirationTime(String method, Map<String, String> params) {
        return METHODS_TO_CACHE_PERMANENTLY.contains(method) ? Long.MAX_VALUE : ONE_YEAR;
    }
}
