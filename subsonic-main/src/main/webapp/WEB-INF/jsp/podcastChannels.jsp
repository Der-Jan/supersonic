<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<%--
  ~ This file is part of Subsonic.
  ~
  ~  Subsonic is free software: you can redistribute it and/or modify
  ~  it under the terms of the GNU General Public License as published by
  ~  the Free Software Foundation, either version 3 of the License, or
  ~  (at your option) any later version.
  ~
  ~  Subsonic is distributed in the hope that it will be useful,
  ~  but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~  GNU General Public License for more details.
  ~
  ~  You should have received a copy of the GNU General Public License
  ~  along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.
  ~
  ~  Copyright 2015 (C) Sindre Mehus
  --%>

<%--@elvariable id="model" type="java.util.Map"--%>

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
</head><body class="mainframe bgcolor1">

<h1 style="padding-bottom: 1em">
    <img src="<spring:theme code="podcastLargeImage"/>" alt="">
    <span style="vertical-align: middle"><fmt:message key="podcastreceiver.title"/></span>
</h1>

<c:if test="${empty model.channels}">
    <p><em><fmt:message key="podcastreceiver.empty"/></em></p>
</c:if>

<c:forEach items="${model.channels}" var="channel" varStatus="loopStatus">

    <c:set var="caption2">
        <fmt:message key="podcastreceiver.episodes"><fmt:param value="${fn:length(channel.value)}"/></fmt:message>
    </c:set>
    <div class="albumThumb">
        <c:import url="coverArt.jsp">
            <c:param name="podcastChannelId" value="${channel.key.id}"/>
            <c:param name="coverArtSize" value="200"/>
            <c:param name="caption1" value="${fn:escapeXml(channel.key.title)}"/>
            <c:param name="caption2" value="${caption2}"/>
            <c:param name="captionCount" value="2"/>
            <c:param name="showLink" value="true"/>
            <c:param name="appearAfter" value="${loopStatus.count * 30}"/>
        </c:import>
    </div>

</c:forEach>

<table style="padding-top:1em"><tr>
    <c:if test="${model.user.podcastRole}">
        <td style="padding-right:2em"><div class="forward"><a href="podcastReceiverAdmin.view?refresh"><fmt:message key="podcastreceiver.check"/></a></div></td>
    </c:if>
    <c:if test="${model.user.adminRole}">
        <td style="padding-right:2em"><div class="forward"><a href="podcastSettings.view?"><fmt:message key="podcastreceiver.settings"/></a></div></td>
    </c:if>
</tr></table>

<c:if test="${model.user.podcastRole}">
    <form method="post" action="podcastReceiverAdmin.view?">
        <table>
            <tr>
                <td><fmt:message key="podcastreceiver.subscribe"/></td>
                <td><input type="text" name="add" value="http://" style="width:30em" onclick="select()"/></td>
                <td><input type="submit" value="<fmt:message key="common.ok"/>"/></td>
            </tr>
        </table>
    </form>
</c:if>

<c:set var="licenseInfo" value="${model.licenseInfo}"/>
<%@ include file="licenseNotice.jsp" %>

</body>
</html>
