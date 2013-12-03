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

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.domain.Version;
import net.sourceforge.subsonic.service.upnp.ApacheUpnpServiceConfiguration;
import net.sourceforge.subsonic.service.upnp.FolderBasedContentDirectory;
import net.sourceforge.subsonic.service.upnp.MSMediaReceiverRegistrarService;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class UPnPService {

    private static final Logger LOG = Logger.getLogger(UPnPService.class);

    private SettingsService settingsService;
    private VersionService versionService;
    private UpnpService upnpService;
    private FolderBasedContentDirectory folderBasedContentDirectory;

    public void init() {
        startService();
    }

    public void startService() {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    LOG.info("Starting UPnP service...");
                    createService();
                    LOG.info("Starting UPnP service - Done!");
                } catch (Throwable x) {
                    LOG.error("Failed to start UPnP service: " + x, x);
                }
            }
        };
        new Thread(runnable).start();
    }

    private synchronized void createService() throws Exception {
        upnpService = new UpnpServiceImpl(new ApacheUpnpServiceConfiguration());

        // Asynch search for other devices (most importantly UPnP-enabled routers for port-mapping)
        upnpService.getControlPoint().search();

        // Start DLNA media server?
        setMediaServerEnabled(settingsService.isDlnaEnabled());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("Shutting down UPnP service...");
                upnpService.shutdown();
                System.err.println("Shutting down UPnP service - Done!");
            }
        });
    }

    public void setMediaServerEnabled(boolean enabled) {
        if (enabled) {
            try {
                upnpService.getRegistry().addDevice(createMediaServerDevice());
                LOG.info("Enabling UPnP/DLNA media server");
            } catch (Exception x) {
                LOG.error("Failed to start UPnP/DLNA media server: " + x, x);
            }
        } else {
            upnpService.getRegistry().removeAllLocalDevices();
            LOG.info("Disabling UPnP/DLNA media server");
        }
    }

    private LocalDevice createMediaServerDevice() throws Exception {

        DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("Subsonic"));
        DeviceType type = new UDADeviceType("MediaServer", 1);

        // TODO: DLNADoc, DLNACaps
        Version version = versionService.getLocalVersion();
        String versionString = version == null ? null : version.toString();
        String licenseEmail = settingsService.getLicenseEmail();
        String licenseString = licenseEmail == null ? "Unlicensed" : ("Licensed to " + licenseEmail);

        DeviceDetails details = new DeviceDetails("Subsonic Media Streamer", new ManufacturerDetails("Subsonic"),
                new ModelDetails("Subsonic", licenseString, versionString),
                new DLNADoc[]{new DLNADoc("DMS", DLNADoc.Version.V1_5)}, null);

        // TODO: add smaller icon as well
        Icon icon = new Icon("image/png", 512, 512, 32, getClass().getResource("subsonic-512.png"));

        LocalService<FolderBasedContentDirectory> contentDirectoryservice = new AnnotationLocalServiceBinder().read(FolderBasedContentDirectory.class);
        contentDirectoryservice.setManager(new DefaultServiceManager<FolderBasedContentDirectory>(contentDirectoryservice) {

            @Override
            protected FolderBasedContentDirectory createServiceInstance() throws Exception {
                return folderBasedContentDirectory;
            }
        });


        // TODO: This is returned from Windows Media Player
        // "http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L4_SO_G726,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_PRO,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAFULL,http-get:*:video/x-ms-asf:DLNA.ORG_PN=VC1_ASF_AP_L1_WMA,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM,http-get:*:audio/L16;rate=44100;channels=2:DLNA.ORG_PN=LPCM,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_BASE,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_MP3,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMABASE,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_FULL,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL_XAC3,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_MED,http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_SP_G726,http-get:*:audio/L16;rate=48000;channels=2:DLNA.ORG_PN=LPCM,http-get:*:audio/mpeg:DLNA.ORG_PN=MP3,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_PRO,http-get:*:audio/mpeg:DLNA.ORG_PN=MP3X,http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L5_SO_G726,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPLL_BASE,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG1,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_BASE,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_FULL,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAPRO,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_LRG,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC_XAC3,http-get:*:audio/L16;rate=44100;channels=1:DLNA.ORG_PN=LPCM,http-get:*:image/x-ycbcr-yuv420:*,http-get:*:video/x-ms-wmv:*,http-get:*:audio/x-ms-wma:*,http-get:*:video/wtv:*,rtsp-rtp-udp:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_SP_G726,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_PRO,rtsp-rtp-udp:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L5_SO_G726,rtsp-rtp-udp:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L4_SO_G726,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPLL_BASE,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_BASE,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_PRO,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_FULL,rtsp-rtp-udp:*:video/x-ms-asf:DLNA.ORG_PN=VC1_ASF_AP_L1_WMA,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_BASE,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_FULL,rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_MP3,rtsp-rtp-udp:*:video/x-ms-wmv:*";

        // TODO: Provide protocol info
        final ProtocolInfos sourceProtocols = new ProtocolInfos(
                new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "audio/mpeg", "DLNA.ORG_PN=MP3;DLNA.ORG_OP=01"),
                new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/mpeg", "DLNA.ORG_PN=MPEG1;DLNA.ORG_OP=01;DLNA.ORG_CI=0"));

        LocalService<ConnectionManagerService> connetionManagerService = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
        connetionManagerService.setManager(new DefaultServiceManager<ConnectionManagerService>(connetionManagerService) {
            @Override
            protected ConnectionManagerService createServiceInstance() throws Exception {
                return new ConnectionManagerService(sourceProtocols, null);
            }
        });

        // For compatibility with Microsoft
        LocalService<MSMediaReceiverRegistrarService> receiverService = new AnnotationLocalServiceBinder().read(MSMediaReceiverRegistrarService.class);
        receiverService.setManager(new DefaultServiceManager<MSMediaReceiverRegistrarService>(receiverService, MSMediaReceiverRegistrarService.class));

        return new LocalDevice(identity, type, details, new Icon[]{icon}, new LocalService[]{contentDirectoryservice, connetionManagerService, receiverService});
    }


    public UpnpService getUpnpService() {
        return upnpService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    public void setFolderBasedContentDirectory(FolderBasedContentDirectory folderBasedContentDirectory) {
        this.folderBasedContentDirectory = folderBasedContentDirectory;
    }
}
