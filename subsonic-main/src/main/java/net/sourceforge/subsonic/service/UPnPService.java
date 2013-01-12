package net.sourceforge.subsonic.service;

import net.sourceforge.subsonic.dao.AlbumDao;
import net.sourceforge.subsonic.dao.ArtistDao;
import net.sourceforge.subsonic.dao.MediaFileDao;
import net.sourceforge.subsonic.domain.Album;
import net.sourceforge.subsonic.domain.Artist;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.MediaLibraryStatistics;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.support.connectionmanager.ConnectionManagerService;
import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.teleal.cling.support.contentdirectory.ContentDirectoryException;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.BrowseResult;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.PersonWithRole;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.container.MusicAlbum;
import org.teleal.cling.support.model.container.MusicArtist;
import org.teleal.cling.support.model.container.StorageFolder;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.common.util.MimeType;

import java.util.Arrays;
import java.util.List;

import static net.sourceforge.subsonic.controller.CoverArtController.ALBUM_COVERART_PREFIX;
import static net.sourceforge.subsonic.controller.CoverArtController.ARTIST_COVERART_PREFIX;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class UPnPService {

    private SettingsService settingsService;
    private MediaFileDao mediaFileDao;
    private ArtistDao artistDao;
    private AlbumDao albumDao;

//    public static void main(String[] args) throws Exception {
//        new UPnPService();
//        Thread.sleep(Long.MAX_VALUE);
//    }

    public UPnPService() throws Exception {

        // TODO: Shutdown hook
        // TODO: Handle exception.
        final UpnpService upnpService = new UpnpServiceImpl();

        upnpService.getRegistry().addDevice(createDevice());
        LocalService<ConnectionManagerService> service = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
        service.setManager(new DefaultServiceManager<ConnectionManagerService>(service, ConnectionManagerService.class));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("Shutting down UPnP.");
                upnpService.shutdown();
                System.err.println("Shutting down UPnP - Done!");
            }
        });
    }

    private LocalDevice createDevice() throws Exception {

        DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("Subsonic"));
        DeviceType type = new UDADeviceType("MediaServer", 1);

        // TODO: Version and license info
        // TODO: DLNADoc, DLNACaps
        DeviceDetails details = new DeviceDetails("Subsonic Media Streamer", new ManufacturerDetails("Subsonic"),
                new ModelDetails("Subsonic", "Licensed to sindre@activeobjects.no ", "4.9"));
        // TODO: icon
