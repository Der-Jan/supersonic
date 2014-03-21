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

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.subsonic.restapi.ObjectFactory;
import org.subsonic.restapi.Response;
import org.subsonic.restapi.ResponseStatus;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.util.StringUtil;

import static org.springframework.web.bind.ServletRequestUtils.getStringParameter;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class JAXBWriter {

    private static final Logger LOG = Logger.getLogger(JAXBWriter.class);

    private final Marshaller xmlMarshaller;
    private final Marshaller jsonMarshaller;

    public JAXBWriter() {
        try {
            javax.xml.bind.JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);

            xmlMarshaller = jaxbContext.createMarshaller();
            xmlMarshaller.setProperty(Marshaller.JAXB_ENCODING, StringUtil.ENCODING_UTF8);
            xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jsonMarshaller = jaxbContext.createMarshaller();
            jsonMarshaller.setProperty(Marshaller.JAXB_ENCODING, StringUtil.ENCODING_UTF8);
            jsonMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jsonMarshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
            jsonMarshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
        } catch (JAXBException x) {
            throw new RuntimeException(x);
        }
    }

    public Response createResponse(boolean ok) {
        Response response = new ObjectFactory().createResponse();
        response.setStatus(ok ? ResponseStatus.OK : ResponseStatus.FAILED);
        response.setVersion(StringUtil.getRESTProtocolVersion());
        return response;
    }

    public void writeResponse(HttpServletRequest request, HttpServletResponse httpResponse, Response jaxbResponse) throws Exception {

        String format = getStringParameter(request, "f", "xml");
        String jsonpCallback = request.getParameter("callback");
        boolean json = "json".equals(format);
        boolean jsonp = "jsonp".equals(format) && jsonpCallback != null;
        Marshaller marshaller;

        if (json) {
            marshaller = jsonMarshaller;
            httpResponse.setContentType("application/json");
        } else if (jsonp) {
            marshaller = jsonMarshaller;
            httpResponse.setContentType("text/javascript");
        } else {
            marshaller = xmlMarshaller;
            httpResponse.setContentType("text/xml");
        }

        httpResponse.setCharacterEncoding(StringUtil.ENCODING_UTF8);

        try {
            StringWriter writer = new StringWriter();
            if (jsonp) {
                writer.append(jsonpCallback).append('(');
            }
            marshaller.marshal(new ObjectFactory().createSubsonicResponse(jaxbResponse), writer);
            if (jsonp) {
                writer.append(");");
            }
            httpResponse.getWriter().append(writer.getBuffer());
        } catch (Exception x) {
            LOG.error("Failed to marshal JAXB", x);
            throw x;
        }
    }
}
