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
 *  Copyright 2014 (C) Sindre Mehus
 */

package net.sourceforge.subsonic.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;
import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.dao.MediaFileDao;
import net.sourceforge.subsonic.domain.ArtistBio;
import net.sourceforge.subsonic.domain.MediaFile;

/**
 * Provides services from the Last.fm REST API.
 *
 * @author Sindre Mehus
 * @version $Id$
 */
public class LastFmService {

    private static final String LAST_FM_KEY = "ece4499898a9440896dfdce5dab26bbf";
    private static final long CACHE_TIME_TO_LIVE_MILLIS = 6 * 30 * 24 * 3600 * 1000L; // 6 months
    private static final Logger LOG = Logger.getLogger(LastFmService.class);

    private MediaFileDao mediaFileDao;
    private MediaFileService mediaFileService;

    public void init() {
        Caller caller = Caller.getInstance();
        caller.setUserAgent("Subsonic");

        File cacheDir = new File(SettingsService.getSubsonicHome(), "lastfmcache");
        caller.setCache(new LastFmCache(cacheDir, CACHE_TIME_TO_LIVE_MILLIS));
    }

    /**
     * Returns similar artists, using last.fm REST API.
     *
     * @param mediaFile The media file (song, album or artist).
     * @param limit     Max number of similar artists to return.
     * @return Similar artists, ordred by similarity.
     */
    public List<MediaFile> getSimilarArtists(MediaFile mediaFile, int limit) {
        List<MediaFile> result = new ArrayList<MediaFile>();
        if (mediaFile == null) {
            return result;
        }

        String artistName = getArtistName(mediaFile);
        try {
            for (Artist lastFmArtist : Artist.getSimilar(artistName, LAST_FM_KEY)) {
                MediaFile similarArtist = mediaFileDao.getArtistByName(lastFmArtist.getName());
                if (similarArtist != null) {
                    result.add(similarArtist);
                    if (result.size() == limit) {
                        break;
                    }
                }
            }

        } catch (Throwable x) {
            LOG.warn("Failed to find similar artists for " + artistName, x);
        }
        return result;
    }

    /**
     * Returns songs from similar artists, using last.fm REST API. Typically used for artist radio features.
     *
     * @param mediaFile The media file (song, album or artist).
     * @param count     Max number of songs to return.
     * @return Songs from similar artists;
     */
    public List<MediaFile> getSimilarSongs(MediaFile mediaFile, int count) throws IOException {
        List<MediaFile> similarSongs = new ArrayList<MediaFile>();

        String artistName = getArtistName(mediaFile);
        MediaFile artist = mediaFileDao.getArtistByName(artistName);
        if (artist != null) {
            similarSongs.addAll(mediaFileService.getRandomSongsForParent(artist, count));
        }

        for (MediaFile similarArtist : getSimilarArtists(mediaFile, 100)) {
            similarSongs.addAll(mediaFileService.getRandomSongsForParent(similarArtist, count));
        }
        Collections.shuffle(similarSongs);
        return similarSongs.subList(0, Math.min(count, similarSongs.size()));
    }

    /**
     * Returns artist bio and images.
     *
     * @param mediaFile The media file (song, album or artist).
     * @return Artist bio.
     */
    public ArtistBio getArtistBio(MediaFile mediaFile) {
        String artistName = getArtistName(mediaFile);
        try {
            Artist info = Artist.getInfo(artistName, LAST_FM_KEY);
            if (info == null) {
                return null;
            }
            // TODO: More images
            return new ArtistBio(processWikiText(info.getWikiText()), info.getImageURL(ImageSize.LARGE));
        } catch (Throwable x) {
            LOG.warn("Failed to find artist bio for " + artistName, x);
            return null;
        }
    }

    private String processWikiText(String text) {
        /*
         System of a Down is an Armenian American <a href="http://www.last.fm/tag/alternative%20metal" class="bbcode_tag" rel="tag">alternative metal</a> band,
         formed in 1994 in Los Angeles, California, USA. All four members are of Armenian descent, and are widely known for their outspoken views expressed in
         many of their songs confronting the Armenian Genocide of 1915 by the Ottoman Empire and the ongoing War on Terror by the US government. The band
         consists of <a href="http://www.last.fm/music/Serj+Tankian" class="bbcode_artist">Serj Tankian</a> (vocals), Daron Malakian (vocals, guitar),
         Shavo Odadjian (bass, vocals) and John Dolmayan (drums).
         <a href="http://www.last.fm/music/System+of+a+Down">Read more about System of a Down on Last.fm</a>.
         User-contributed text is available under the Creative Commons By-SA License and may also be available under the GNU FDL.
         */

        text = text.replaceAll("<a href.*>Read more about.*", "");
        text = text.replaceAll("User-contributed text.*", "");
        text = text.replaceAll("<a ", "<a target='_blank' ");

        // TODO
        return text;
    }

    private String getArtistName(MediaFile mediaFile) {
        String artistName = mediaFile.getName();
        if (mediaFile.isAlbum() || mediaFile.isFile()) {
            artistName = mediaFile.getAlbumArtist() != null ? mediaFile.getAlbumArtist() : mediaFile.getArtist();
        }
        return artistName;
    }

    public void setMediaFileDao(MediaFileDao mediaFileDao) {
        this.mediaFileDao = mediaFileDao;
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }
}
