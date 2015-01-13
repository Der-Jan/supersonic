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

import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.w3c.dom.Node;

import com.sonos.services._1.AbstractMedia;
import com.sonos.services._1.AddToContainerResult;
import com.sonos.services._1.ContentKey;
import com.sonos.services._1.CreateContainerResult;
import com.sonos.services._1.Credentials;
import com.sonos.services._1.DeleteContainerResult;
import com.sonos.services._1.DeviceAuthTokenResult;
import com.sonos.services._1.DeviceLinkCodeResult;
import com.sonos.services._1.GetExtendedMetadata;
import com.sonos.services._1.GetExtendedMetadataResponse;
import com.sonos.services._1.GetExtendedMetadataText;
import com.sonos.services._1.GetExtendedMetadataTextResponse;
import com.sonos.services._1.GetMediaMetadata;
import com.sonos.services._1.GetMediaMetadataResponse;
import com.sonos.services._1.GetMetadata;
import com.sonos.services._1.GetMetadataResponse;
import com.sonos.services._1.GetSessionId;
import com.sonos.services._1.GetSessionIdResponse;
import com.sonos.services._1.HttpHeaders;
import com.sonos.services._1.LastUpdate;
import com.sonos.services._1.MediaList;
import com.sonos.services._1.RateItem;
import com.sonos.services._1.RateItemResponse;
import com.sonos.services._1.RemoveFromContainerResult;
import com.sonos.services._1.RenameContainerResult;
import com.sonos.services._1.ReorderContainerResult;
import com.sonos.services._1.ReportPlaySecondsResult;
import com.sonos.services._1.Search;
import com.sonos.services._1.SearchResponse;
import com.sonos.services._1.SegmentMetadataList;
import com.sonos.services._1_1.CustomFault;
import com.sonos.services._1_1.SonosSoap;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.User;
import net.sourceforge.subsonic.service.sonos.SonosHelper;
import net.sourceforge.subsonic.util.Util;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class SonosService implements SonosSoap {

    private static final Logger LOG = Logger.getLogger(SonosService.class);

    public static final String ID_ROOT = "root";
    public static final String ID_PLAYLISTS = "playlists";
    public static final String ID_LIBRARY = "library";
    public static final String ID_STARRED = "starred";
    public static final String ID_STARRED_ARTISTS = "starred-artists";
    public static final String ID_STARRED_ALBUMS = "starred-albums";
    public static final String ID_STARRED_SONGS = "starred-songs";
    public static final String ID_PLAYLIST_PREFIX = "pl-";

    private SonosHelper sonosHelper;
    private MediaFileService mediaFileService;
    private SecurityService securityService;

    /**
     * The context for the request. This is used to get the Auth information
     * form the headers as well as using the request url to build the correct
     * media resource url.
     */
    @Resource
    private WebServiceContext context;

    @Override
    public LastUpdate getLastUpdate() throws CustomFault {
        LastUpdate result = new LastUpdate();
        result.setCatalog(RandomStringUtils.randomAscii(5));
        result.setFavorites(RandomStringUtils.randomAscii(5));
        return result;
    }

    @Override
    public GetMetadataResponse getMetadata(GetMetadata parameters) throws CustomFault {
        String id = parameters.getId();
        System.out.printf("getMetadata: id=%s index=%s count=%s recursive=%s\n",
                          id, parameters.getIndex(), parameters.getCount(), parameters.isRecursive());

        List<? extends AbstractMedia> mediaList = null;
        if (ID_ROOT.equals(id)) {
            mediaList = sonosHelper.forRoot();
        } else if (ID_LIBRARY.equals(id)) {
            mediaList = sonosHelper.forLibrary();
        } else if (ID_PLAYLISTS.equals(id)) {
            mediaList = sonosHelper.forPlaylists(getUsername());
        } else if (ID_STARRED.equals(id)) {
            mediaList = sonosHelper.forStarred();
        } else if (ID_STARRED_ARTISTS.equals(id)) {
            mediaList = sonosHelper.forStarredArtists(getUsername());
        } else if (ID_STARRED_ALBUMS.equals(id)) {
            mediaList = sonosHelper.forStarredAlbums(getUsername());
        } else if (ID_STARRED_SONGS.equals(id)) {
            mediaList = sonosHelper.forStarredSongs(getUsername());
        } else if (id.startsWith(ID_PLAYLIST_PREFIX)) {
            int playlistId = Integer.parseInt(id.replace(ID_PLAYLIST_PREFIX, ""));
            mediaList = sonosHelper.forPlaylist(playlistId);
        } else {
            mediaList = sonosHelper.forDirectoryContent(Integer.parseInt(id));
        }

        GetMetadataResponse response = new GetMetadataResponse();
        response.setGetMetadataResult(createSubList(parameters.getIndex(), parameters.getCount(), mediaList));
        return response;
    }

    @Override
    public GetSessionIdResponse getSessionId(GetSessionId parameters) throws CustomFault {
        System.out.println("getSessionId: " + parameters.getUsername());
        User user = securityService.getUserByName(parameters.getUsername());
        if (user == null || !StringUtils.equals(user.getPassword(), parameters.getPassword())) {
            throw new RuntimeException("Wrong username or password"); // TODO: Different exception?
        }

        // Use username as session ID for easy access to it later.
        GetSessionIdResponse result = new GetSessionIdResponse();
        result.setGetSessionIdResult(user.getUsername());
        return result;
    }

    @Override
    public GetMediaMetadataResponse getMediaMetadata(GetMediaMetadata parameters) throws CustomFault {
        System.out.println("getMediaMetadata: " + parameters.getId());

        int id = Integer.parseInt(parameters.getId());
        MediaFile song = mediaFileService.getMediaFile(id);

        GetMediaMetadataResponse response = new GetMediaMetadataResponse();
        GetMediaMetadataResponse.GetMediaMetadataResult result = new GetMediaMetadataResponse.GetMediaMetadataResult();
        result.setMediaMetadata(sonosHelper.forSong(song));
        response.setGetMediaMetadataResult(result);

        return response;
    }

    @Override
    public void getMediaURI(String id, Holder<String> getMediaURIResult, Holder<HttpHeaders> httpHeaders, Holder<Integer> uriTimeout) throws CustomFault {
        System.out.println("getMediaURI " + id); // TODO
        getMediaURIResult.value = sonosHelper.getMediaURI(Integer.parseInt(id));
    }

    private MediaList createSubList(int index, int count, List<? extends AbstractMedia> mediaCollections) {
        MediaList result = new MediaList();
        List<? extends AbstractMedia> selectedMediaCollections = Util.subList(mediaCollections, index, count);

        result.setIndex(index);
        result.setCount(selectedMediaCollections.size());
        result.setTotal(mediaCollections.size());
        result.getMediaCollectionOrMediaMetadata().addAll(selectedMediaCollections);

        return result;
    }

    private String getUsername() {
        MessageContext messageContext = context.getMessageContext();
        if (messageContext == null || !(messageContext instanceof WrappedMessageContext)) {
            LOG.error("Message context is null or not an instance of WrappedMessageContext.");
            return null;
        }

        Message message = ((WrappedMessageContext) messageContext).getWrappedMessage();
        List<Header> headers = CastUtils.cast((List<?>) message.get(Header.HEADER_LIST));
        if (headers != null) {
            for (Header h : headers) {
                Object o = h.getObject();
                // Unwrap the node using JAXB
                if (o instanceof Node) {
                    JAXBContext jaxbContext;
                    try {
                        // TODO: Check performance
                        jaxbContext = new JAXBDataBinding(Credentials.class).getContext();
                        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                        o = unmarshaller.unmarshal((Node) o);
                    } catch (JAXBException e) {
                        // failed to get the credentials object from the headers
                        LOG.error("JAXB error trying to unwrap credentials", e);
                    }
                }
                if (o instanceof Credentials) {
                    Credentials c = (Credentials) o;

                    // Note: We're using the username as session ID.
                    String username = c.getSessionId();
                    if (username == null) {
                        LOG.debug("No session id in credentials object, get from login");
                        username = c.getLogin().getUsername();
                    }
                    return username;
                } else {
                    LOG.error("No credentials object");
                }
            }
        } else {
            LOG.error("No headers found");
        }
        return null;
    }

    public void setSonosHelper(SonosHelper sonosHelper) {
        this.sonosHelper = sonosHelper;
    }

    @Override
    public RateItemResponse rateItem(RateItem parameters) throws CustomFault {
        return null;
    }

    @Override
    public CreateContainerResult createContainer(String containerType, String title, String parentId, String seedId) throws CustomFault {
        return null;
    }

    @Override
    public AddToContainerResult addToContainer(String id, String parentId, int index, String updateId) throws CustomFault {
        return null;
    }

    @Override
    public RenameContainerResult renameContainer(String id, String title) throws CustomFault {
        return null;
    }

    @Override
    public SegmentMetadataList getStreamingMetadata(String id, XMLGregorianCalendar startTime, int duration) throws CustomFault {
        return null;
    }

    @Override
    public ReorderContainerResult reorderContainer(String id, String from, int to, String updateId) throws CustomFault {
        return null;
    }

    @Override
    public GetExtendedMetadataTextResponse getExtendedMetadataText(GetExtendedMetadataText parameters) throws CustomFault {
        return null;
    }

    @Override
    public DeviceLinkCodeResult getDeviceLinkCode(String householdId) throws CustomFault {
        return null;
    }

    @Override
    public void deleteItem(String favorite) throws CustomFault {

    }

    @Override
    public void reportAccountAction(String type) throws CustomFault {

    }

    @Override
    public void setPlayedSeconds(String id, int seconds) throws CustomFault {

    }

    @Override
    public ReportPlaySecondsResult reportPlaySeconds(String id, int seconds) throws CustomFault {
        return null;
    }

    @Override
    public DeviceAuthTokenResult getDeviceAuthToken(String householdId, String linkCode, String linkDeviceId) throws CustomFault {
        return null;
    }

    @Override
    public void reportStatus(String id, int errorCode, String message) throws CustomFault {

    }

    @Override
    public GetExtendedMetadataResponse getExtendedMetadata(GetExtendedMetadata parameters) throws CustomFault {
        return null;
    }

    @Override
    public String getScrollIndices(String id) throws CustomFault {
        return null;
    }

    @Override
    public DeleteContainerResult deleteContainer(String id) throws CustomFault {
        return null;
    }

    @Override
    public void reportPlayStatus(String id, String status) throws CustomFault {

    }

    @Override
    public ContentKey getContentKey(String id, String uri) throws CustomFault {
        return null;
    }

    @Override
    public SearchResponse search(Search parameters) throws CustomFault {
        return null;
    }

    @Override
    public RemoveFromContainerResult removeFromContainer(String id, String indices, String updateId) throws CustomFault {
        return null;
    }

    @Override
    public String createItem(String favorite) throws CustomFault {
        return null;
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}
