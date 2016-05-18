<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>
<%@ include file="include.jsp" %>

<%--
PARAMETERS
  albumId: ID of album.
  playlistId: ID of playlist.
  podcastChannelId: ID of podcast channel
  auth: Authentication token
  coverArtSize: Height and width of cover art.
  caption1: Caption line 1
  caption2: Caption line 2
  caption3: Caption line 3
  captionCount: Number of caption lines to display (default 0)
  showLink: Whether to make the cover art image link to the album page.
  showZoom: Whether to display a link for zooming the cover art.
  showChange: Whether to display a link for changing the cover art.
  appearAfter: Fade in after this many milliseconds, or nil if no fading in should happen.
--%>
<c:choose>
    <c:when test="${empty param.coverArtSize}">
        <c:set var="size" value="auto"/>
    </c:when>
    <c:otherwise>
        <c:set var="size" value="${param.coverArtSize}px"/>
    </c:otherwise>
</c:choose>

<c:set var="captionCount" value="${empty param.captionCount ? 0 : param.captionCount}"/>

<div class="coverart dropshadow hoverable"
     onmouseover="$(this).find('.coverart-play').show()"
     onmouseout="$(this).find('.coverart-play').hide()">

    <div style="width:${size};max-width:${size};height:${size};max-height:${size};cursor:pointer" title="${param.caption1}">

        <c:if test="${not empty param.albumId}">
            <c:url value="main.view" var="targetUrl">
                <c:param name="id" value="${param.albumId}"/>
            </c:url>
        </c:if>
        <c:if test="${not empty param.playlistId}">
            <c:url value="playlist.view" var="targetUrl">
                <c:param name="id" value="${param.playlistId}"/>
            </c:url>
        </c:if>
        <c:if test="${not empty param.podcastChannelId}">
            <c:url value="podcastChannel.view" var="targetUrl">
                <c:param name="id" value="${param.podcastChannelId}"/>
            </c:url>
        </c:if>

        <c:url value="/coverArt.view" var="coverArtUrl">
            <c:if test="${not empty param.coverArtSize}">
                <c:param name="size" value="${param.coverArtSize}"/>
            </c:if>
            <c:if test="${not empty param.albumId}">
                <c:param name="id" value="${param.albumId}"/>
            </c:if>
            <c:if test="${not empty param.podcastChannelId}">
                <c:param name="id" value="pod-${param.podcastChannelId}"/>
            </c:if>
            <c:if test="${not empty param.playlistId}">
                <c:param name="id" value="pl-${param.playlistId}"/>
            </c:if>
            <c:if test="${not empty param.auth}">
                <c:param name="auth" value="${param.auth}"/>
            </c:if>
        </c:url>

        <c:url value="/coverArt.view" var="zoomCoverArtUrl">
            <c:param name="id" value="${param.albumId}"/>
            <c:param name="auth" value="${param.auth}"/>
        </c:url>

        <div class="coverart-play" style="position:relative; width:0; height:0; display:none">
            <div onclick="<c:if test="${not empty param.albumId}">top.playQueue.onPlay(${param.albumId});</c:if>
            <c:if test="${not empty param.playlistId}">top.playQueue.onPlayPlaylist(${param.playlistId}, false);</c:if>
            <c:if test="${not empty param.podcastChannelId}">top.playQueue.onPlayPodcastChannel(${param.podcastChannelId}, false);</c:if>">
                <i class="material-icons" style="position:absolute; top: 8px; left: 8px; z-index: 2; font-size:36px; opacity:0.8">play_circle_filled</i>
                <i class="material-icons" style="position:absolute; top: 14px; left: 14px; z-index: 3; font-size:24px; color:white">play_arrow</i>
            </div>
            <div onclick="<c:if test="${not empty param.albumId}">top.playQueue.onAdd(${param.albumId});</c:if>
            <c:if test="${not empty param.playlistId}">top.playQueue.onPlayPlaylist(${param.playlistId}, true);</c:if>
            <c:if test="${not empty param.podcastChannelId}">top.playQueue.onPlayPodcastChannel(${param.podcastChannelId}, true);</c:if>">
                <i class="material-icons" style="position:absolute; top: 8px; left: 46px; z-index: 2; font-size:36px; opacity:0.8">add_circle</i>
                <i class="material-icons" style="position:absolute; top: 14px; left: 52px; z-index: 3; font-size:24px; color:white">add</i>
            </div>
        </div>
        <c:choose>
        <c:when test="${param.showLink}"><a href="${targetUrl}" title="${param.caption1}"></c:when>
        <c:when test="${param.showZoom}"><a href="${zoomCoverArtUrl}" rel="zoom" title="${param.caption1}"></c:when>
            </c:choose>
            <img src="${coverArtUrl}" alt="${param.caption1}" style="display:none"
                 onload="$(this).delay(${empty param.appearAfter ? 0 : param.appearAfter}).fadeIn(500);">
        <c:if test="${param.showLink or param.showZoom}"></a></c:if>
    </div>

    <c:if test="${captionCount gt 0}">
        <div class="caption1" style="width:${param.coverArtSize - 16}px"><a href="${targetUrl}" title="${param.caption1}">${param.caption1}</a></div>
    </c:if>
    <c:if test="${captionCount gt 1}">
        <div class="caption2" style="width:${param.coverArtSize - 16}px">${param.caption2}&nbsp;</div>
    </c:if>
    <c:if test="${captionCount gt 2}">
        <div class="caption3" style="width:${param.coverArtSize - 16}px">${param.caption3}&nbsp;</div>
    </c:if>
</div>

<c:if test="${param.showChange}">
    <div style="padding-top:6px;text-align:right">
        <c:url value="/changeCoverArt.view" var="changeCoverArtUrl">
            <c:param name="id" value="${param.albumId}"/>
        </c:url>
        <i class="fa fa-edit icon clickable" onclick="location.href='${changeCoverArtUrl}'"
           title="<fmt:message key="coverart.change"/>"></i>
    </div>
</c:if>
