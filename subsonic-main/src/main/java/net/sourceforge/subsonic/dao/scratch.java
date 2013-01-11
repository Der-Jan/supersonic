package net.sourceforge.subsonic.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
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
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.common.util.MimeType;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class scratch {

    public static void main(String[] args) throws Exception {
        new scratch();
        Thread.sleep(Long.MAX_VALUE);
    }

    public scratch() throws Exception {

        UpnpService upnpService = new UpnpServiceImpl();

        upnpService.getRegistry().addDevice(createDevice());
        LocalService<ConnectionManagerService> service = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
        service.setManager(new DefaultServiceManager<ConnectionManagerService>(service, ConnectionManagerService.class));

    }

    private LocalDevice createDevice() throws Exception {

        DeviceIdentity identity =
                new DeviceIdentity(
                        UDN.uniqueSystemIdentifier("Demo Binary Light")
                );

        DeviceType type =
                new UDADeviceType("BinaryLight", 1);

        DeviceDetails details =
                new DeviceDetails(
                        "Friendly Binary Light",
                        new ManufacturerDetails("ACME"),
                        new ModelDetails(
                                "BinLight2000",
                                "A demo light with on/off switch.",
                                "v1"
                        )
                );

//        Icon icon =
//                new Icon(
//                        "image/png", 48, 48, 8,
//                        getClass().getResource("icon.png")
//                );

        LocalService<MP3ContentDirectory> switchPowerService = new AnnotationLocalServiceBinder().read(MP3ContentDirectory.class);
        switchPowerService.setManager(new DefaultServiceManager(switchPowerService, MP3ContentDirectory.class));

        return new LocalDevice(identity, type, details, new Icon[0], switchPowerService);
    }

    public static class MP3ContentDirectory extends AbstractContentDirectoryService {

        @Override
        public BrowseResult browse(String objectID, BrowseFlag browseFlag,
                String filter,
                long firstResult, long maxResults,
                SortCriterion[] orderby) throws ContentDirectoryException {
            try {

                // This is just an example... you have to create the DIDL content dynamically!

                DIDLContent didl = new DIDLContent();

                String album = ("Black Gives Way To Blue");
                String creator = "Alice In Chains"; // Required
                PersonWithRole artist = new PersonWithRole(creator, "Performer");
                MimeType mimeType = new MimeType("audio", "mpeg");

                didl.addItem(new MusicTrack(
                        "101", "3", // 101 is the Item ID, 3 is the parent Container ID
                        "All Secrets Known",
                        creator, album, artist,
                        new Res(mimeType, 123456l, "00:03:25", 8192l, "http://10.0.0.1/files/101.mp3")
                ));

                didl.addItem(new MusicTrack(
                        "102", "3",
                        "Check My Brain",
                        creator, album, artist,
                        new Res(mimeType, 2222222l, "00:04:11", 8192l, "http://10.0.0.1/files/102.mp3")
                ));

                // Create more tracks...

                // Count and total matches is 2
                return new BrowseResult(new DIDLParser().generate(didl), 2, 2);

            } catch (Exception ex) {
                throw new ContentDirectoryException(
                        ContentDirectoryErrorCode.CANNOT_PROCESS,
                        ex.toString()
                );
            }
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
