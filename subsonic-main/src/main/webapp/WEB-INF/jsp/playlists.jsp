<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<%--@elvariable id="model" type="java.util.Map"--%>

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>

    <style type="text/css">
        .playlistContainer {
            width: 200px;
            float: left;
            padding-right: 24px;
            padding-bottom: 20px;
        }
        .title, .duration {
            width:200px;
            overflow: hidden;
            text-overflow: ellipsis;
            padding-top: 3px;
        }
    </style>

</head><body class="mainframe bgcolor1">

<h1 style="padding-bottom: 1em">
    <img src="<spring:theme code="playlistImage"/>" alt="">
    <span style="vertical-align: middle"><fmt:message key="left.playlists"/></span>
</h1>

<c:forEach items="${model.playlists}" var="playlist" varStatus="loopStatus">

    <div class="playlistContainer">
        <div style="position:relative">
            <div>
                <c:import url="coverArt.jsp">
                    <c:param name="playlistId" value="${playlist.id}"/>
                    <c:param name="coverArtSize" value="200"/>
                    <c:param name="showLink" value="true"/>
                    <c:param name="appearAfter" value="${loopStatus.count * 30}"/>
                </c:import>
                <div class="detail title" title="${playlist.name}"><b>${playlist.name}</b></div>
            </div>
        </div>
        <div class="detail duration">${playlist.fileCount} <fmt:message key="playlist2.songs"/> &ndash; ${playlist.durationAsString}</div>
    </div>

</c:forEach>

</body>
</html>
