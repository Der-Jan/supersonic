<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>
<%@ include file="include.jsp" %>

<%--
PARAMETERS
  albumId: ID of album.
  coverArtSize: Height and width of cover art.
  albumName: Album name to display as caption and img alt.
  showLink: Whether to make the cover art image link to the album page.
  showZoom: Whether to display a link for zooming the cover art.
  showChange: Whether to display a link for changing the cover art.
  showCaption: Whether to display the album name as a caption below the image.
  captionLength: Truncate caption after this many characters.
  appearAfter: Fade in after this many milliseconds, or nil if no fading in should happen.
--%>
<c:choose>
    <c:when test="${empty param.coverArtSize}">
        <c:set var="size" value="auto"/>
    </c:when>
    <c:otherwise>
        <c:set var="size" value="${param.coverArtSize + 8}px"/>
    </c:otherwise>
</c:choose>

<str:randomString count="5" type="alphabet" var="divId"/>
<str:randomString count="5" type="alphabet" var="imgId"/>
<str:randomString count="5" type="alphabet" var="playId"/>

<div style="width:${size}; max-width:${size}; height:${size}; max-height:${size};" title="${param.albumName}" id="${divId}">
    <sub:url value="main.view" var="mainUrl">
        <sub:param name="id" value="${param.albumId}"/>
    </sub:url>

    <sub:url value="/coverArt.view" var="coverArtUrl">
        <c:if test="${not empty param.coverArtSize}">
            <sub:param name="size" value="${param.coverArtSize}"/>
        </c:if>
        <sub:param name="id" value="${param.albumId}"/>
    </sub:url>
    <sub:url value="/coverArt.view" var="zoomCoverArtUrl">
        <sub:param name="id" value="${param.albumId}"/>
    </sub:url>

    <div style="position: relative; width: 0; height: 0">
        <a href="#" onclick="top.playQueue.onPlay(${param.albumId}); return false;">
            <img src="<spring:theme code="playOverlayImage"/>" id="${playId}"
                 style="position: relative; top: 8px; left: 8px; z-index: 2; display:none" ></a>
    </div>
    <c:choose>
    <c:when test="${param.showLink}"><a href="${mainUrl}" title="${param.albumName}"></c:when>
    <c:when test="${param.showZoom}"><a href="${zoomCoverArtUrl}" rel="zoom" title="${param.albumName}"></c:when>
        </c:choose>
        <img src="${coverArtUrl}" id="${imgId}" class="dropshadow" alt="${param.albumName}"
             style="display:none">
        <c:if test="${param.showLink or param.showZoom}"></a></c:if>
</div>

<script type="text/javascript">
    <c:if test="${not empty param.appearAfter}">
    $(document).ready(function () {
        setTimeout("$('#${imgId}').fadeIn(500)", ${param.appearAfter});
    });
    </c:if>

    $("#${divId}").mouseenter(function () {
        $("#${playId}").show();
        $("#${imgId}").stop();
        $("#${imgId}").animate({opacity: 0.7}, 150);
    });
    $("#${divId}").mouseleave(function () {
        $("#${playId}").hide();
        $("#${imgId}").stop();
        $("#${imgId}").animate({opacity: 1.0}, 150);
    });

</script>

<div style="text-align:right; padding-right: 8px;">
    <c:if test="${param.showChange}">
        <sub:url value="/changeCoverArt.view" var="changeCoverArtUrl">
            <sub:param name="id" value="${param.albumId}"/>
        </sub:url>
        <a class="detail" href="${changeCoverArtUrl}"><fmt:message key="coverart.change"/></a>
    </c:if>

    <c:if test="${param.showZoom and param.showChange}">
        |
    </c:if>

    <c:if test="${param.showZoom}">
        <a class="detail" rel="zoom" title="${param.albumName}" href="${zoomCoverArtUrl}"><fmt:message key="coverart.zoom"/></a>
    </c:if>

    <c:if test="${not param.showZoom and not param.showChange and param.showCaption}">
        <span class="detail"><str:truncateNicely upper="${param.captionLength}">${param.albumName}</str:truncateNicely></span>
    </c:if>
</div>