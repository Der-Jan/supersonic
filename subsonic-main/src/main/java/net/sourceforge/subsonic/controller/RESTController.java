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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.subsonic.restapi.AlbumID3;
import org.subsonic.restapi.AlbumList;
import org.subsonic.restapi.AlbumList2;
import org.subsonic.restapi.AlbumWithSongsID3;
import org.subsonic.restapi.ArtistID3;
import org.subsonic.restapi.ArtistWithAlbumsID3;
import org.subsonic.restapi.ArtistsID3;
import org.subsonic.restapi.Child;
import org.subsonic.restapi.Directory;
import org.subsonic.restapi.Genres;
import org.subsonic.restapi.Index;
import org.subsonic.restapi.IndexID3;
import org.subsonic.restapi.Indexes;
import org.subsonic.restapi.License;
import org.subsonic.restapi.MediaType;
import org.subsonic.restapi.MusicFolders;
import org.subsonic.restapi.NowPlaying;
import org.subsonic.restapi.NowPlayingEntry;
import org.subsonic.restapi.PlaylistWithSongs;
import org.subsonic.restapi.Playlists;
import org.subsonic.restapi.Response;
import org.subsonic.restapi.SearchResult2;
import org.subsonic.restapi.SearchResult3;
import org.subsonic.restapi.Songs;
import org.subsonic.restapi.Starred;
import org.subsonic.restapi.Starred2;
import org.subsonic.restapi.Videos;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.ajax.ChatService;
import net.sourceforge.subsonic.ajax.LyricsInfo;
import net.sourceforge.subsonic.ajax.LyricsService;
import net.sourceforge.subsonic.ajax.PlayQueueService;
import net.sourceforge.subsonic.command.UserSettingsCommand;
import net.sourceforge.subsonic.dao.AlbumDao;
import net.sourceforge.subsonic.dao.ArtistDao;
import net.sourceforge.subsonic.dao.BookmarkDao;
import net.sourceforge.subsonic.dao.MediaFileDao;
import net.sourceforge.subsonic.domain.Album;
import net.sourceforge.subsonic.domain.Artist;
import net.sourceforge.subsonic.domain.Bookmark;
import net.sourceforge.subsonic.domain.Genre;
import net.sourceforge.subsonic.domain.InternetRadio;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.MusicFolder;
import net.sourceforge.subsonic.domain.MusicIndex;
import net.sourceforge.subsonic.domain.PlayQueue;
import net.sourceforge.subsonic.domain.Player;
import net.sourceforge.subsonic.domain.PlayerTechnology;
import net.sourceforge.subsonic.domain.Playlist;
import net.sourceforge.subsonic.domain.PodcastChannel;
import net.sourceforge.subsonic.domain.PodcastEpisode;
import net.sourceforge.subsonic.domain.RandomSearchCriteria;
import net.sourceforge.subsonic.domain.SearchCriteria;
import net.sourceforge.subsonic.domain.SearchResult;
import net.sourceforge.subsonic.domain.Share;
import net.sourceforge.subsonic.domain.TranscodeScheme;
import net.sourceforge.subsonic.domain.TransferStatus;
import net.sourceforge.subsonic.domain.User;
import net.sourceforge.subsonic.domain.UserSettings;
import net.sourceforge.subsonic.service.AudioScrobblerService;
import net.sourceforge.subsonic.service.JukeboxService;
import net.sourceforge.subsonic.service.MediaFileService;
import net.sourceforge.subsonic.service.MusicIndexService;
import net.sourceforge.subsonic.service.PlayerService;
import net.sourceforge.subsonic.service.PlaylistService;
import net.sourceforge.subsonic.service.PodcastService;
import net.sourceforge.subsonic.service.RatingService;
import net.sourceforge.subsonic.service.SearchService;
import net.sourceforge.subsonic.service.SecurityService;
import net.sourceforge.subsonic.service.SettingsService;
import net.sourceforge.subsonic.service.ShareService;
import net.sourceforge.subsonic.service.StatusService;
import net.sourceforge.subsonic.service.TranscodingService;
import net.sourceforge.subsonic.util.Pair;
import net.sourceforge.subsonic.util.StringUtil;
import net.sourceforge.subsonic.util.XMLBuilder;

import static net.sourceforge.subsonic.security.RESTRequestParameterProcessingFilter.decrypt;
import static net.sourceforge.subsonic.util.XMLBuilder.Attribute;
import static net.sourceforge.subsonic.util.XMLBuilder.AttributeSet;
import static org.springframework.web.bind.ServletRequestUtils.*;

/**
 * Multi-controller used for the REST API.
 * <p/>
 * For documentation, please refer to api.jsp.
 * <p/>
 * Note: Exceptions thrown from the methods are intercepted by RESTFilter.
 *
 * @author Sindre Mehus
 */
public class RESTController extends MultiActionController {

    private static final Logger LOG = Logger.getLogger(RESTController.class);

    private SettingsService settingsService;
    private SecurityService securityService;
    private PlayerService playerService;
    private MediaFileService mediaFileService;
    private MusicIndexService musicIndexService;
    private TranscodingService transcodingService;
    private DownloadController downloadController;
    private CoverArtController coverArtController;
    private AvatarController avatarController;
    private UserSettingsController userSettingsController;
    private LeftController leftController;
    private HomeController homeController;
    private StatusService statusService;
    private StreamController streamController;
    private HLSController hlsController;
    private ShareService shareService;
    private PlaylistService playlistService;
    private ChatService chatService;
    private LyricsService lyricsService;
    private PlayQueueService playQueueService;
    private JukeboxService jukeboxService;
    private AudioScrobblerService audioScrobblerService;
    private PodcastService podcastService;
    private RatingService ratingService;
    private SearchService searchService;
    private MediaFileDao mediaFileDao;
    private ArtistDao artistDao;
    private AlbumDao albumDao;
    private BookmarkDao bookmarkDao;

    private final Map<BookmarkKey, Bookmark> bookmarkCache = new ConcurrentHashMap<BookmarkKey, Bookmark>();
    private final JAXBWriter jaxbWriter = new JAXBWriter();

    public void init() {
        refreshBookmarkCache();
    }

    private void refreshBookmarkCache() {
        bookmarkCache.clear();
        for (Bookmark bookmark : bookmarkDao.getBookmarks()) {
            bookmarkCache.put(BookmarkKey.forBookmark(bookmark), bookmark);
        }
    }

