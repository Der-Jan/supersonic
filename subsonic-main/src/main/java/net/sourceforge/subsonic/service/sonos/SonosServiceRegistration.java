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

package net.sourceforge.subsonic.service.sonos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;

import net.sourceforge.subsonic.util.Pair;
import net.sourceforge.subsonic.util.StringUtil;
import net.sourceforge.subsonic.util.Util;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class SonosServiceRegistration {

    private void register() throws IOException {
        String localUrl = String.format("http://%s:4040/ws/Sonos", Util.getLocalIpAddress());
//        String localUrl = String.format("http://%s:8080/smapi-1.0/ws/Sonos", Util.getLocalIpAddress());
        String controllerIp = "192.168.10.186";

        System.out.println("Using local URL     " + localUrl);
        System.out.println("Using controller IP " + controllerIp);

        String controllerUrl = String.format("http://%s:1400/customsd", controllerIp);
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(Pair.create("sid", "255"));
        params.add(Pair.create("name", "Subsonic"));
        params.add(Pair.create("uri", localUrl));
        params.add(Pair.create("secureUri", localUrl));
        params.add(Pair.create("pollInterval", "1200"));
        params.add(Pair.create("authType", "UserId"));
        params.add(Pair.create("containerType", "MService"));
        params.add(Pair.create("caps", "search"));
        params.add(Pair.create("caps", "trFavorites"));
        params.add(Pair.create("caps", "alFavorites"));
        params.add(Pair.create("presentationMapVersion", "0"));
        params.add(Pair.create("presentationMapUri", String.format("http://%s:4040/sonos/presentationMap.xml", Util.getLocalIpAddress())));
//        params.put("stringsVersion", "0");
//        params.put("stringsUri", "http://192.168.10.140:8080/smapi-1.0/static/config/strings.xml");
//        params.put("presentationMapVersion", "0");
//        params.put("presentationMapUri", "http://192.168.10.140:8080/smapi-1.0/PresentationMap.xml");
//        params.put("caps", "extendedMD");

        String result = execute(controllerUrl, params);
        System.out.println(result);
    }

    private String execute(String url, List<Pair<String, String>> parameters) throws IOException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Pair<String, String> parameter : parameters) {
            params.add(new BasicNameValuePair(parameter.getFirst(), parameter.getSecond()));
        }

        HttpPost request = new HttpPost(url);
        request.setEntity(new UrlEncodedFormEntity(params, StringUtil.ENCODING_UTF8));

        return executeRequest(request);
    }

    private String executeRequest(HttpUriRequest request) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 15000);
        HttpConnectionParams.setSoTimeout(client.getParams(), 15000);

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            return client.execute(request, responseHandler);

        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    public static void main(String[] args) throws IOException {
        new SonosServiceRegistration().register();
    }
}
