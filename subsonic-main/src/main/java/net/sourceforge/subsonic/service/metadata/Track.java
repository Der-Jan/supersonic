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

package net.sourceforge.subsonic.service.metadata;

import java.util.Arrays;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;

/**
 * A track within a media file container (e.g., mp4 or mkv)
 *
 * @author Sindre Mehus
 * @version $Id$
 */
public class Track {

    private final int id;
    private final String type;
    private final String language;
    private final String codec;

    public Track(int id, String type, String language, String codec) {
        this.id = id;
		this.type = StringUtils.trimToNull(type);
		this.language = StringUtils.trimToNull(language);
		this.codec = StringUtils.trimToNull(codec);
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getLanguage() {
        return language;
    }

	public String getLanguageName() {
		if (language == null) {
			return String.valueOf(id);
		}
		Locale locale = Locale.forLanguageTag(language);
		if (locale == null) {
			return language;
		}
		String languageName = StringUtils.trimToNull(locale.getDisplayLanguage(Locale.ENGLISH));
		return (languageName == null) ? language : languageName;
	}
	
    public String getCodec() {
        return codec;
    }

    @Override
    public String toString() {
        return id + " " + type + " " + language + " " + codec;
    }

    public boolean isAudio() {
        return "Audio".equals(type);
    }

    public boolean isVideo() {
        return "Video".equals(type) && !"mjpeg".equals(codec) && !"bmp".equals(codec);
    }

    public boolean isStreamable() {
        return Arrays.asList("h264", "aac").contains(codec);
    }
}