    public void ping(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Response res = jaxbWriter.createResponse(true);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getLicense(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        License license = new License();

        String email = settingsService.getLicenseEmail();
        String key = settingsService.getLicenseCode();
        Date date = settingsService.getLicenseDate();
        boolean valid = settingsService.isLicenseValid();

        license.setValid(valid);
        if (valid) {
            license.setEmail(email);
            license.setKey(key);
            license.setDate(jaxbWriter.convertDate(date));
        }

        Response res = jaxbWriter.createResponse(true);
        res.setLicense(license);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getMusicFolders(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        MusicFolders musicFolders = new MusicFolders();
        for (MusicFolder musicFolder : settingsService.getAllMusicFolders()) {
            org.subsonic.restapi.MusicFolder mf = new org.subsonic.restapi.MusicFolder();
            mf.setId(musicFolder.getId());
            mf.setName(musicFolder.getName());
            musicFolders.getMusicFolder().add(mf);
        }
        Response res = jaxbWriter.createResponse(true);
        res.setMusicFolders(musicFolders);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getIndexes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Response res = jaxbWriter.createResponse(true);
        String username = securityService.getCurrentUser(request).getUsername();

        long ifModifiedSince = getLongParameter(request, "ifModifiedSince", 0L);
        long lastModified = leftController.getLastModified(request);

        if (lastModified <= ifModifiedSince) {
            jaxbWriter.writeResponse(request, response, res);
            return;
        }

        Indexes indexes = new Indexes();
        indexes.setLastModified(lastModified);
        indexes.setIgnoredArticles(settingsService.getIgnoredArticles());

        List<MusicFolder> musicFolders = settingsService.getAllMusicFolders();
        Integer musicFolderId = getIntParameter(request, "musicFolderId");
        if (musicFolderId != null) {
            for (MusicFolder musicFolder : musicFolders) {
                if (musicFolderId.equals(musicFolder.getId())) {
                    musicFolders = Arrays.asList(musicFolder);
                    break;
                }
            }
        }

        List<MediaFile> shortcuts = leftController.getShortcuts(musicFolders, settingsService.getShortcutsAsArray());
        for (MediaFile shortcut : shortcuts) {
            indexes.getShortcut().add(createArtist(shortcut, username));
        }

        SortedMap<MusicIndex, SortedSet<MusicIndex.SortableArtistWithMediaFiles>> indexedArtists =
                leftController.getMusicFolderContent(musicFolders, false).getIndexedArtists();

        for (Map.Entry<MusicIndex, SortedSet<MusicIndex.SortableArtistWithMediaFiles>> entry : indexedArtists.entrySet()) {
            Index index = new Index();
            indexes.getIndex().add(index);
            index.setName(entry.getKey().getIndex());

            for (MusicIndex.SortableArtistWithMediaFiles artist : entry.getValue()) {
                for (MediaFile mediaFile : artist.getMediaFiles()) {
                    if (mediaFile.isDirectory()) {
                        Date starredDate = mediaFileDao.getMediaFileStarredDate(mediaFile.getId(), username);
                        org.subsonic.restapi.Artist a = new org.subsonic.restapi.Artist();
                        index.getArtist().add(a);
                        a.setId(String.valueOf(mediaFile.getId()));
                        a.setName(artist.getName());
                        a.setStarred(jaxbWriter.convertDate(starredDate));
                    }
                }
            }
        }

        // Add children
        Player player = playerService.getPlayer(request, response);
        List<MediaFile> singleSongs = leftController.getSingleSongs(musicFolders, false);

        for (MediaFile singleSong : singleSongs) {
            indexes.getChild().add(createChild(player, singleSong, username));
        }

        res.setIndexes(indexes);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getGenres(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Genres genres = new Genres();

        for (Genre genre : mediaFileDao.getGenres(false)) {
            org.subsonic.restapi.Genre g = new org.subsonic.restapi.Genre();
            genres.getGenre().add(g);
            g.setContent(genre.getName());
            g.setAlbumCount(genre.getAlbumCount());
            g.setSongCount(genre.getSongCount());
        }
        Response res = jaxbWriter.createResponse(true);
        res.setGenres(genres);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getSongsByGenre(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        Songs songs = new Songs();

        String genre = getRequiredStringParameter(request, "genre");
        int offset = getIntParameter(request, "offset", 0);
        int count = getIntParameter(request, "count", 10);
        count = Math.max(0, Math.min(count, 500));

        for (MediaFile mediaFile : mediaFileDao.getSongsByGenre(genre, offset, count)) {
            songs.getSong().add(createChild(player, mediaFile, username));
        }
        Response res = jaxbWriter.createResponse(true);
        res.setSongsByGenre(songs);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getArtists(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        String username = securityService.getCurrentUsername(request);

        ArtistsID3 result = new ArtistsID3();
        result.setIgnoredArticles(settingsService.getIgnoredArticles());

        List<Artist> artists = artistDao.getAlphabetialArtists(0, Integer.MAX_VALUE);
        SortedMap<MusicIndex, SortedSet<MusicIndex.SortableArtistWithArtist>> indexedArtists = musicIndexService.getIndexedArtists(artists);
        for (Map.Entry<MusicIndex, SortedSet<MusicIndex.SortableArtistWithArtist>> entry : indexedArtists.entrySet()) {
            IndexID3 index = new IndexID3();
            result.getIndex().add(index);
            index.setName(entry.getKey().getIndex());
            for (MusicIndex.SortableArtistWithArtist sortableArtist : entry.getValue()) {
                index.getArtist().add(createArtist(new ArtistID3(), sortableArtist.getArtist(), username));
            }
        }

        Response res = jaxbWriter.createResponse(true);
        res.setArtists(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    @Deprecated
    private AttributeSet createAttributesForArtist(Artist artist, String username) {
        AttributeSet attributes = new AttributeSet();
        attributes.add("id", artist.getId());
        attributes.add("name", artist.getName());
        if (artist.getCoverArtPath() != null) {
            attributes.add("coverArt", CoverArtController.ARTIST_COVERART_PREFIX + artist.getId());
        }
        attributes.add("albumCount", artist.getAlbumCount());
        attributes.add("starred", StringUtil.toISO8601(artistDao.getArtistStarredDate(artist.getId(), username)));
        return attributes;
    }

    private <T extends ArtistID3> T createArtist(T jaxbArtist, Artist artist, String username) {
        jaxbArtist.setId(String.valueOf(artist.getId()));
        jaxbArtist.setName(artist.getName());
        jaxbArtist.setStarred(jaxbWriter.convertDate(mediaFileDao.getMediaFileStarredDate(artist.getId(), username)));
        jaxbArtist.setAlbumCount(artist.getAlbumCount());
        if (artist.getCoverArtPath() != null) {
            jaxbArtist.setCoverArt(CoverArtController.ARTIST_COVERART_PREFIX + artist.getId());
        }
        return jaxbArtist;
    }

    @Deprecated
    private AttributeSet createAttributesForArtist(MediaFile artist, String username) {
        AttributeSet attributes = new AttributeSet();
        attributes.add("id", artist.getId());
        attributes.add("name", artist.getName());
        attributes.add("starred", StringUtil.toISO8601(mediaFileDao.getMediaFileStarredDate(artist.getId(), username)));
        return attributes;
    }

    private org.subsonic.restapi.Artist createArtist(MediaFile artist, String username) {
        org.subsonic.restapi.Artist result = new org.subsonic.restapi.Artist();
        result.setId(String.valueOf(artist.getId()));
        result.setName(artist.getName());
        Date starred = mediaFileDao.getMediaFileStarredDate(artist.getId(), username);
        result.setStarred(jaxbWriter.convertDate(starred));
        return result;
    }

    public void getArtist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        String username = securityService.getCurrentUsername(request);
        int id = getRequiredIntParameter(request, "id");
        Artist artist = artistDao.getArtist(id);
        if (artist == null) {
            error(request, response, ErrorCode.NOT_FOUND, "Artist not found.");
            return;
        }

        ArtistWithAlbumsID3 result = createArtist(new ArtistWithAlbumsID3(), artist, username);
        for (Album album : albumDao.getAlbumsForArtist(artist.getName())) {
            result.getAlbum().add(createAlbum(new AlbumID3(), album, username));
        }

        Response res = jaxbWriter.createResponse(true);
        res.setArtist(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    private <T extends AlbumID3> T createAlbum(T jaxbAlbum, Album album, String username) {
        jaxbAlbum.setId(String.valueOf(album.getId()));
        jaxbAlbum.setName(album.getName());
        if (album.getArtist() != null) {
            jaxbAlbum.setArtist(album.getArtist());
            Artist artist = artistDao.getArtist(album.getArtist());
            if (artist != null) {
                jaxbAlbum.setArtistId(String.valueOf(artist.getId()));
            }
        }
        if (album.getCoverArtPath() != null) {
            jaxbAlbum.setCoverArt(CoverArtController.ALBUM_COVERART_PREFIX + album.getId());
        }
        jaxbAlbum.setSongCount(album.getSongCount());
        jaxbAlbum.setDuration(album.getDurationSeconds());
        jaxbAlbum.setCreated(jaxbWriter.convertDate(album.getCreated()));
        jaxbAlbum.setStarred(jaxbWriter.convertDate(albumDao.getAlbumStarredDate(album.getId(), username)));
        jaxbAlbum.setYear(album.getYear());
        jaxbAlbum.setGenre(album.getGenre());
        return jaxbAlbum;
    }

    @Deprecated
    private AttributeSet createAttributesForAlbum(Album album, String username) {
        AttributeSet attributes;
        attributes = new AttributeSet();
        attributes.add("id", album.getId());
        attributes.add("name", album.getName());
        attributes.add("artist", album.getArtist());
        if (album.getArtist() != null) {
            Artist artist = artistDao.getArtist(album.getArtist());
            if (artist != null) {
                attributes.add("artistId", artist.getId());
            }
        }
        if (album.getCoverArtPath() != null) {
            attributes.add("coverArt", CoverArtController.ALBUM_COVERART_PREFIX + album.getId());
        }
        attributes.add("songCount", album.getSongCount());
        attributes.add("duration", album.getDurationSeconds());
        attributes.add("created", StringUtil.toISO8601(album.getCreated()));
        attributes.add("starred", StringUtil.toISO8601(albumDao.getAlbumStarredDate(album.getId(), username)));
        attributes.add("year", album.getYear());
        attributes.add("genre", album.getGenre());

        return attributes;
    }

    @Deprecated
    private AttributeSet createAttributesForPlaylist(Playlist playlist) {
        AttributeSet attributes;
        attributes = new AttributeSet();
        attributes.add("id", playlist.getId());
        attributes.add("name", playlist.getName());
        attributes.add("comment", playlist.getComment());
        attributes.add("owner", playlist.getUsername());
        attributes.add("public", playlist.isShared());
        attributes.add("songCount", playlist.getFileCount());
        attributes.add("duration", playlist.getDurationSeconds());
        attributes.add("created", StringUtil.toISO8601(playlist.getCreated()));
        return attributes;
    }

    private <T extends org.subsonic.restapi.Playlist> T createPlaylist(T jaxbPlaylist, Playlist playlist) {
        jaxbPlaylist.setId(String.valueOf(playlist.getId()));
        jaxbPlaylist.setName(playlist.getName());
        jaxbPlaylist.setComment(playlist.getComment());
        jaxbPlaylist.setOwner(playlist.getUsername());
        jaxbPlaylist.setPublic(playlist.isShared());
        jaxbPlaylist.setSongCount(playlist.getFileCount());
        jaxbPlaylist.setDuration(playlist.getDurationSeconds());
        jaxbPlaylist.setCreated(jaxbWriter.convertDate(playlist.getCreated()));

        for (String username : playlistService.getPlaylistUsers(playlist.getId())) {
            jaxbPlaylist.getAllowedUser().add(username);
        }
        return jaxbPlaylist;
    }

    public void getAlbum(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        int id = getRequiredIntParameter(request, "id");
        Album album = albumDao.getAlbum(id);
        if (album == null) {
            error(request, response, ErrorCode.NOT_FOUND, "Album not found.");
            return;
        }

        AlbumWithSongsID3 result = createAlbum(new AlbumWithSongsID3(), album, username);
        for (MediaFile mediaFile : mediaFileDao.getSongsForAlbum(album.getArtist(), album.getName())) {
            result.getSong().add(createChild(player, mediaFile, username));
        }

        Response res = jaxbWriter.createResponse(true);
        res.setAlbum(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getSong(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        int id = getRequiredIntParameter(request, "id");
        MediaFile song = mediaFileDao.getMediaFile(id);
        if (song == null || song.isDirectory()) {
            error(request, response, ErrorCode.NOT_FOUND, "Song not found.");
            return;
        }

        Response res = jaxbWriter.createResponse(true);
        res.setSong(createChild(player, song, username));
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getMusicDirectory(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        int id = getRequiredIntParameter(request, "id");
        MediaFile dir = mediaFileService.getMediaFile(id);
        if (dir == null) {
            error(request, response, ErrorCode.NOT_FOUND, "Directory not found");
            return;
        }

        MediaFile parent = mediaFileService.getParentOf(dir);
        Directory directory = new Directory();
        directory.setId(String.valueOf(id));
        try {
            if (!mediaFileService.isRoot(parent)) {
                directory.setParent(String.valueOf(parent.getId()));
            }
        } catch (SecurityException x) {
            // Ignored.
        }
        directory.setName(dir.getName());
        directory.setStarred(jaxbWriter.convertDate(mediaFileDao.getMediaFileStarredDate(id, username)));

        for (MediaFile child : mediaFileService.getChildrenOf(dir, true, true, true)) {
            directory.getChild().add(createChild(player, child, username));
        }

        Response res = jaxbWriter.createResponse(true);
        res.setDirectory(directory);
        jaxbWriter.writeResponse(request, response, res);
    }

    @Deprecated
    public void search(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        String any = request.getParameter("any");
        String artist = request.getParameter("artist");
        String album = request.getParameter("album");
        String title = request.getParameter("title");

        StringBuilder query = new StringBuilder();
        if (any != null) {
            query.append(any).append(" ");
        }
        if (artist != null) {
            query.append(artist).append(" ");
        }
        if (album != null) {
            query.append(album).append(" ");
        }
        if (title != null) {
            query.append(title);
        }

        SearchCriteria criteria = new SearchCriteria();
        criteria.setQuery(query.toString().trim());
        criteria.setCount(getIntParameter(request, "count", 20));
        criteria.setOffset(getIntParameter(request, "offset", 0));

        SearchResult result = searchService.search(criteria, SearchService.IndexType.SONG);
        org.subsonic.restapi.SearchResult searchResult = new org.subsonic.restapi.SearchResult();
        searchResult.setOffset(result.getOffset());
        searchResult.setTotalHits(result.getTotalHits());
        
        for (MediaFile mediaFile : result.getMediaFiles()) {
            searchResult.getMatch().add(createChild(player, mediaFile, username));
        }
        Response res = jaxbWriter.createResponse(true);
        res.setSearchResult(searchResult);
        jaxbWriter.writeResponse(request, response, res);
    }
    
    public void search2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        SearchResult2 searchResult = new SearchResult2();

        String query = request.getParameter("query");
        SearchCriteria criteria = new SearchCriteria();
        criteria.setQuery(StringUtils.trimToEmpty(query));
        criteria.setCount(getIntParameter(request, "artistCount", 20));
        criteria.setOffset(getIntParameter(request, "artistOffset", 0));
        SearchResult artists = searchService.search(criteria, SearchService.IndexType.ARTIST);
        for (MediaFile mediaFile : artists.getMediaFiles()) {
            searchResult.getArtist().add(createArtist(mediaFile, username));
        }

        criteria.setCount(getIntParameter(request, "albumCount", 20));
        criteria.setOffset(getIntParameter(request, "albumOffset", 0));
        SearchResult albums = searchService.search(criteria, SearchService.IndexType.ALBUM);
        for (MediaFile mediaFile : albums.getMediaFiles()) {
            searchResult.getAlbum().add(createChild(player, mediaFile, username));
        }

        criteria.setCount(getIntParameter(request, "songCount", 20));
        criteria.setOffset(getIntParameter(request, "songOffset", 0));
        SearchResult songs = searchService.search(criteria, SearchService.IndexType.SONG);
        for (MediaFile mediaFile : songs.getMediaFiles()) {
            searchResult.getSong().add(createChild(player, mediaFile, username));
        }

        Response res = jaxbWriter.createResponse(true);
        res.setSearchResult2(searchResult);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void search3(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        SearchResult3 searchResult = new SearchResult3();

        String query = request.getParameter("query");
        SearchCriteria criteria = new SearchCriteria();
        criteria.setQuery(StringUtils.trimToEmpty(query));
        criteria.setCount(getIntParameter(request, "artistCount", 20));
        criteria.setOffset(getIntParameter(request, "artistOffset", 0));
        SearchResult result = searchService.search(criteria, SearchService.IndexType.ARTIST_ID3);
        for (Artist artist : result.getArtists()) {
            searchResult.getArtist().add(createArtist(new ArtistID3(), artist, username));
        }

        criteria.setCount(getIntParameter(request, "albumCount", 20));
        criteria.setOffset(getIntParameter(request, "albumOffset", 0));
        result = searchService.search(criteria, SearchService.IndexType.ALBUM_ID3);
        for (Album album : result.getAlbums()) {
            searchResult.getAlbum().add(createAlbum(new AlbumID3(), album, username));
        }

        criteria.setCount(getIntParameter(request, "songCount", 20));
        criteria.setOffset(getIntParameter(request, "songOffset", 0));
        result = searchService.search(criteria, SearchService.IndexType.SONG);
        for (MediaFile song : result.getMediaFiles()) {
            searchResult.getSong().add(createChild(player, song, username));
        }

        Response res = jaxbWriter.createResponse(true);
        res.setSearchResult3(searchResult);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getPlaylists(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        User user = securityService.getCurrentUser(request);
        String authenticatedUsername = user.getUsername();
        String requestedUsername = request.getParameter("username");

        if (requestedUsername == null) {
            requestedUsername = authenticatedUsername;
        } else if (!user.isAdminRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, authenticatedUsername + " is not authorized to get playlists for " + requestedUsername);
            return;
        }

        Playlists result = new Playlists();

        for (Playlist playlist : playlistService.getReadablePlaylistsForUser(requestedUsername)) {
            result.getPlaylist().add(createPlaylist(new org.subsonic.restapi.Playlist(), playlist));
        }

        Response res = jaxbWriter.createResponse(true);
        res.setPlaylists(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getPlaylist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        int id = getRequiredIntParameter(request, "id");

        Playlist playlist = playlistService.getPlaylist(id);
        if (playlist == null) {
            error(request, response, ErrorCode.NOT_FOUND, "Playlist not found: " + id);
            return;
        }
        if (!playlistService.isReadAllowed(playlist, username)) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, "Permission denied for playlist " + id);
            return;
        }
        PlaylistWithSongs result = createPlaylist(new PlaylistWithSongs(), playlist);
        for (MediaFile mediaFile : playlistService.getFilesInPlaylist(id)) {
            result.getEntry().add(createChild(player, mediaFile, username));
        }

        Response res = jaxbWriter.createResponse(true);
        res.setPlaylist(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void jukeboxControl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request, true);

        User user = securityService.getCurrentUser(request);
        if (!user.isJukeboxRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to use jukebox.");
            return;
        }

        boolean returnPlaylist = false;
        String action = getRequiredStringParameter(request, "action");
        if ("start".equals(action)) {
            playQueueService.doStart(request, response);
        } else if ("stop".equals(action)) {
            playQueueService.doStop(request, response);
        } else if ("skip".equals(action)) {
            int index = getRequiredIntParameter(request, "index");
            int offset = getIntParameter(request, "offset", 0);
            playQueueService.doSkip(request, response, index, offset);
        } else if ("add".equals(action)) {
            int[] ids = getIntParameters(request, "id");
            playQueueService.doAdd(request, response, ids, null);
        } else if ("set".equals(action)) {
            int[] ids = getIntParameters(request, "id");
            playQueueService.doSet(request, response, ids);
        } else if ("clear".equals(action)) {
            playQueueService.doClear(request, response);
        } else if ("remove".equals(action)) {
            int index = getRequiredIntParameter(request, "index");
            playQueueService.doRemove(request, response, index);
        } else if ("shuffle".equals(action)) {
            playQueueService.doShuffle(request, response);
        } else if ("setGain".equals(action)) {
            float gain = getRequiredFloatParameter(request, "gain");
            jukeboxService.setGain(gain);
        } else if ("get".equals(action)) {
            returnPlaylist = true;
        } else if ("status".equals(action)) {
            // No action necessary.
        } else {
            throw new Exception("Unknown jukebox action: '" + action + "'.");
        }

        XMLBuilder builder = createXMLBuilder(request, response, true);

        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);
        Player jukeboxPlayer = jukeboxService.getPlayer();
        boolean controlsJukebox = jukeboxPlayer != null && jukeboxPlayer.getId().equals(player.getId());
        PlayQueue playQueue = player.getPlayQueue();

        List<Attribute> attrs = new ArrayList<Attribute>(Arrays.asList(
                new Attribute("currentIndex", controlsJukebox && !playQueue.isEmpty() ? playQueue.getIndex() : -1),
                new Attribute("playing", controlsJukebox && !playQueue.isEmpty() && playQueue.getStatus() == PlayQueue.Status.PLAYING),
                new Attribute("gain", jukeboxService.getGain()),
                new Attribute("position", controlsJukebox && !playQueue.isEmpty() ? jukeboxService.getPosition() : 0)));

        if (returnPlaylist) {
            builder.add("jukeboxPlaylist", attrs, false);
            List<MediaFile> result;
            synchronized (playQueue) {
                result = playQueue.getFiles();
            }
            for (MediaFile mediaFile : result) {
                AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
                builder.add("entry", attributes, true);
            }
        } else {
            builder.add("jukeboxStatus", attrs, false);
        }

        builder.endAll();
        response.getWriter().print(builder);
    }

    public void createPlaylist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request, true);
        String username = securityService.getCurrentUsername(request);

        Integer playlistId = getIntParameter(request, "playlistId");
        String name = request.getParameter("name");
        if (playlistId == null && name == null) {
            error(request, response, ErrorCode.MISSING_PARAMETER, "Playlist ID or name must be specified.");
            return;
        }

        Playlist playlist;
        if (playlistId != null) {
            playlist = playlistService.getPlaylist(playlistId);
            if (playlist == null) {
                error(request, response, ErrorCode.NOT_FOUND, "Playlist not found: " + playlistId);
                return;
            }
            if (!playlistService.isWriteAllowed(playlist, username)) {
                error(request, response, ErrorCode.NOT_AUTHORIZED, "Permission denied for playlist " + playlistId);
                return;
            }
        } else {
            playlist = new Playlist();
            playlist.setName(name);
            playlist.setCreated(new Date());
            playlist.setChanged(new Date());
            playlist.setShared(false);
            playlist.setUsername(username);
            playlistService.createPlaylist(playlist);
        }

        List<MediaFile> songs = new ArrayList<MediaFile>();
        for (int id : getIntParameters(request, "songId")) {
            MediaFile song = mediaFileService.getMediaFile(id);
            if (song != null) {
                songs.add(song);
            }
        }
        playlistService.setFilesInPlaylist(playlist.getId(), songs);

        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void updatePlaylist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request, true);
        String username = securityService.getCurrentUsername(request);

        int id = getRequiredIntParameter(request, "playlistId");
        Playlist playlist = playlistService.getPlaylist(id);
        if (playlist == null) {
            error(request, response, ErrorCode.NOT_FOUND, "Playlist not found: " + id);
            return;
        }
        if (!playlistService.isWriteAllowed(playlist, username)) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, "Permission denied for playlist " + id);
            return;
        }

        String name = request.getParameter("name");
        if (name != null) {
            playlist.setName(name);
        }
        String comment = request.getParameter("comment");
        if (comment != null) {
            playlist.setComment(comment);
        }
        Boolean shared = getBooleanParameter(request, "public");
        if (shared != null) {
            playlist.setShared(shared);
        }
        playlistService.updatePlaylist(playlist);

        // TODO: Add later
//            for (String usernameToAdd : ServletRequestUtils.getStringParameters(request, "usernameToAdd")) {
//                if (securityService.getUserByName(usernameToAdd) != null) {
//                    playlistService.addPlaylistUser(id, usernameToAdd);
//                }
//            }
//            for (String usernameToRemove : ServletRequestUtils.getStringParameters(request, "usernameToRemove")) {
//                if (securityService.getUserByName(usernameToRemove) != null) {
//                    playlistService.deletePlaylistUser(id, usernameToRemove);
//                }
//            }
        List<MediaFile> songs = playlistService.getFilesInPlaylist(id);
        boolean songsChanged = false;

        SortedSet<Integer> tmp = new TreeSet<Integer>();
        for (int songIndexToRemove : getIntParameters(request, "songIndexToRemove")) {
            tmp.add(songIndexToRemove);
        }
        List<Integer> songIndexesToRemove = new ArrayList<Integer>(tmp);
        Collections.reverse(songIndexesToRemove);
        for (Integer songIndexToRemove : songIndexesToRemove) {
            songs.remove(songIndexToRemove.intValue());
            songsChanged = true;
        }
        for (int songToAdd : getIntParameters(request, "songIdToAdd")) {
            MediaFile song = mediaFileService.getMediaFile(songToAdd);
            if (song != null) {
                songs.add(song);
                songsChanged = true;
            }
        }
        if (songsChanged) {
            playlistService.setFilesInPlaylist(id, songs);
        }

        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void deletePlaylist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request, true);
        String username = securityService.getCurrentUsername(request);

        int id = getRequiredIntParameter(request, "id");
        Playlist playlist = playlistService.getPlaylist(id);
        if (playlist == null) {
            error(request, response, ErrorCode.NOT_FOUND, "Playlist not found: " + id);
            return;
        }
        if (!playlistService.isWriteAllowed(playlist, username)) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, "Permission denied for playlist " + id);
            return;
        }
        playlistService.deletePlaylist(id);

        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void getAlbumList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        int size = getIntParameter(request, "size", 10);
        int offset = getIntParameter(request, "offset", 0);
        size = Math.max(0, Math.min(size, 500));
        String type = getRequiredStringParameter(request, "type");

        List<MediaFile> albums;
        if ("highest".equals(type)) {
            albums = ratingService.getHighestRatedAlbums(offset, size);
        } else if ("frequent".equals(type)) {
            albums = mediaFileService.getMostFrequentlyPlayedAlbums(offset, size);
        } else if ("recent".equals(type)) {
            albums = mediaFileService.getMostRecentlyPlayedAlbums(offset, size);
        } else if ("newest".equals(type)) {
            albums = mediaFileService.getNewestAlbums(offset, size);
        } else if ("starred".equals(type)) {
            albums = mediaFileService.getStarredAlbums(offset, size, username);
        } else if ("alphabeticalByArtist".equals(type)) {
            albums = mediaFileService.getAlphabeticalAlbums(offset, size, true);
        } else if ("alphabeticalByName".equals(type)) {
            albums = mediaFileService.getAlphabeticalAlbums(offset, size, false);
        } else if ("byGenre".equals(type)) {
            albums = mediaFileService.getAlbumsByGenre(offset, size, getRequiredStringParameter(request, "genre"));
        } else if ("byYear".equals(type)) {
            albums = mediaFileService.getAlbumsByYear(offset, size, getRequiredIntParameter(request, "fromYear"),
                    getRequiredIntParameter(request, "toYear"));
        } else if ("random".equals(type)) {
            albums = searchService.getRandomAlbums(size);
        } else {
            throw new Exception("Invalid list type: " + type);
        }

        AlbumList result = new AlbumList();
        for (MediaFile album : albums) {
            result.getAlbum().add(createChild(player, album, username));
        }
        
        Response res = jaxbWriter.createResponse(true);
        res.setAlbumList(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getAlbumList2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        int size = getIntParameter(request, "size", 10);
        int offset = getIntParameter(request, "offset", 0);
        size = Math.max(0, Math.min(size, 500));
        String type = getRequiredStringParameter(request, "type");
        String username = securityService.getCurrentUsername(request);

        List<Album> albums;
        if ("frequent".equals(type)) {
            albums = albumDao.getMostFrequentlyPlayedAlbums(offset, size);
        } else if ("recent".equals(type)) {
            albums = albumDao.getMostRecentlyPlayedAlbums(offset, size);
        } else if ("newest".equals(type)) {
            albums = albumDao.getNewestAlbums(offset, size);
        } else if ("alphabeticalByArtist".equals(type)) {
            albums = albumDao.getAlphabetialAlbums(offset, size, true);
        } else if ("alphabeticalByName".equals(type)) {
            albums = albumDao.getAlphabetialAlbums(offset, size, false);
        } else if ("byGenre".equals(type)) {
            albums = albumDao.getAlbumsByGenre(offset, size, getRequiredStringParameter(request, "genre"));
        } else if ("byYear".equals(type)) {
            albums = albumDao.getAlbumsByYear(offset, size, getRequiredIntParameter(request, "fromYear"),
                    getRequiredIntParameter(request, "toYear"));
        } else if ("starred".equals(type)) {
            albums = albumDao.getStarredAlbums(offset, size, securityService.getCurrentUser(request).getUsername());
        } else if ("random".equals(type)) {
            albums = searchService.getRandomAlbumsId3(size);
        } else {
            throw new Exception("Invalid list type: " + type);
        }
        AlbumList2 result = new AlbumList2();
        for (Album album : albums) {
            result.getAlbum().add(createAlbum(new AlbumID3(), album, username));
        }
        Response res = jaxbWriter.createResponse(true);
        res.setAlbumList2(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getRandomSongs(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        int size = getIntParameter(request, "size", 10);
        size = Math.max(0, Math.min(size, 500));
        String genre = getStringParameter(request, "genre");
        Integer fromYear = getIntParameter(request, "fromYear");
        Integer toYear = getIntParameter(request, "toYear");
        Integer musicFolderId = getIntParameter(request, "musicFolderId");
        RandomSearchCriteria criteria = new RandomSearchCriteria(size, genre, fromYear, toYear, musicFolderId);

        Songs result = new Songs();
        for (MediaFile mediaFile : searchService.getRandomSongs(criteria)) {
            result.getSong().add(createChild(player, mediaFile, username));
        }
        Response res = jaxbWriter.createResponse(true);
        res.setRandomSongs(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getVideos(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        int size = getIntParameter(request, "size", Integer.MAX_VALUE);
        int offset = getIntParameter(request, "offset", 0);

        Videos result = new Videos();
        for (MediaFile mediaFile : mediaFileDao.getVideos(size, offset)) {
            result.getVideo().add(createChild(player, mediaFile, username));
        }
        Response res = jaxbWriter.createResponse(true);
        res.setVideos(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getNowPlaying(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        NowPlaying result = new NowPlaying();

        for (TransferStatus status : statusService.getAllStreamStatuses()) {

            Player player = status.getPlayer();
            File file = status.getFile();
            if (player != null && player.getUsername() != null && file != null) {

                String username = player.getUsername();
                UserSettings userSettings = settingsService.getUserSettings(username);
                if (!userSettings.isNowPlayingAllowed()) {
                    continue;
                }

                MediaFile mediaFile = mediaFileService.getMediaFile(file);

                long minutesAgo = status.getMillisSinceLastUpdate() / 1000L / 60L;
                if (minutesAgo < 60) {
                    NowPlayingEntry entry = new NowPlayingEntry();
                    entry.setUsername(username);
                    entry.setPlayerId(Integer.parseInt(player.getId()));
                    entry.setPlayerName(player.getName());
                    entry.setMinutesAgo((int) minutesAgo);
                    result.getEntry().add(createChild(entry, player, mediaFile, username));
                }
            }
        }

        Response res = jaxbWriter.createResponse(true);
        res.setNowPlaying(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    private Child createChild(Player player, MediaFile mediaFile, String username) {
        return createChild(new Child(), player, mediaFile, username);
    }
    
    private <T extends Child> T createChild(T child, Player player, MediaFile mediaFile, String username) {
        MediaFile parent = mediaFileService.getParentOf(mediaFile);
        child.setId(String.valueOf(mediaFile.getId()));
        try {
            if (!mediaFileService.isRoot(parent)) {
                child.setParent(String.valueOf(parent.getId()));
            }
        } catch (SecurityException x) {
            // Ignored.
        }
        child.setTitle(mediaFile.getName());
        child.setAlbum(mediaFile.getAlbumName());
        child.setArtist(mediaFile.getArtist());
        child.setIsDir(mediaFile.isDirectory());
        child.setCoverArt(findCoverArt(mediaFile, parent));
        child.setYear(mediaFile.getYear());
        child.setGenre(mediaFile.getGenre());
        child.setCreated(jaxbWriter.convertDate(mediaFile.getCreated()));
        child.setStarred(jaxbWriter.convertDate(mediaFileDao.getMediaFileStarredDate(mediaFile.getId(), username)));
        child.setUserRating(ratingService.getRatingForUser(username, mediaFile));
        child.setAverageRating(ratingService.getAverageRating(mediaFile));

        if (mediaFile.isFile()) {
            child.setDuration(mediaFile.getDurationSeconds());
            child.setBitRate(mediaFile.getBitRate());
            child.setTrack(mediaFile.getTrackNumber());
            child.setDiscNumber(mediaFile.getDiscNumber());
            child.setSize(mediaFile.getFileSize());
            String suffix = mediaFile.getFormat();
            child.setSuffix(suffix);
            child.setContentType(StringUtil.getMimeType(suffix));
            child.setIsVideo(mediaFile.isVideo());
            child.setPath(getRelativePath(mediaFile));

            Bookmark bookmark = bookmarkCache.get(new BookmarkKey(username, mediaFile.getId()));
            if (bookmark != null) {
                child.setBookmarkPosition(bookmark.getPositionMillis());
            }

            if (mediaFile.getAlbumArtist() != null && mediaFile.getAlbumName() != null) {
                Album album = albumDao.getAlbum(mediaFile.getAlbumArtist(), mediaFile.getAlbumName());
                if (album != null) {
                    child.setAlbumId(String.valueOf(album.getId()));
                }
            }
            if (mediaFile.getArtist() != null) {
                Artist artist = artistDao.getArtist(mediaFile.getArtist());
                if (artist != null) {
                    child.setArtistId(String.valueOf(artist.getId()));
                }
            }
            switch (mediaFile.getMediaType()) {
                case MUSIC:
                    child.setType(MediaType.MUSIC);
                    break;
                case PODCAST:
                    child.setType(MediaType.PODCAST);
                    break;
                case AUDIOBOOK:
                    child.setType(MediaType.AUDIOBOOK);
                    break;
                case VIDEO:
                    child.setType(MediaType.VIDEO);
                    break;
                default:
                    break;
            }

            if (transcodingService.isTranscodingRequired(mediaFile, player)) {
                String transcodedSuffix = transcodingService.getSuffix(player, mediaFile, null);
                child.setTranscodedSuffix(transcodedSuffix);
                child.setTranscodedContentType(StringUtil.getMimeType(transcodedSuffix));
            }
        }
        return child;
    }

    private AttributeSet createAttributesForMediaFile(Player player, MediaFile mediaFile, String username) {
        MediaFile parent = mediaFileService.getParentOf(mediaFile);
        AttributeSet attributes = new AttributeSet();
        attributes.add("id", mediaFile.getId());
        try {
            if (!mediaFileService.isRoot(parent)) {
                attributes.add("parent", parent.getId());
            }
        } catch (SecurityException x) {
            // Ignored.
        }
        attributes.add("title", mediaFile.getName());
        attributes.add("album", mediaFile.getAlbumName());
        attributes.add("artist", mediaFile.getArtist());
        attributes.add("isDir", mediaFile.isDirectory());
        attributes.add("coverArt", findCoverArt(mediaFile, parent));
        attributes.add("year", mediaFile.getYear());
        attributes.add("genre", mediaFile.getGenre());
        attributes.add("created", StringUtil.toISO8601(mediaFile.getCreated()));
        attributes.add("starred", StringUtil.toISO8601(mediaFileDao.getMediaFileStarredDate(mediaFile.getId(), username)));
        attributes.add("userRating", ratingService.getRatingForUser(username, mediaFile));
        attributes.add("averageRating", ratingService.getAverageRating(mediaFile));

        if (mediaFile.isFile()) {
            attributes.add("duration", mediaFile.getDurationSeconds());
            attributes.add("bitRate", mediaFile.getBitRate());
            attributes.add("track", mediaFile.getTrackNumber());
            attributes.add("discNumber", mediaFile.getDiscNumber());
            attributes.add("size", mediaFile.getFileSize());
            String suffix = mediaFile.getFormat();
            attributes.add("suffix", suffix);
            attributes.add("contentType", StringUtil.getMimeType(suffix));
            attributes.add("isVideo", mediaFile.isVideo());
            attributes.add("path", getRelativePath(mediaFile));

            Bookmark bookmark = bookmarkCache.get(new BookmarkKey(username, mediaFile.getId()));
            if (bookmark != null) {
                attributes.add("bookmarkPosition", bookmark.getPositionMillis());
            }

            if (mediaFile.getAlbumArtist() != null && mediaFile.getAlbumName() != null) {
                Album album = albumDao.getAlbum(mediaFile.getAlbumArtist(), mediaFile.getAlbumName());
                if (album != null) {
                    attributes.add("albumId", album.getId());
                }
            }
            if (mediaFile.getArtist() != null) {
                Artist artist = artistDao.getArtist(mediaFile.getArtist());
                if (artist != null) {
                    attributes.add("artistId", artist.getId());
                }
            }
            switch (mediaFile.getMediaType()) {
                case MUSIC:
                    attributes.add("type", "music");
                    break;
                case PODCAST:
                    attributes.add("type", "podcast");
                    break;
                case AUDIOBOOK:
                    attributes.add("type", "audiobook");
                    break;
                default:
                    break;
            }

            if (transcodingService.isTranscodingRequired(mediaFile, player)) {
                String transcodedSuffix = transcodingService.getSuffix(player, mediaFile, null);
                attributes.add("transcodedSuffix", transcodedSuffix);
                attributes.add("transcodedContentType", StringUtil.getMimeType(transcodedSuffix));
            }
        }
        return attributes;
    }

    private String findCoverArt(MediaFile mediaFile, MediaFile parent) {
        MediaFile dir = mediaFile.isDirectory() ? mediaFile : parent;
        if (dir != null && dir.getCoverArtPath() != null) {
            return String.valueOf(dir.getId());
        }
        return null;
    }

    private String getRelativePath(MediaFile musicFile) {

        String filePath = musicFile.getPath();

        // Convert slashes.
        filePath = filePath.replace('\\', '/');

        String filePathLower = filePath.toLowerCase();

        List<MusicFolder> musicFolders = settingsService.getAllMusicFolders(false, true);
        for (MusicFolder musicFolder : musicFolders) {
            String folderPath = musicFolder.getPath().getPath();
            folderPath = folderPath.replace('\\', '/');
            String folderPathLower = folderPath.toLowerCase();
            if (!folderPathLower.endsWith("/")) {
                folderPathLower += "/";
            }

            if (filePathLower.startsWith(folderPathLower)) {
                String relativePath = filePath.substring(folderPath.length());
                return relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            }
        }

        return null;
    }

    public ModelAndView download(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isDownloadRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to download files.");
            return null;
        }

        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        long lastModified = downloadController.getLastModified(request);

        if (ifModifiedSince != -1 && lastModified != -1 && lastModified <= ifModifiedSince) {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return null;
        }

        if (lastModified != -1) {
            response.setDateHeader("Last-Modified", lastModified);
        }

        return downloadController.handleRequest(request, response);
    }

    public ModelAndView stream(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isStreamRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to play files.");
            return null;
        }

        streamController.handleRequest(request, response);
        return null;
    }

    public ModelAndView hls(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isStreamRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to play files.");
            return null;
        }
        hlsController.handleRequest(request, response);
        return null;
    }

    public void scrobble(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        Player player = playerService.getPlayer(request, response);

        if (!settingsService.getUserSettings(player.getUsername()).isLastFmEnabled()) {
            error(request, response, ErrorCode.GENERIC, "Scrobbling is not enabled for " + player.getUsername() + ".");
            return;
        }

        boolean submission = getBooleanParameter(request, "submission", true);
        int[] ids = getRequiredIntParameters(request, "id");
        long[] times = getLongParameters(request, "time");
        if (times.length > 0 && times.length != ids.length) {
            error(request, response, ErrorCode.GENERIC, "Wrong number of timestamps: " + times.length);
            return;
        }

        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            MediaFile file = mediaFileService.getMediaFile(id);
            if (file == null) {
                LOG.warn("File to scrobble not found: " + id);
                continue;
            }
            Date time = times.length == 0 ? null : new Date(times[i]);
            audioScrobblerService.register(file, player.getUsername(), submission, time);
        }

        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void star(HttpServletRequest request, HttpServletResponse response) throws Exception {
        starOrUnstar(request, response, true);
    }

    public void unstar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        starOrUnstar(request, response, false);
    }

    private void starOrUnstar(HttpServletRequest request, HttpServletResponse response, boolean star) throws Exception {
        request = wrapRequest(request);

        String username = securityService.getCurrentUser(request).getUsername();
        for (int id : getIntParameters(request, "id")) {
            MediaFile mediaFile = mediaFileDao.getMediaFile(id);
            if (mediaFile == null) {
                error(request, response, ErrorCode.NOT_FOUND, "Media file not found: " + id);
                return;
            }
            if (star) {
                mediaFileDao.starMediaFile(id, username);
            } else {
                mediaFileDao.unstarMediaFile(id, username);
            }
        }
        for (int albumId : getIntParameters(request, "albumId")) {
            Album album = albumDao.getAlbum(albumId);
            if (album == null) {
                error(request, response, ErrorCode.NOT_FOUND, "Album not found: " + albumId);
                return;
            }
            if (star) {
                albumDao.starAlbum(albumId, username);
            } else {
                albumDao.unstarAlbum(albumId, username);
            }
        }
        for (int artistId : getIntParameters(request, "artistId")) {
            Artist artist = artistDao.getArtist(artistId);
            if (artist == null) {
                error(request, response, ErrorCode.NOT_FOUND, "Artist not found: " + artistId);
                return;
            }
            if (star) {
                artistDao.starArtist(artistId, username);
            } else {
                artistDao.unstarArtist(artistId, username);
            }
        }

        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void getStarred(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        Starred result = new Starred();
        for (MediaFile artist : mediaFileDao.getStarredDirectories(0, Integer.MAX_VALUE, username)) {
            result.getArtist().add(createArtist(artist, username));
        }
        for (MediaFile album : mediaFileDao.getStarredAlbums(0, Integer.MAX_VALUE, username)) {
            result.getAlbum().add(createChild(player, album, username));
        }
        for (MediaFile song : mediaFileDao.getStarredFiles(0, Integer.MAX_VALUE, username)) {
            result.getSong().add(createChild(player, song, username));
        }
        Response res = jaxbWriter.createResponse(true);
        res.setStarred(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getStarred2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        Starred2 result = new Starred2();
        for (Artist artist : artistDao.getStarredArtists(0, Integer.MAX_VALUE, username)) {
            result.getArtist().add(createArtist(new ArtistID3(), artist, username));
        }
        for (Album album : albumDao.getStarredAlbums(0, Integer.MAX_VALUE, username)) {
            result.getAlbum().add(createAlbum(new AlbumID3(), album, username));
        }
        for (MediaFile song : mediaFileDao.getStarredFiles(0, Integer.MAX_VALUE, username)) {
            result.getSong().add(createChild(player, song, username));
        }
        Response res = jaxbWriter.createResponse(true);
        res.setStarred2(result);
        jaxbWriter.writeResponse(request, response, res);
    }

    public void getPodcasts(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);
        boolean includeEpisodes = getBooleanParameter(request, "includeEpisodes", true);
        Integer channelId = getIntParameter(request, "id");

        XMLBuilder builder = createXMLBuilder(request, response, true);
        builder.add("podcasts", false);

        for (PodcastChannel channel : podcastService.getAllChannels()) {
            if (channelId == null || channelId.equals(channel.getId())) {
                AttributeSet channelAttrs = new AttributeSet();
                channelAttrs.add("id", channel.getId());
                channelAttrs.add("url", channel.getUrl());
                channelAttrs.add("status", channel.getStatus().toString().toLowerCase());
                channelAttrs.add("title", channel.getTitle());
                channelAttrs.add("description", channel.getDescription());
                channelAttrs.add("errorMessage", channel.getErrorMessage());
                builder.add("channel", channelAttrs, false);

                if (includeEpisodes) {
                    List<PodcastEpisode> episodes = podcastService.getEpisodes(channel.getId(), false);
                    for (PodcastEpisode episode : episodes) {
                        AttributeSet episodeAttrs = new AttributeSet();

                        String path = episode.getPath();
                        if (path != null) {
                            MediaFile mediaFile = mediaFileService.getMediaFile(path);
                            episodeAttrs.addAll(createAttributesForMediaFile(player, mediaFile, username));
                            episodeAttrs.add("streamId", mediaFile.getId());
                        }

                        episodeAttrs.add("id", episode.getId());  // Overwrites the previous "id" attribute.
                        episodeAttrs.add("status", episode.getStatus().toString().toLowerCase());
                        episodeAttrs.add("title", episode.getTitle());
                        episodeAttrs.add("description", episode.getDescription());
                        episodeAttrs.add("publishDate", episode.getPublishDate());

                        builder.add("episode", episodeAttrs, true);
                    }
                }
                builder.end(); // <channel>
            }
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void refreshPodcasts(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isPodcastRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to administrate podcasts.");
            return;
        }
        podcastService.refreshAllChannels(true);
        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void createPodcastChannel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isPodcastRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to administrate podcasts.");
            return;
        }

        String url = getRequiredStringParameter(request, "url");
        podcastService.createChannel(url);
        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void deletePodcastChannel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isPodcastRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to administrate podcasts.");
            return;
        }

        int id = getRequiredIntParameter(request, "id");
        podcastService.deleteChannel(id);
        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void deletePodcastEpisode(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isPodcastRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to administrate podcasts.");
            return;
        }

        int id = getRequiredIntParameter(request, "id");
        podcastService.deleteEpisode(id, true);
        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void downloadPodcastEpisode(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isPodcastRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to administrate podcasts.");
            return;
        }

        int id = getRequiredIntParameter(request, "id");
        PodcastEpisode episode = podcastService.getEpisode(id, true);
        if (episode == null) {
            error(request, response, ErrorCode.NOT_FOUND, "Podcast episode " + id + " not found.");
            return;
        }

        podcastService.downloadEpisode(episode);
        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void getInternetRadioStations(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        builder.add("internetRadioStations", false);
        for (InternetRadio radio : settingsService.getAllInternetRadios()) {
            AttributeSet attrs = new AttributeSet();
            attrs.add("id", radio.getId());
            attrs.add("name", radio.getName());
            attrs.add("streamUrl", radio.getStreamUrl());
            attrs.add("homePageUrl", radio.getHomepageUrl());
            builder.add("internetRadioStation", attrs, true);
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getBookmarks(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        XMLBuilder builder = createXMLBuilder(request, response, true);

        builder.add("bookmarks", false);
        for (Bookmark bookmark : bookmarkDao.getBookmarks(username)) {
            builder.add("bookmark", createAttributesForBookmark(bookmark), false);
            MediaFile mediaFile = mediaFileService.getMediaFile(bookmark.getMediaFileId());
            AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
            builder.add("entry", attributes, true);
            builder.end();
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void createBookmark(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        String username = securityService.getCurrentUsername(request);
        int mediaFileId = getRequiredIntParameter(request, "id");
        long position = getRequiredLongParameter(request, "position");
        String comment = request.getParameter("comment");
        Date now = new Date();

        Bookmark bookmark = new Bookmark(0, mediaFileId, position, username, comment, now, now);
        bookmarkDao.createOrUpdateBookmark(bookmark);
        refreshBookmarkCache();
        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void deleteBookmark(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        String username = securityService.getCurrentUsername(request);
        int mediaFileId = getRequiredIntParameter(request, "id");
        bookmarkDao.deleteBookmark(username, mediaFileId);
        refreshBookmarkCache();

        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void getShares(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        User user = securityService.getCurrentUser(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        builder.add("shares", false);
        for (Share share : shareService.getSharesForUser(user)) {
            builder.add("share", createAttributesForShare(share), false);

            for (MediaFile mediaFile : shareService.getSharedFiles(share.getId())) {
                AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
                builder.add("entry", attributes, true);
            }

            builder.end();
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void createShare(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        User user = securityService.getCurrentUser(request);
        if (!user.isShareRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to share media.");
            return;
        }

        if (!settingsService.isUrlRedirectionEnabled()) {
            error(request, response, ErrorCode.GENERIC, "Sharing is only supported for *.subsonic.org domain names.");
            return;
        }

        XMLBuilder builder = createXMLBuilder(request, response, true);

        List<MediaFile> files = new ArrayList<MediaFile>();
        for (int id : getRequiredIntParameters(request, "id")) {
            files.add(mediaFileService.getMediaFile(id));
        }

        // TODO: Update api.jsp

        Share share = shareService.createShare(request, files);
        share.setDescription(request.getParameter("description"));
        long expires = getLongParameter(request, "expires", 0L);
        if (expires != 0) {
            share.setExpires(new Date(expires));
        }
        shareService.updateShare(share);

        builder.add("shares", false);
        builder.add("share", createAttributesForShare(share), false);

        for (MediaFile mediaFile : shareService.getSharedFiles(share.getId())) {
            AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
            builder.add("entry", attributes, true);
        }

        builder.endAll();
        response.getWriter().print(builder);
    }

    public void deleteShare(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        int id = getRequiredIntParameter(request, "id");

        Share share = shareService.getShareById(id);
        if (share == null) {
            error(request, response, ErrorCode.NOT_FOUND, "Shared media not found.");
            return;
        }
        if (!user.isAdminRole() && !share.getUsername().equals(user.getUsername())) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, "Not authorized to delete shared media.");
            return;
        }

        shareService.deleteShare(id);
        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void updateShare(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        int id = getRequiredIntParameter(request, "id");

        Share share = shareService.getShareById(id);
        if (share == null) {
            error(request, response, ErrorCode.NOT_FOUND, "Shared media not found.");
            return;
        }
        if (!user.isAdminRole() && !share.getUsername().equals(user.getUsername())) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, "Not authorized to modify shared media.");
            return;
        }

        share.setDescription(request.getParameter("description"));
        String expiresString = request.getParameter("expires");
        if (expiresString != null) {
            long expires = Long.parseLong(expiresString);
            share.setExpires(expires == 0L ? null : new Date(expires));
        }
        shareService.updateShare(share);
        jaxbWriter.writeEmptyResponse(request, response);
    }

    private List<Attribute> createAttributesForShare(Share share) {
        List<Attribute> attributes = new ArrayList<Attribute>();

        attributes.add(new Attribute("id", share.getId()));
        attributes.add(new Attribute("url", shareService.getShareUrl(share)));
        attributes.add(new Attribute("username", share.getUsername()));
        attributes.add(new Attribute("created", StringUtil.toISO8601(share.getCreated())));
        attributes.add(new Attribute("visitCount", share.getVisitCount()));
        attributes.add(new Attribute("description", share.getDescription()));
        attributes.add(new Attribute("expires", StringUtil.toISO8601(share.getExpires())));
        attributes.add(new Attribute("lastVisited", StringUtil.toISO8601(share.getLastVisited())));

        return attributes;
    }

    private List<Attribute> createAttributesForBookmark(Bookmark bookmark) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("position", bookmark.getPositionMillis()));
        attributes.add(new Attribute("username", bookmark.getUsername()));
        attributes.add(new Attribute("comment", bookmark.getComment()));
        attributes.add(new Attribute("created", StringUtil.toISO8601(bookmark.getCreated())));
        attributes.add(new Attribute("changed", StringUtil.toISO8601(bookmark.getChanged())));
        return attributes;
    }

    public ModelAndView videoPlayer(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        Map<String, Object> map = new HashMap<String, Object>();
        int id = getRequiredIntParameter(request, "id");
        MediaFile file = mediaFileService.getMediaFile(id);

        int timeOffset = getIntParameter(request, "timeOffset", 0);
        timeOffset = Math.max(0, timeOffset);
        Integer duration = file.getDurationSeconds();
        if (duration != null) {
            map.put("skipOffsets", VideoPlayerController.createSkipOffsets(duration));
            timeOffset = Math.min(duration, timeOffset);
            duration -= timeOffset;
        }

        map.put("id", request.getParameter("id"));
        map.put("u", request.getParameter("u"));
        map.put("p", request.getParameter("p"));
        map.put("c", request.getParameter("c"));
        map.put("v", request.getParameter("v"));
        map.put("video", file);
        map.put("maxBitRate", getIntParameter(request, "maxBitRate", VideoPlayerController.DEFAULT_BIT_RATE));
        map.put("duration", duration);
        map.put("timeOffset", timeOffset);
        map.put("bitRates", VideoPlayerController.BIT_RATES);
        map.put("autoplay", getBooleanParameter(request, "autoplay", true));

        ModelAndView result = new ModelAndView("rest/videoPlayer");
        result.addObject("model", map);
        return result;
    }

    public ModelAndView getCoverArt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        return coverArtController.handleRequest(request, response);
    }

    public ModelAndView getAvatar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        return avatarController.handleRequest(request, response);
    }

    public void changePassword(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        String username = getRequiredStringParameter(request, "username");
        String password = decrypt(getRequiredStringParameter(request, "password"));

        User authUser = securityService.getCurrentUser(request);
        if (!authUser.isAdminRole() && !username.equals(authUser.getUsername())) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, authUser.getUsername() + " is not authorized to change password for " + username);
            return;
        }

        User user = securityService.getUserByName(username);
        user.setPassword(password);
        securityService.updateUser(user);

        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void getUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        String username = getRequiredStringParameter(request, "username");

        User currentUser = securityService.getCurrentUser(request);
        if (!username.equals(currentUser.getUsername()) && !currentUser.isAdminRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, currentUser.getUsername() + " is not authorized to get details for other users.");
            return;
        }

        User requestedUser = securityService.getUserByName(username);
        if (requestedUser == null) {
            error(request, response, ErrorCode.NOT_FOUND, "No such user: " + username);
            return;
        }

        UserSettings userSettings = settingsService.getUserSettings(username);

        XMLBuilder builder = createXMLBuilder(request, response, true);
        List<Attribute> attributes = createAttributesForUser(requestedUser, userSettings);

        builder.add("user", attributes, true);
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getUsers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        User currentUser = securityService.getCurrentUser(request);
        if (!currentUser.isAdminRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, currentUser.getUsername() + " is not authorized to get details for other users.");
            return;
        }

        XMLBuilder builder = createXMLBuilder(request, response, true);
        builder.add("users", false);
        for (User user : securityService.getAllUsers()) {
            UserSettings userSettings = settingsService.getUserSettings(user.getUsername());
            List<Attribute> attributes = createAttributesForUser(user, userSettings);
            builder.add("user", attributes, true);

        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    private List<Attribute> createAttributesForUser(User user, UserSettings userSettings) {
        return Arrays.asList(
                new Attribute("username", user.getUsername()),
                new Attribute("email", user.getEmail()),
                new Attribute("scrobblingEnabled", userSettings.isLastFmEnabled()),
                new Attribute("adminRole", user.isAdminRole()),
                new Attribute("settingsRole", user.isSettingsRole()),
                new Attribute("downloadRole", user.isDownloadRole()),
                new Attribute("uploadRole", user.isUploadRole()),
                new Attribute("playlistRole", true),  // Since 1.8.0
                new Attribute("coverArtRole", user.isCoverArtRole()),
                new Attribute("commentRole", user.isCommentRole()),
                new Attribute("podcastRole", user.isPodcastRole()),
                new Attribute("streamRole", user.isStreamRole()),
                new Attribute("jukeboxRole", user.isJukeboxRole()),
                new Attribute("shareRole", user.isShareRole())
        );
    }

    public void createUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isAdminRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to create new users.");
            return;
        }

        UserSettingsCommand command = new UserSettingsCommand();
        command.setUsername(getRequiredStringParameter(request, "username"));
        command.setPassword(decrypt(getRequiredStringParameter(request, "password")));
        command.setEmail(getRequiredStringParameter(request, "email"));
        command.setLdapAuthenticated(getBooleanParameter(request, "ldapAuthenticated", false));
        command.setAdminRole(getBooleanParameter(request, "adminRole", false));
        command.setCommentRole(getBooleanParameter(request, "commentRole", false));
        command.setCoverArtRole(getBooleanParameter(request, "coverArtRole", false));
        command.setDownloadRole(getBooleanParameter(request, "downloadRole", false));
        command.setStreamRole(getBooleanParameter(request, "streamRole", true));
        command.setUploadRole(getBooleanParameter(request, "uploadRole", false));
        command.setJukeboxRole(getBooleanParameter(request, "jukeboxRole", false));
        command.setPodcastRole(getBooleanParameter(request, "podcastRole", false));
        command.setSettingsRole(getBooleanParameter(request, "settingsRole", true));
        command.setShareRole(getBooleanParameter(request, "shareRole", false));
        command.setTranscodeSchemeName(TranscodeScheme.OFF.name());

        userSettingsController.createUser(command);
        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void updateUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isAdminRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to update users.");
            return;
        }

        String username = getRequiredStringParameter(request, "username");
        User u = securityService.getUserByName(username);
        UserSettings s = settingsService.getUserSettings(username);

        if (u == null) {
            error(request, response, ErrorCode.NOT_FOUND, "No such user: " + username);
            return;
        } else if (User.USERNAME_ADMIN.equals(username)) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, "Not allowed to change admin user");
            return;
        }

        UserSettingsCommand command = new UserSettingsCommand();
        command.setUsername(username);
        command.setEmail(getStringParameter(request, "email", u.getEmail()));
        command.setLdapAuthenticated(getBooleanParameter(request, "ldapAuthenticated", u.isLdapAuthenticated()));
        command.setAdminRole(getBooleanParameter(request, "adminRole", u.isAdminRole()));
        command.setCommentRole(getBooleanParameter(request, "commentRole", u.isCommentRole()));
        command.setCoverArtRole(getBooleanParameter(request, "coverArtRole", u.isCoverArtRole()));
        command.setDownloadRole(getBooleanParameter(request, "downloadRole", u.isDownloadRole()));
        command.setStreamRole(getBooleanParameter(request, "streamRole", u.isDownloadRole()));
        command.setUploadRole(getBooleanParameter(request, "uploadRole", u.isUploadRole()));
        command.setJukeboxRole(getBooleanParameter(request, "jukeboxRole", u.isJukeboxRole()));
        command.setPodcastRole(getBooleanParameter(request, "podcastRole", u.isPodcastRole()));
        command.setSettingsRole(getBooleanParameter(request, "settingsRole", u.isSettingsRole()));
        command.setShareRole(getBooleanParameter(request, "shareRole", u.isShareRole()));
        command.setTranscodeSchemeName(s.getTranscodeScheme().name());

        if (hasParameter(request, "password")) {
            command.setPassword(decrypt(getRequiredStringParameter(request, "password")));
            command.setPasswordChange(true);
        }

        userSettingsController.updateUser(command);
        jaxbWriter.writeEmptyResponse(request, response);
    }

    private boolean hasParameter(HttpServletRequest request, String name) {
        return request.getParameter(name) != null;
    }

    public void deleteUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isAdminRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to delete users.");
            return;
        }

        String username = getRequiredStringParameter(request, "username");
        if (User.USERNAME_ADMIN.equals(username)) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, "Not allowed to delete admin user");
            return;
        }

        securityService.deleteUser(username);

        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void getChatMessages(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        long since = getLongParameter(request, "since", 0L);

        builder.add("chatMessages", false);

        for (ChatService.Message message : chatService.getMessages(0L).getMessages()) {
            long time = message.getDate().getTime();
            if (time > since) {
                builder.add("chatMessage", true, new Attribute("username", message.getUsername()),
                        new Attribute("time", time), new Attribute("message", message.getContent()));
            }
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void addChatMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        chatService.doAddMessage(getRequiredStringParameter(request, "message"), request);
        jaxbWriter.writeEmptyResponse(request, response);
    }

    public void getLyrics(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        String artist = request.getParameter("artist");
        String title = request.getParameter("title");
        LyricsInfo lyrics = lyricsService.getLyrics(artist, title);

        XMLBuilder builder = createXMLBuilder(request, response, true);
        AttributeSet attributes = new AttributeSet();
        attributes.add("artist", lyrics.getArtist());
        attributes.add("title", lyrics.getTitle());
        builder.add("lyrics", attributes, lyrics.getLyrics(), true);

        builder.endAll();
        response.getWriter().print(builder);
    }

    public void setRating(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Integer rating = getRequiredIntParameter(request, "rating");
        if (rating == 0) {
            rating = null;
        }

        int id = getRequiredIntParameter(request, "id");
        MediaFile mediaFile = mediaFileService.getMediaFile(id);
        if (mediaFile == null) {
            error(request, response, ErrorCode.NOT_FOUND, "File not found: " + id);
            return;
        }

        String username = securityService.getCurrentUsername(request);
        ratingService.setRatingForUser(username, mediaFile, rating);

        jaxbWriter.writeEmptyResponse(request, response);
    }

    private HttpServletRequest wrapRequest(HttpServletRequest request) {
        return wrapRequest(request, false);
    }

    private HttpServletRequest wrapRequest(final HttpServletRequest request, boolean jukebox) {
        final String playerId = createPlayerIfNecessary(request, jukebox);
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                // Returns the correct player to be used in PlayerService.getPlayer()
                if ("player".equals(name)) {
                    return playerId;
                }

                // Support old style ID parameters.
                if ("id".equals(name)) {
                    return mapId(request.getParameter("id"));
                }

                return super.getParameter(name);
            }
        };
    }

    private String mapId(String id) {
        if (id == null || id.startsWith(CoverArtController.ALBUM_COVERART_PREFIX) ||
                id.startsWith(CoverArtController.ARTIST_COVERART_PREFIX) || StringUtils.isNumeric(id)) {
            return id;
        }

        try {
            String path = StringUtil.utf8HexDecode(id);
            MediaFile mediaFile = mediaFileService.getMediaFile(path);
            return String.valueOf(mediaFile.getId());
        } catch (Exception x) {
            return id;
        }
    }

    public static void error(HttpServletRequest request, HttpServletResponse response, ErrorCode code, String message) throws IOException {
        XMLBuilder builder = createXMLBuilder(request, response, false);
        builder.add("error", true,
                new XMLBuilder.Attribute("code", code.getCode()),
                new XMLBuilder.Attribute("message", message));
        builder.end();
        response.getWriter().print(builder);
    }

    @Deprecated
    private static XMLBuilder createXMLBuilder(HttpServletRequest request, HttpServletResponse response, boolean ok) throws IOException {
        String format = getStringParameter(request, "f", "xml");
        boolean json = "json".equals(format);
        boolean jsonp = "jsonp".equals(format);
        XMLBuilder builder;

        response.setCharacterEncoding(StringUtil.ENCODING_UTF8);

        if (json) {
            builder = XMLBuilder.createJSONBuilder();
            response.setContentType("application/json");
        } else if (jsonp) {
            builder = XMLBuilder.createJSONPBuilder(request.getParameter("callback"));
            response.setContentType("text/javascript");
        } else {
            builder = XMLBuilder.createXMLBuilder();
            response.setContentType("text/xml");
        }

        builder.preamble(StringUtil.ENCODING_UTF8);
        builder.add("subsonic-response", false,
                new Attribute("xmlns", "http://subsonic.org/restapi"),
                new Attribute("status", ok ? "ok" : "failed"),
                new Attribute("version", StringUtil.getRESTProtocolVersion()));
        return builder;
    }

    private String createPlayerIfNecessary(HttpServletRequest request, boolean jukebox) {
        String username = request.getRemoteUser();
        String clientId = request.getParameter("c");
        if (jukebox) {
            clientId += "-jukebox";
        }

        List<Player> players = playerService.getPlayersForUserAndClientId(username, clientId);

        // If not found, create it.
        if (players.isEmpty()) {
            Player player = new Player();
            player.setIpAddress(request.getRemoteAddr());
            player.setUsername(username);
            player.setClientId(clientId);
            player.setName(clientId);
            player.setTechnology(jukebox ? PlayerTechnology.JUKEBOX : PlayerTechnology.EXTERNAL_WITH_PLAYLIST);
            playerService.createPlayer(player);
            players = playerService.getPlayersForUserAndClientId(username, clientId);
        }

        // Return the player ID.
        return !players.isEmpty() ? players.get(0).getId() : null;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setTranscodingService(TranscodingService transcodingService) {
        this.transcodingService = transcodingService;
    }

    public void setDownloadController(DownloadController downloadController) {
        this.downloadController = downloadController;
    }

    public void setCoverArtController(CoverArtController coverArtController) {
        this.coverArtController = coverArtController;
    }

    public void setUserSettingsController(UserSettingsController userSettingsController) {
        this.userSettingsController = userSettingsController;
    }

    public void setLeftController(LeftController leftController) {
        this.leftController = leftController;
    }

    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public void setStreamController(StreamController streamController) {
        this.streamController = streamController;
    }

    public void setHlsController(HLSController hlsController) {
        this.hlsController = hlsController;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    public void setLyricsService(LyricsService lyricsService) {
        this.lyricsService = lyricsService;
    }

    public void setPlayQueueService(PlayQueueService playQueueService) {
        this.playQueueService = playQueueService;
    }

    public void setJukeboxService(JukeboxService jukeboxService) {
        this.jukeboxService = jukeboxService;
    }

    public void setAudioScrobblerService(AudioScrobblerService audioScrobblerService) {
        this.audioScrobblerService = audioScrobblerService;
    }

    public void setPodcastService(PodcastService podcastService) {
        this.podcastService = podcastService;
    }

    public void setRatingService(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setShareService(ShareService shareService) {
        this.shareService = shareService;
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    public void setAvatarController(AvatarController avatarController) {
        this.avatarController = avatarController;
    }

    public void setArtistDao(ArtistDao artistDao) {
        this.artistDao = artistDao;
    }

    public void setAlbumDao(AlbumDao albumDao) {
        this.albumDao = albumDao;
    }

    public void setMediaFileDao(MediaFileDao mediaFileDao) {
        this.mediaFileDao = mediaFileDao;
    }

    public void setMusicIndexService(MusicIndexService musicIndexService) {
        this.musicIndexService = musicIndexService;
    }

    public void setBookmarkDao(BookmarkDao bookmarkDao) {
        this.bookmarkDao = bookmarkDao;
    }

    public static enum ErrorCode {

        GENERIC(0, "A generic error."),
        MISSING_PARAMETER(10, "Required parameter is missing."),
        PROTOCOL_MISMATCH_CLIENT_TOO_OLD(20, "Incompatible Subsonic REST protocol version. Client must upgrade."),
        PROTOCOL_MISMATCH_SERVER_TOO_OLD(30, "Incompatible Subsonic REST protocol version. Server must upgrade."),
        NOT_AUTHENTICATED(40, "Wrong username or password."),
        NOT_AUTHORIZED(50, "User is not authorized for the given operation."),
        NOT_LICENSED(60, "The trial period for the Subsonic server is over. Please upgrade to Subsonic Premium. Visit subsonic.org for details."),
        NOT_FOUND(70, "Requested data was not found.");

        private final int code;
        private final String message;

        ErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class BookmarkKey extends Pair<String, Integer> {
        private BookmarkKey(String username, int mediaFileId) {
            super(username, mediaFileId);
        }

        static BookmarkKey forBookmark(Bookmark b) {
            return new BookmarkKey(b.getUsername(), b.getMediaFileId());
        }
    }
}
