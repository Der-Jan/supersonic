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
  ~  Copyright 2014 (C) Sindre Mehus
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%--@elvariable id="model" type="java.util.Map"--%>

<html><head>
    <%@ include file="head.jsp" %>
</head><body class="mainframe bgcolor1">

<h1 style="padding-bottom: 1em">
    <span style="vertical-align: middle;">
        <c:forEach items="${model.ancestors}" var="ancestor">
            <sub:url value="main.view" var="ancestorUrl">
                <sub:param name="id" value="${ancestor.id}"/>
            </sub:url>
            <a href="${ancestorUrl}">${ancestor.name}</a> &raquo;
        </c:forEach>
        ${model.dir.name}
    </span>
</h1>

<c:forEach items="${model.children}" var="child">
    <c:if test="${child.video}">
        <div style="float:left;padding-right:14px; padding-bottom: 10px;width:213px">
            <sub:url value="/videoPlayer.view" var="videoUrl">
                <sub:param name="id" value="${child.id}"/>
            </sub:url>
            <sub:url value="/coverArt.view" var="coverArtUrl">
                <sub:param name="id" value="${child.id}"/>
                <sub:param name="size" value="120"/>
            </sub:url>
            <div style="padding-bottom: 3px">
                <a href="${videoUrl}" title="${child.name}"><img src="${coverArtUrl}" alt="${child.name}"></a>
            </div>
            <div class="detail">
                <div style="float:left;overflow:hidden;text-overflow: ellipsis;width:165px"><b>${child.name}</b></div>
                <div style="float: right">${child.durationString}</div>
            </div>
        </div>
    </c:if>
</c:forEach>

<div style="clear:both;padding-top: 1em">
    <c:set var="cssClass" value=""/>
    <c:forEach items="${model.children}" var="child" varStatus="loopStatus">
        <c:if test="${child.directory}">
            <c:choose>
                <c:when test="${empty cssClass}">
                    <c:set var="cssClass" value="class='bgcolor2'"/>
                </c:when>
                <c:otherwise>
                    <c:set var="cssClass" value=""/>
                </c:otherwise>
            </c:choose>
            <sub:url value="main.view" var="childUrl">
                <sub:param name="id" value="${child.id}"/>
            </sub:url>

            <div ${cssClass} style="width:213px;overflow: hidden;text-overflow: ellipsis">
                <a href="${childUrl}" title="${child.name}"><span style="white-space:nowrap;">${child.name}</span></a>
            </div>
        </c:if>
    </c:forEach>
</div>

</body>
</html>