//        Icon icon = new Icon("image/png", 48, 48, 8, getClass().getResource("icon.png"));

        LocalService<ContentDirectory> contentDirectoryservice = new AnnotationLocalServiceBinder().read(ContentDirectory.class);
        contentDirectoryservice.setManager(new DefaultServiceManager<ContentDirectory>(contentDirectoryservice) {
            @Override
            protected ContentDirectory createServiceInstance() throws Exception {
                return new ContentDirectory();
            }
        });

        // TODO: Provide protocol info
        LocalService<ConnectionManagerService> connetionManagerService = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
        connetionManagerService.setManager(new DefaultServiceManager<ConnectionManagerService>(connetionManagerService, ConnectionManagerService.class));

        return new LocalDevice(identity, type, details, new Icon[0],
                new LocalService[] {contentDirectoryservice, connetionManagerService});
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
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


    private class ContentDirectory extends AbstractContentDirectoryService {

        public static final String ROOT_CONTAINER_ID = "0";

        public ContentDirectory() {
            // TODO: Specify sort & search capabilities?
            super();
        }

        @Override
        public BrowseResult browse(String objectId, BrowseFlag browseFlag, String filter, long firstResult,
                                   long maxResults, SortCriterion[] orderby) throws ContentDirectoryException {

            System.out.println("objectId    : " + objectId);
            System.out.println("browseFlag  : " + browseFlag);
            System.out.println("filter      : " + filter);
            System.out.println("firstResult : " + firstResult);
            System.out.println("maxResults  : " + maxResults);
            System.out.println("orderBy     :" + Arrays.toString(orderby));
            System.out.println();

            // maxResult == 0 means all.
            if (maxResults == 0) {
                maxResults = Integer.MAX_VALUE;
            }

            try {

                if (ROOT_CONTAINER_ID.equals(objectId)) {
                    return browseRoot(browseFlag, firstResult, maxResults);
                }

                if (objectId.startsWith(ARTIST_COVERART_PREFIX)) {
                    return browseArtist(objectId, browseFlag, firstResult, maxResults);
                }

                if (objectId.startsWith(ALBUM_COVERART_PREFIX)) {
                    return browseAlbum(objectId, browseFlag, firstResult, maxResults);
                }

                throw new Exception("Not implemented");
/*
                    String album = ("Black Gives Way To Blue");
                    String creator = "Alice In Chains"; // Required
                    PersonWithRole artist = new PersonWithRole(creator, "Performer");
                    MimeType mimeType = new MimeType("audio", "mpeg");
                    didl.addItem(new MusicTrack(
                            "101", ROOT_CONTAINER_ID, // 101 is the Item ID, 0 is the parent Container ID
                            "All Secrets Known",
                            creator, album, artist,
                            new Res(mimeType, 123456l, "00:03:25", 8192l, "http://10.0.0.1/files/101.mp3")
                    ));
*/

                // TODO: Container update ID.

            } catch (Exception ex) {
                // TODO: Use different error codes.
                throw new ContentDirectoryException(
                        ContentDirectoryErrorCode.CANNOT_PROCESS,
                        ex.toString()
                );
            }
        }

        private BrowseResult browseRoot(BrowseFlag browseFlag, long firstResult, long maxResults) throws Exception {
            return browseFlag == BrowseFlag.METADATA ? browseRootMetadata() : browseArtists(firstResult, maxResults);
        }

        private BrowseResult browseRootMetadata() throws Exception {
            StorageFolder root = new StorageFolder();
            root.setId(ROOT_CONTAINER_ID);
            root.setParentID("-1");

            MediaLibraryStatistics statistics = settingsService.getMediaLibraryStatistics();
            root.setStorageUsed(statistics == null ? 0 : statistics.getTotalLengthInBytes());
            root.setTitle("Subsonic Media");
            root.setRestricted(true); // TODO
            root.setSearchable(false); // TODO
            root.setWriteStatus(WriteStatus.NOT_WRITABLE); // TODO
            // TODO: Support videos
            root.setChildCount(artistDao.getAlphabetialArtists(0, Integer.MAX_VALUE).size());

            DIDLContent didl = new DIDLContent();
            didl.addContainer(root);
            return createBrowseResult(didl, 1, 1);
        }

        private BrowseResult browseArtists(long firstResult, long maxResults) throws Exception {
            DIDLContent didl = new DIDLContent();
            for (Artist artist : artistDao.getAlphabetialArtists((int) firstResult, (int) maxResults)) {
                didl.addContainer(createArtistContainer(artist));
            }
            int artistCount = artistDao.getAlphabetialArtists(0, Integer.MAX_VALUE).size();
            return createBrowseResult(didl, didl.getContainers().size(), artistCount);
        }

        private BrowseResult browseArtist(String objectId, BrowseFlag browseFlag, long firstResult, long maxResults) throws Exception {
            Artist artist = getArtistByObjectId(objectId);
            return browseFlag == BrowseFlag.METADATA ? browseArtistMetadata(artist) : browseAlbums(artist, firstResult, maxResults);
        }

        private BrowseResult browseArtistMetadata(Artist artist) throws Exception {
            DIDLContent didl = new DIDLContent();
            didl.addContainer(createArtistContainer(artist));
            return createBrowseResult(didl, 1, 1);
        }

        private BrowseResult browseAlbums(Artist artist, long firstResult, long maxResults) throws Exception {
            DIDLContent didl = new DIDLContent();
            List<Album> albums = albumDao.getAlbumsForArtist(artist.getName());
            for (int i = (int) firstResult; i < Math.min(albums.size(), firstResult + maxResults); i++) {
                didl.addContainer(createAlbumContainer(artist, albums.get(i)));
            }
            return createBrowseResult(didl, didl.getContainers().size(), albums.size());
        }

        private BrowseResult browseAlbum(String objectId, BrowseFlag browseFlag, long firstResult, long maxResults) throws Exception {
            Album album = getAlbumByObjectId(objectId);
            Artist artist = artistDao.getArtist(album.getArtist());
            return browseFlag == BrowseFlag.METADATA ? browseAlbumMetadata(artist, album) : browseSongs(album, firstResult, maxResults);
        }

        private BrowseResult browseAlbumMetadata(Artist artist, Album album) throws Exception {
            DIDLContent didl = new DIDLContent();
            didl.addContainer(createAlbumContainer(artist, album));
            return createBrowseResult(didl, 1, 1);
        }

        private BrowseResult browseSongs(Album album, long firstResult, long maxResults) throws Exception {
            DIDLContent didl = new DIDLContent();
            List<MediaFile> songs = mediaFileDao.getSongsForAlbum(album.getArtist(), album.getName());
            // TODO: Create util method for extracting sublist.
            for (int i = (int) firstResult; i < Math.min(songs.size(), firstResult + maxResults); i++) {
                didl.addItem(createSongItem(album, songs.get(i)));
            }
            return createBrowseResult(didl, didl.getItems().size(), songs.size());
        }

        private Container createArtistContainer(Artist artist) {
            MusicArtist container = new MusicArtist();
            container.setId(ARTIST_COVERART_PREFIX + artist.getId());
            container.setParentID(ROOT_CONTAINER_ID);
            container.setTitle(artist.getName());
            container.setChildCount(albumDao.getAlbumsForArtist(artist.getName()).size());
            return container;
        }

        private Container createAlbumContainer(Artist artist, Album album) {
            MusicAlbum container = new MusicAlbum();
            container.setId(ALBUM_COVERART_PREFIX + album.getId());
            container.setParentID(ARTIST_COVERART_PREFIX + artist.getId());
            container.setTitle(album.getName());
//            container.setAlbumArtURIs(); // TODO
            container.setArtists(new PersonWithRole[]{new PersonWithRole(artist.getName())});
//            container.setDate(); //TODO
            container.setDescription(album.getComment());
            container.setChildCount(album.getSongCount());
            return container;
        }

        private Item createSongItem(Album album, MediaFile song) {
            MusicTrack item = new MusicTrack();
            item.setId(String.valueOf(song.getId()));
            item.setParentID(ALBUM_COVERART_PREFIX + album.getId());
            item.setTitle(song.getTitle());
            item.setAlbum(song.getAlbumName());
            if (song.getArtist() != null ) {
                item.setArtists(new PersonWithRole[]{new PersonWithRole(song.getArtist())});
            }
//            item.setDate(); // TODO
            item.setOriginalTrackNumber(song.getTrackNumber());
            if (song.getGenre() != null) {
                item.setGenres(new String[]{song.getGenre()});
            }
            item.setResources(Arrays.asList(createResourceForsong(song)));
            item.setDescription(song.getComment());
            return item;
        }

        private Res createResourceForsong(MediaFile song) {
            // TODO
            MimeType mimeType = new MimeType("audio", "mpeg");
            return new Res(mimeType, 123456L, "00:03:25", 8192L, "http://10.0.0.1/files/101.mp3");
        }

        private Artist getArtistByObjectId(String objectId) {
            return artistDao.getArtist(Integer.parseInt(objectId.replace(ARTIST_COVERART_PREFIX, "")));
        }

        private Album getAlbumByObjectId(String objectId) {
            return albumDao.getAlbum(Integer.parseInt(objectId.replace(ALBUM_COVERART_PREFIX, "")));
        }

        private BrowseResult createBrowseResult(DIDLContent didl, int count, int totalMatches) throws Exception {
            return new BrowseResult(new DIDLParser().generate(didl), count, totalMatches);
        }

        @Override
        public BrowseResult search(String containerId,
                String searchCriteria, String filter,
                long firstResult, long maxResults,
                SortCriterion[] orderBy) throws ContentDirectoryException {
            // You can override this method to implement searching!
            return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
        }
    }
}
