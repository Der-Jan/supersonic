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
package net.sourceforge.subsonic.service;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.ObjectUtils;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.dao.AlbumDao;
import net.sourceforge.subsonic.dao.ArtistDao;
import net.sourceforge.subsonic.dao.MediaFileDao;
import net.sourceforge.subsonic.domain.Album;
import net.sourceforge.subsonic.domain.Artist;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.MediaLibraryStatistics;
import net.sourceforge.subsonic.domain.MusicFolder;
import net.sourceforge.subsonic.util.FileUtil;

/**
 * Provides services for scanning the music library.
 *
 * @author Sindre Mehus
 */
public class MediaScannerService {

    private static final int INDEX_VERSION = 15;
    private static final Logger LOG = Logger.getLogger(MediaScannerService.class);

    private MediaLibraryStatistics statistics;

    private boolean scanning;
    private Timer timer;
    private SettingsService settingsService;
    private SearchService searchService;
    private PlaylistService playlistService;
    private MediaFileService mediaFileService;
    private MediaFileDao mediaFileDao;
    private ArtistDao artistDao;
    private AlbumDao albumDao;
    private int scanCount;

    public void init() {
        deleteOldIndexFiles();
        statistics = settingsService.getMediaLibraryStatistics();
        schedule();
    }

    /**
     * Schedule background execution of media library scanning.
     */
    public synchronized void schedule() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                scanLibrary();
            }
        };

        long daysBetween = settingsService.getIndexCreationInterval();
        int hour = settingsService.getIndexCreationHour();

        if (daysBetween == -1) {
            LOG.info("Automatic media scanning disabled.");
            return;
        }

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        if (cal.getTime().before(now)) {
            cal.add(Calendar.DATE, 1);
        }

        Date firstTime = cal.getTime();
        long period = daysBetween * 24L * 3600L * 1000L;
        timer.schedule(task, firstTime, period);

        LOG.info("Automatic media library scanning scheduled to run every " + daysBetween + " day(s), starting at " + firstTime);

        // In addition, create index immediately if it doesn't exist on disk.
        if (settingsService.getLastScanned() == null) {
            LOG.info("Media library never scanned. Doing it now.");
            scanLibrary();
        }
    }

    /**
     * Returns whether the media library is currently being scanned.
     */
    public synchronized boolean isScanning() {
        return scanning;
    }

    /**
     * Returns the number of files scanned so far.
     */
    public int getScanCount() {
        return scanCount;
    }

    /**
     * Scans the media library.
     * The scanning is done asynchronously, i.e., this method returns immediately.
     */
    public synchronized void scanLibrary() {
        if (isScanning()) {
            return;
        }
        scanning = true;

        Thread thread = new Thread("MediaLibraryScanner") {
            @Override
            public void run() {
                doScanLibrary();
                playlistService.importPlaylists();
                playlistService.updatePlaylistStatistics();
            }
        };

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private void doScanLibrary() {
        LOG.info("Starting to scan media library.");

        try {
            Date lastScanned = new Date();

            // Maps from artist name to album count.
            Map<String, Integer> albumCount = new HashMap<String, Integer>();

            // Maps from genre name to song count.
            Map<String, Integer> genreCount = new HashMap<String, Integer>();

            scanCount = 0;
            statistics.reset();

            mediaFileService.setMemoryCacheEnabled(false);
            searchService.startIndexing();

            mediaFileService.clearMemoryCache();

            // Recurse through all files on disk.
            for (MusicFolder musicFolder : settingsService.getAllMusicFolders()) {
                MediaFile root = mediaFileService.getMediaFile(musicFolder.getPath(), false);
                scanFile(root, musicFolder, lastScanned, albumCount, genreCount);
            }
            LOG.info("Scanned media library with " + scanCount + " entries.");

            LOG.info("Marking non-present files.");
            mediaFileDao.markNonPresent(lastScanned);
            LOG.info("Marking non-present artists.");
            artistDao.markNonPresent(lastScanned);
            LOG.info("Marking non-present albums.");
            albumDao.markNonPresent(lastScanned);

            // Update statistics
            statistics.incrementArtists(albumCount.size());
            for (Integer albums : albumCount.values()) {
                statistics.incrementAlbums(albums);
            }

            // Update genres
            mediaFileDao.updateGenres(genreCount);

            settingsService.setMediaLibraryStatistics(statistics);
            settingsService.setLastScanned(lastScanned);
            settingsService.save(false);
            LOG.info("Completed media library scan.");

        } catch (Throwable x) {
            LOG.error("Failed to scan media library.", x);
        } finally {
            mediaFileService.setMemoryCacheEnabled(true);
            searchService.stopIndexing();
            scanning = false;
        }
    }

    private void scanFile(MediaFile file, MusicFolder musicFolder, Date lastScanned,
                          Map<String, Integer> albumCount, Map<String, Integer> genreCount) {
        scanCount++;
        if (scanCount % 250 == 0) {
            LOG.info("Scanned media library with " + scanCount + " entries.");
        }

        searchService.index(file);

        // Update the root folder if it has changed.
        if (!musicFolder.getPath().getPath().equals(file.getFolder())) {
            file.setFolder(musicFolder.getPath().getPath());
            mediaFileDao.createOrUpdateMediaFile(file);
        }

        if (file.isDirectory()) {
            for (MediaFile child : mediaFileService.getChildrenOf(file, true, false, false, false)) {
                scanFile(child, musicFolder, lastScanned, albumCount, genreCount);
            }
            for (MediaFile child : mediaFileService.getChildrenOf(file, false, true, false, false)) {
                scanFile(child, musicFolder, lastScanned, albumCount, genreCount);
            }
        } else {
            updateAlbum(file, lastScanned, albumCount);
            updateArtist(file, lastScanned, albumCount);
            updateGenre(file, genreCount);
            statistics.incrementSongs(1);
        }

        mediaFileDao.markPresent(file.getPath(), lastScanned);
        artistDao.markPresent(file.getAlbumArtist(), lastScanned);

        if (file.getDurationSeconds() != null) {
            statistics.incrementTotalDurationInSeconds(file.getDurationSeconds());
        }
        if (file.getFileSize() != null) {
            statistics.incrementTotalLengthInBytes(file.getFileSize());
        }
    }

    private void updateGenre(MediaFile file, Map<String, Integer> genreCount) {
        String genre = file.getGenre();
        if (genre == null) {
            return;
        }
        Integer count = genreCount.get(file.getGenre());
        if (count == null) {
            genreCount.put(genre, 1);
        } else {
            genreCount.put(genre, count + 1);
        }
    }

    private void updateAlbum(MediaFile file, Date lastScanned, Map<String, Integer> albumCount) {
        String artist = file.getAlbumArtist() != null ? file.getAlbumArtist() : file.getArtist();
        if (file.getAlbumName() == null || artist == null || file.getParentPath() == null || !file.isAudio()) {
            return;
        }

        Album album = albumDao.getAlbumForFile(file);
        if (album == null) {
            album = new Album();
            album.setPath(file.getParentPath());
            album.setName(file.getAlbumName());
            album.setArtist(artist);
            album.setCreated(file.getChanged());
        }
        if (album.getCoverArtPath() == null) {
            MediaFile parent = mediaFileService.getParentOf(file);
            if (parent != null) {
                album.setCoverArtPath(parent.getCoverArtPath());
            }
        }
        if (album.getYear() == null) {
            album.setYear(file.getYear());
        }
        if (album.getGenre() == null) {
            album.setGenre(file.getGenre());
        }

        boolean firstEncounter = !lastScanned.equals(album.getLastScanned());
        if (firstEncounter) {
            album.setDurationSeconds(0);
            album.setSongCount(0);
            Integer n = albumCount.get(artist);
            albumCount.put(artist, n == null ? 1 : n + 1);
        }
        if (file.getDurationSeconds() != null) {
            album.setDurationSeconds(album.getDurationSeconds() + file.getDurationSeconds());
        }
        if (file.isAudio()) {
            album.setSongCount(album.getSongCount() + 1);
        }

        album.setLastScanned(lastScanned);
        album.setPresent(true);
        albumDao.createOrUpdateAlbum(album);
        if (firstEncounter) {
            searchService.index(album);
        }

        // Update the file's album artist, if necessary.
        if (!ObjectUtils.equals(album.getArtist(), file.getAlbumArtist())) {
            file.setAlbumArtist(album.getArtist());
            mediaFileDao.createOrUpdateMediaFile(file);
        }
    }

    private void updateArtist(MediaFile file, Date lastScanned, Map<String, Integer> albumCount) {
        if (file.getAlbumArtist() == null || !file.isAudio()) {
            return;
        }

        Artist artist = artistDao.getArtist(file.getAlbumArtist());
        if (artist == null) {
            artist = new Artist();
            artist.setName(file.getAlbumArtist());
        }
        if (artist.getCoverArtPath() == null) {
            MediaFile parent = mediaFileService.getParentOf(file);
            if (parent != null) {
                artist.setCoverArtPath(parent.getCoverArtPath());
            }
        }
        boolean firstEncounter = !lastScanned.equals(artist.getLastScanned());

        Integer n = albumCount.get(artist.getName());
        artist.setAlbumCount(n == null ? 0 : n);

        artist.setLastScanned(lastScanned);
        artist.setPresent(true);
        artistDao.createOrUpdateArtist(artist);

        if (firstEncounter) {
            searchService.index(artist);
        }
    }

    /**
     * Returns media library statistics, including the number of artists, albums and songs.
     *
     * @return Media library statistics.
     */
    public MediaLibraryStatistics getStatistics() {
        return statistics;
    }

    /**
     * Deletes old versions of the index file.
     */
    private void deleteOldIndexFiles() {
        for (int i = 2; i < INDEX_VERSION; i++) {
            File file = getIndexFile(i);
            try {
                if (FileUtil.exists(file)) {
                    if (file.delete()) {
                        LOG.info("Deleted old index file: " + file.getPath());
                    }
                }
            } catch (Exception x) {
                LOG.warn("Failed to delete old index file: " + file.getPath(), x);
            }
        }
    }

    /**
     * Returns the index file for the given index version.
     *
     * @param version The index version.
     * @return The index file for the given index version.
     */
    private File getIndexFile(int version) {
        File home = SettingsService.getSubsonicHome();
        return new File(home, "subsonic" + version + ".index");
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    public void setMediaFileDao(MediaFileDao mediaFileDao) {
        this.mediaFileDao = mediaFileDao;
    }

    public void setArtistDao(ArtistDao artistDao) {
        this.artistDao = artistDao;
    }

    public void setAlbumDao(AlbumDao albumDao) {
        this.albumDao = albumDao;
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }
}
