<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
    <script type="text/javascript" src="<c:url value="/script/scripts.js"/>"></script>
    <script type="text/javascript" src="<c:url value='/dwr/util.js'/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/engine.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/interface/starService.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/interface/playlistService.js"/>"></script>
    <script type="text/javascript" language="javascript">

        function toggleStar(mediaFileId, imageId) {
            if ($(imageId).attr("src").indexOf("<spring:theme code="ratingOnImage"/>") != -1) {
                $(imageId).attr("src", "<spring:theme code="ratingOffImage"/>");
                starService.unstar(mediaFileId);
            }
            else if ($(imageId).attr("src").indexOf("<spring:theme code="ratingOffImage"/>") != -1) {
                $(imageId).attr("src", "<spring:theme code="ratingOnImage"/>");
                starService.star(mediaFileId);
            }
        }

        function onSavePlaylist() {
            playlistService.createPlaylistForStarredSongs(function (playlistId) {
                top.left.updatePlaylists();
                top.left.showAllPlaylists();
                top.main.location.href = "playlist.view?id=" + playlistId;
                $().toastmessage("showSuccessToast", "<fmt:message key="playlist.toast.saveasplaylist"/>");
            });
        }

        function onPlayAll() {
            top.playQueue.onPlayStarred();
        }

    </script>
</head>
<body class="mainframe bgcolor1">

<h1>
    <img src="<spring:theme code="starredImage"/>" alt="">
    <span style="vertical-align: middle"><fmt:message key="starred.title"/></span>
</h1>

<c:if test="${empty model.artists and empty model.albums and empty model.songs}">
    <p style="padding-top: 1em"><em><fmt:message key="starred.empty"/></em></p>
</c:if>

<c:if test="${not empty model.albums}">
    <h2><fmt:message key="search.hits.albums"/></h2>

<div>
    <c:forEach items="${model.albums}" var="album" varStatus="loopStatus">

        <div class="albumThumb">
            <c:import url="coverArt.jsp">
                <c:param name="albumId" value="${album.id}"/>
                <c:param name="albumName" value="${album.title}"/>
                <c:param name="coverArtSize" value="${model.coverArtSize}"/>
                <c:param name="showLink" value="true"/>
                <c:param name="showZoom" value="false"/>
                <c:param name="showChange" value="false"/>
                <c:param name="appearAfter" value="${loopStatus.count * 30}"/>
            </c:import>
            <c:choose>
                <c:when test="${empty album.artist and empty album.title}">
                    <div class="detail"><fmt:message key="common.unknown"/></div>
                </c:when>
                <c:otherwise>
                    <div class="detail"><b><str:truncateNicely lower="22" upper="22">${album.artist}</str:truncateNicely></b></div>
                    <div class="detail"><str:truncateNicely lower="22" upper="22">${album.name}</str:truncateNicely></div>
                </c:otherwise>
            </c:choose>
        </div>
    </c:forEach>
    </c:if>
</div>

<c:if test="${not empty model.artists}">
    <h2><fmt:message key="search.hits.artists"/></h2>
    <table class="music">
        <c:forEach items="${model.artists}" var="artist" varStatus="loopStatus">

            <sub:url value="/main.view" var="mainUrl">
                <sub:param name="path" value="${artist.path}"/>
            </sub:url>

            <tr ${loopStatus.count % 2 == 1 ? "class='bgcolor2'" : ""}>
                <c:import url="playButtons.jsp">
                    <c:param name="id" value="${artist.id}"/>
                    <c:param name="playEnabled" value="${model.user.streamRole and not model.partyModeEnabled}"/>
                    <c:param name="addEnabled" value="${model.user.streamRole and (not model.partyModeEnabled or not artist.directory)}"/>
                    <c:param name="starEnabled" value="true"/>
                    <c:param name="starred" value="${not empty artist.starredDate}"/>
                    <c:param name="asTable" value="true"/>
                </c:import>
                <td style="padding-left:0.25em;padding-right:1.25em">
                    <a href="${mainUrl}">${artist.name}</a>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>

<c:if test="${not empty model.songs}">
    <h2><fmt:message key="search.hits.songs"/></h2>
    <table class="music">
        <c:forEach items="${model.songs}" var="song" varStatus="loopStatus">

            <sub:url value="/main.view" var="mainUrl">
                <sub:param name="path" value="${song.parentPath}"/>
            </sub:url>

            <tr ${loopStatus.count % 2 == 1 ? "class='bgcolor2'" : ""}>
                <c:import url="playButtons.jsp">
                    <c:param name="id" value="${song.id}"/>
                    <c:param name="playEnabled" value="${model.user.streamRole and not model.partyModeEnabled}"/>
                    <c:param name="addEnabled" value="${model.user.streamRole and (not model.partyModeEnabled or not song.directory)}"/>
                    <c:param name="starEnabled" value="true"/>
                    <c:param name="starred" value="${not empty song.starredDate}"/>
                    <c:param name="video" value="${song.video and model.player.web}"/>
                    <c:param name="asTable" value="true"/>
                </c:import>

                <td style="padding-left:0.25em;padding-right:1.25em">
                        ${song.title}
                </td>

                <td style="padding-right:1.25em">
                    <a href="${mainUrl}"><span class="detail">${song.albumName}</span></a>
                </td>

                <td style="padding-right:0.25em">
                    <span class="detail">${song.artist}</span>
                </td>
            </tr>

        </c:forEach>
    </table>

    <div class="forward" style="float:left;padding-right:1.5em">
        <a href="javascript:noop()" onclick="onSavePlaylist()"><fmt:message key="playlist.save"/></a>
    </div>
    <div class="forward" style="float: left">
        <a href="javascript:noop()" onclick="onPlayAll()"><fmt:message key="main.playall"/></a>
    </div>
    <div style="clear: both"></div>

</c:if>

</body></html>