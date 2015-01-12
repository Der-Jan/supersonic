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

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import org.apache.commons.lang.RandomStringUtils;

import com.sonos.services._1.AbstractMedia;
import com.sonos.services._1.AddToContainerResult;
import com.sonos.services._1.ContentKey;
import com.sonos.services._1.CreateContainerResult;
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

import net.sourceforge.subsonic.service.sonos.SonosHelper;
import net.sourceforge.subsonic.util.Util;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class SonosService implements SonosSoap {

    public static final String ID_ROOT = "root";
    public static final String ID_PLAYLISTS = "playlists";
    public static final String ID_BROWSE = "browse";
    public static final String ID_PLAYLIST_PREFIX = "pl-";

    private SonosHelper sonosHelper;

    @Override
    public LastUpdate getLastUpdate() throws CustomFault {
        LastUpdate result = new LastUpdate();
        result.setCatalog(RandomStringUtils.random(5));
        result.setFavorites(RandomStringUtils.random(5));
        return result;
    }

    @Override
    public GetMetadataResponse getMetadata(GetMetadata parameters) throws CustomFault {

        String id = parameters.getId();
        System.out.println("getMetadata: " + id);
        MediaList mediaList = new MediaList();

        if (ID_ROOT.equals(id)) {
            sonosHelper.populateMediaListForRoot(mediaList);
        } else if (ID_PLAYLISTS.equals(id)) {
            populateMediaList(mediaList, parameters.getIndex(), parameters.getCount(),
                              sonosHelper.forPlaylists());
        } else if (id.startsWith(ID_PLAYLIST_PREFIX)) {
            int playlistId = Integer.parseInt(id.replace(ID_PLAYLIST_PREFIX, ""));
            populateMediaList(mediaList, parameters.getIndex(), parameters.getCount(),
                              sonosHelper.forPlaylist(playlistId));
        }


        GetMetadataResponse response = new GetMetadataResponse();
        response.setGetMetadataResult(mediaList);
        return response;
    }

    @Override
    public GetSessionIdResponse getSessionId(GetSessionId parameters) throws CustomFault {
        GetSessionIdResponse result = new GetSessionIdResponse();
        result.setGetSessionIdResult("void");
        return result;
    }

    public void setSonosHelper(SonosHelper sonosHelper) {
        this.sonosHelper = sonosHelper;
    }

    private void populateMediaList(MediaList mediaList, int index, int count, List<? extends AbstractMedia> mediaCollections) {
        List<? extends AbstractMedia> selectedMediaCollections = Util.subList(mediaCollections, index, count);
        mediaList.setIndex(index);
        mediaList.setCount(selectedMediaCollections.size());
        mediaList.setTotal(mediaCollections.size());
        mediaList.getMediaCollectionOrMediaMetadata().addAll(selectedMediaCollections);
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
    public void getMediaURI(String id, Holder<String> getMediaURIResult, Holder<HttpHeaders> httpHeaders, Holder<Integer> uriTimeout) throws CustomFault {

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
    public GetMediaMetadataResponse getMediaMetadata(GetMediaMetadata parameters) throws CustomFault {
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
}
