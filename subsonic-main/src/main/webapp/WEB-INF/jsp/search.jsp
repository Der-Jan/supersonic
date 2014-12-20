<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<%--@elvariable id="command" type="net.sourceforge.subsonic.command.SearchCommand"--%>

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
</head>
<body class="mainframe bgcolor1">

<h1>
    <img src="<spring:theme code="searchImage"/>" alt=""/>
    <span style="vertical-align: middle"><fmt:message key="search.title"/></span>
</h1>

<c:if test="${command.indexBeingCreated}">
    <p class="warning"><fmt:message key="search.index"/></p>
</c:if>

<c:if test="${not command.indexBeingCreated and empty command.artists and empty command.albums and empty command.songs}">
    <p class="warning"><fmt:message key="search.hits.none"/></p>
</c:if>

<c:if test="${not empty command.artists}">
    <h2><b><fmt:message key="search.hits.artists"/></b></h2>
    <table class="music" style="width: 100%">
        <c:forEach items="${command.artists}" var="match" varStatus="loopStatus">

            <sub:url value="/main.view" var="mainUrl">
                <sub:param name="path" value="${match.path}"/>
            </sub:url>

            <tr class="${loopStatus.count % 2 == 1 ? "artistRow bgcolor2" : "artistRow"}" ${loopStatus.count > 5 ? "style='display:none'" : ""}>
                <c:import url="playButtons.jsp">
                    <c:param name="id" value="${match.id}"/>
                    <c:param name="playEnabled" value="${command.user.streamRole and not command.partyModeEnabled}"/>
                    <c:param name="addEnabled" value="${command.user.streamRole and (not command.partyModeEnabled or not match.directory)}"/>
                    <c:param name="asTable" value="true"/>
                </c:import>
                <td style="padding-left:0.25em;padding-right:1.25em">
                    <a href="${mainUrl}">${match.name}</a>
                </td>
                <td style="width:100%"></td>
            </tr>

            </c:forEach>
    </table>
    <c:if test="${fn:length(command.artists) gt 5}">
        <div id="moreArtists" class="forward"><a href="#" onclick="$('.artistRow').show(); $('#moreArtists').hide();"><fmt:message key="search.hits.more"/></a></div>
    </c:if>
</c:if>

<c:if test="${not empty command.albums}">
    <h2><b><fmt:message key="search.hits.albums"/></b></h2>
    <table class="music" style="width:100%">
        <c:forEach items="${command.albums}" var="match" varStatus="loopStatus">

            <sub:url value="/main.view" var="mainUrl">
                <sub:param name="path" value="${match.path}"/>
            </sub:url>

            <tr class="${loopStatus.count % 2 == 1 ? "albumRow bgcolor2" : "albumRow"}" ${loopStatus.count > 5 ? "style='display:none'" : ""}>
                <c:import url="playButtons.jsp">
                    <c:param name="id" value="${match.id}"/>
                    <c:param name="playEnabled" value="${command.user.streamRole and not command.partyModeEnabled}"/>
                    <c:param name="addEnabled" value="${command.user.streamRole and (not command.partyModeEnabled or not match.directory)}"/>
                    <c:param name="asTable" value="true"/>
                </c:import>

                <td style="padding-left:0.25em;padding-right:1.25em">
                    <a href="${mainUrl}">${match.albumName}</a>
                </td>

                <td style="padding-right:0.25em">
                    <span class="detail">${match.artist}</span>
                </td>

                <td style="width:100%"></td>
            </tr>

            </c:forEach>
    </table>
    <c:if test="${fn:length(command.albums) gt 5}">
        <div id="moreAlbums" class="forward"><a href="#" onclick="$('.albumRow').show(); $('#moreAlbums').hide();"><fmt:message key="search.hits.more"/></a></div>
    </c:if>
</c:if>


<c:if test="${not empty command.songs}">
    <h2><b><fmt:message key="search.hits.songs"/></b></h2>
    <table class="music" style="width:100%">
        <c:forEach items="${command.songs}" var="match" varStatus="loopStatus">

            <sub:url value="/main.view" var="mainUrl">
                <sub:param name="path" value="${match.parentPath}"/>
            </sub:url>

            <tr class="${loopStatus.count % 2 == 1 ? "songRow bgcolor2" : "songRow"}" ${loopStatus.count > 15 ? "style='display:none'" : ""}>
                <c:import url="playButtons.jsp">
                    <c:param name="id" value="${match.id}"/>
                    <c:param name="playEnabled" value="${command.user.streamRole and not command.partyModeEnabled}"/>
                    <c:param name="addEnabled" value="${command.user.streamRole and (not command.partyModeEnabled or not match.directory)}"/>
                    <c:param name="video" value="${match.video and command.player.web}"/>
                    <c:param name="asTable" value="true"/>
                </c:import>

                <td style="padding-left:0.25em;padding-right:1.25em">
                        <span class="songTitle">${match.title}</span>
                </td>

                <td style="padding-right:1.25em">
                    <a href="${mainUrl}"><span class="detail">${match.albumName}</span></a>
                </td>

                <td style="padding-right:0.25em">
                    <span class="detail">${match.artist}</span>
                </td>

                <td style="width:100%"></td>
            </tr>

            </c:forEach>
    </table>
<c:if test="${fn:length(command.songs) gt 15}">
    <div id="moreSongs" class="forward"><a href="#" onclick="$('.songRow').show();$('#moreSongs').hide(); "><fmt:message key="search.hits.more"/></a></div>
</c:if>
</c:if>

</body></html>