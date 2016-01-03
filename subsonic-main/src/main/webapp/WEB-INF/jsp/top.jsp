<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
    <script type="text/javascript" src="<c:url value="/dwr/engine.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/interface/multiService.js"/>"></script>

    <script type="text/javascript">
        var previousQuery = "";
        var instantSearchTimeout;

        function showPage(url) {
            parent.frames.main.location.href = url;
        }

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

        function showLeftFrame() {
            $("#show-left-frame").hide();
            $("#hide-left-frame").show();
            toggleLeftFrame(230);
            multiService.setShowSideBar(true);
        }

        function hideLeftFrame() {
            $("#hide-left-frame").hide();
            $("#show-left-frame").show();
            toggleLeftFrame(0);
            multiService.setShowSideBar(false);
        }

        function toggleLeftFrame(width) {
            <%-- Disable animation in Chrome. It stopped working in Chrome 44. --%>
            var duration = navigator.userAgent.indexOf("Chrome") != -1 ? 0 : 400;

            $("#dummy-animation-target").stop();
            $("#dummy-animation-target").animate({"max-width": width}, {
                step: function (now, fx) {
                    top.document.getElementById("mainFrameset").cols = now + ",*";
                },
                duration: duration
            });
        }
    </script>

    <style type="text/css">
        .top-menu-item {
            min-width:4em; padding-right:3em; text-align:center; cursor:pointer
        }
    </style>
</head>

<body class="bgcolor2 topframe" style="margin:0.4em 1em 0 1em;">

<span id="dummy-animation-target" style="max-width:0;display: none"></span>

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
        <td style="padding-right:5.5em;">
            <span class="icon icon-20 clickable">
                <i id="show-left-frame" class="fa fa-bars" onclick="showLeftFrame()" style="display:${model.showSideBar ? 'none' : 'inline'}"></i>
                <i id="hide-left-frame" class="fa fa-bars" onclick="hideLeftFrame()" style="display:${model.showSideBar ? 'inline' : 'none'}"></i>
            </span>
        </td>
        <td class="top-menu-item" onclick="showPage('home.view')">
            <span class="icon icon-26"><i class="fa fa-home fa-fw"></i></span>
            <div class="topHeader">${home}</div>
        </td>
        <td class="top-menu-item" onclick="showPage('nowPlaying.view')">
            <span class="icon icon-26"><i class="fa fa-headphones fa-fw"></i></span>
            <div class="topHeader">${nowPlaying}</div>
        </td>
        <td class="top-menu-item" onclick="showPage('starred.view')">
            <span class="icon icon-26"><i class="fa fa-star fa-fw"></i></span>
            <div class="topHeader">${starred}</div>
        </td>
        <td class="top-menu-item" onclick="showPage('playlists.view')">
            <span class="icon icon-26"><i class="fa fa-music fa-fw"></i></span>
            <div class="topHeader">${playlists}</div>
        </td>
        <td class="top-menu-item" onclick="showPage('podcastChannels.view')">
            <span class="icon icon-26"><i class="fa fa-rss fa-fw"></i></span>
            <div class="topHeader">${podcast}</div>
        </td>
        <c:if test="${model.user.settingsRole}">
            <td class="top-menu-item" onclick="showPage('settings.view')">
                <span class="icon icon-26"><i class="fa fa-cog fa-fw"></i></span>
                <div class="topHeader">${settings}</div>
            </td>
        </c:if>
        <td class="top-menu-item" onclick="showPage('more.view')">
            <span class="icon icon-26"><i class="fa fa-mobile fa-fw"></i></span>
            <div class="topHeader">${more}</div>
        </td>
        <td style="padding-left:1em">
            <form method="post" action="search.view" target="main" name="searchForm">
                <td><input type="text" name="query" id="query" size="28" placeholder="${search}" onclick="select();"
                           onkeyup="triggerInstantSearch();"></td>
                <td>
                    <span class="icon icon-20 clickable" onclick="document.searchForm.submit()"><i class="fa fa-search"></i></span>
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
                <fmt:message key="top.logout" var="logout"><fmt:param value="${model.user.username}"/></fmt:message>
                <a href="j_acegi_logout" target="_top">${fn:escapeXml(logout)}</a>
            </div>
        </td>

    </tr></table>

</body></html>