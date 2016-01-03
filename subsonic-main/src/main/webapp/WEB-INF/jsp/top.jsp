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
            margin-right:2em;
            cursor:pointer
        }
    </style>
</head>

<body class="bgcolor2 topframe" style="margin:0.4em 1.2em 0 1.2em;white-space:nowrap">

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

<form method="post" action="search.view" target="main" name="searchForm">

    <span class="topHeader top-menu-item" style="margin-right:3em">
        <i id="show-left-frame" class="fa fa-bars fa-lg icon" onclick="showLeftFrame()" style="display:${model.showSideBar ? 'none' : 'inline'}"></i>
        <i id="hide-left-frame" class="fa fa-bars fa-lg icon" onclick="hideLeftFrame()" style="display:${model.showSideBar ? 'inline' : 'none'}"></i>
    </span>

    <span class="topHeader top-menu-item" onclick="showPage('home.view')"><i class="fa fa-home fa-fw fa-lg icon"></i>&nbsp;${home}</span>
    <span class="topHeader top-menu-item" onclick="showPage('nowPlaying.view')"><i class="fa fa-headphones fa-fw fa-lg icon"></i>&nbsp;${nowPlaying}</span>
    <span class="topHeader top-menu-item" onclick="showPage('starred.view')"><i class="fa fa-star fa-fw fa-lg icon"></i>&nbsp;${starred}</span>
    <span class="topHeader top-menu-item" onclick="showPage('playlists.view')"><i class="fa fa-music fa-fw fa-lg icon"></i>&nbsp;${playlists}</span>
    <span class="topHeader top-menu-item" onclick="showPage('podcasts.view')"><i class="fa fa-rss fa-lg icon"></i>&nbsp;${podcast}</span>
    <span class="topHeader top-menu-item" onclick="showPage('settings.view')"><i class="fa fa-cog fa-lg icon"></i>&nbsp;${settings}</span>
    <span class="topHeader top-menu-item" onclick="showPage('more.view')"><i class="fa fa-mobile fa-lg icon"></i>&nbsp;${more}</span>

    <input type="text" name="query" id="query" size="28" placeholder="${search}" onclick="select();" onkeyup="triggerInstantSearch();" style="margin-left:1em">
    <i class="fa fa-search icon clickable" onclick="document.searchForm.submit()"></i>

    <span class="topHeader" style="float:right">
    <c:if test="${model.showAvatar}">
        <sub:url value="avatar.view" var="avatarUrl">
            <sub:param name="username" value="${model.user.username}"/>
        </sub:url>
            <c:if test="${model.user.settingsRole}"><a href="personalSettings.view" target="main"></c:if>
            <img src="${avatarUrl}" alt="" width="24" height="24">
            <c:if test="${model.user.settingsRole}"></a></c:if>
    </c:if>

        <fmt:message key="top.logout" var="logout"><fmt:param value="${model.user.username}"/></fmt:message>
        <a href="j_acegi_logout" target="_top">${fn:escapeXml(logout)}</a>
    </span>

</form>

</body></html>