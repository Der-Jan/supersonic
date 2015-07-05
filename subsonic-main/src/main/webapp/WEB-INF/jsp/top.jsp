<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>

    <script type="text/javascript">
        var previousQuery = "";
        var instantSearchTimeout;

        function triggerInstantSearch() {
            if (instantSearchTimeout) {
                window.clearTimeout(instantSearchTimeout);
            }
            instantSearchTimeout = window.setTimeout(executeInstantSearch, 300);
        }

        function executeInstantSearch() {
            var query = $("#query").val().trim();
            if (query.length > 1 && query != previousQuery) {
                previousQuery = query;
                document.searchForm.submit();
            }
        }
    </script>
</head>

<body class="bgcolor2 topframe" style="margin:0.4em 1em 0 1em;">

<fmt:message key="top.home" var="home"/>
<fmt:message key="top.now_playing" var="nowPlaying"/>
<fmt:message key="top.starred" var="starred"/>
<fmt:message key="left.playlists" var="playlists"/>
<fmt:message key="top.settings" var="settings"/>
<fmt:message key="top.podcast" var="podcast"/>
<fmt:message key="top.more" var="more"/>
<fmt:message key="top.help" var="help"/>
<fmt:message key="top.search" var="search"/>

<table style="margin:0;padding-top:5px">
    <tr>
        <td style="padding-right:3.5em;">
            <a href="help.view?" target="main"><img src="<spring:theme code="logoImage"/>" title="${help}" alt=""></a>
        </td>
        <td style="min-width:4em;padding-right:2em;text-align: center">
            <a href="home.view?" target="main"><img src="<spring:theme code="homeImage"/>" title="${home}" alt="${home}"></a>
            <div class="topHeader"><a href="home.view?" target="main">${home}</a></div>
        </td>
        <td style="min-width:4em;padding-right:2em;text-align: center">
            <a href="nowPlaying.view?" target="main"><img src="<spring:theme code="nowPlayingImage"/>" title="${nowPlaying}" alt="${nowPlaying}"></a>
            <div class="topHeader"><a href="nowPlaying.view?" target="main">${nowPlaying}</a></div>
        </td>
        <td style="min-width:4em;padding-right:2em;text-align: center">
            <a href="starred.view?" target="main"><img src="<spring:theme code="starredImage"/>" title="${starred}" alt="${starred}"></a>
            <div class="topHeader"><a href="starred.view?" target="main">${starred}</a></div>
        </td>
        <td style="min-width:4em;padding-right:2em;text-align: center">
            <a href="playlists.view?" target="main"><img src="<spring:theme code="playlistImage"/>" title="${playlists}" alt="${playlists}"></a>
            <div class="topHeader"><a href="playlists.view?" target="main">${playlists}</a></div>
        </td>
        <td style="min-width:4em;padding-right:2em;text-align: center">
            <a href="podcastChannels.view?" target="main"><img src="<spring:theme code="podcastLargeImage"/>" title="${podcast}" alt="${podcast}"></a>
            <div class="topHeader"><a href="podcastChannels.view?" target="main">${podcast}</a></div>
        </td>
        <c:if test="${model.user.settingsRole}">
            <td style="min-width:4em;padding-right:2em;text-align: center">
                <a href="settings.view?" target="main"><img src="<spring:theme code="settingsImage"/>" title="${settings}" alt="${settings}"></a>
                <div class="topHeader"><a href="settings.view?" target="main">${settings}</a></div>
            </td>
        </c:if>
        <td style="min-width:4em;padding-right:2em;text-align: center">
            <a href="more.view?" target="main"><img src="<spring:theme code="moreImage"/>" title="${more}" alt="${more}"></a>
            <div class="topHeader"><a href="more.view?" target="main">${more}</a></div>
        </td>
        <td style="min-width:4em;padding-right:2em;text-align: center">
            <a href="help.view?" target="main"><img src="<spring:theme code="helpImage"/>" title="${help}" alt="${help}"></a>
            <div class="topHeader"><a href="help.view?" target="main">${help}</a></div>
        </td>

        <td style="padding-left:1em">
            <form method="post" action="search.view" target="main" name="searchForm">
                <td><input type="text" name="query" id="query" size="28" placeholder="${search}" onclick="select();"
                           onkeyup="triggerInstantSearch();"></td>
                <td><a href="javascript:document.searchForm.submit()"><img src="<spring:theme code="searchImage"/>" alt="${search}" title="${search}"></a></td>
            </form>
        </td>

        <td style="padding-left:15pt;padding-right:5pt;vertical-align: middle;width: 100%;text-align: center">

            <c:if test="${model.showAvatar}">
            <sub:url value="avatar.view" var="avatarUrl">
                <sub:param name="username" value="${model.user.username}"/>
            </sub:url>
                <div style="padding-bottom: 4px">
                    <c:if test="${model.user.settingsRole}"><a href="personalSettings.view" target="main"></c:if>
                        <img src="${avatarUrl}" alt="" width="30" height="30">
                        <c:if test="${model.user.settingsRole}"></a></c:if>
                </div>
            </c:if>

            <div class="detail">
                <a href="j_acegi_logout" target="_top"><fmt:message key="top.logout"><fmt:param value="${model.user.username}"/></fmt:message></a>
            </div>
        </td>

    </tr></table>

</body></html>