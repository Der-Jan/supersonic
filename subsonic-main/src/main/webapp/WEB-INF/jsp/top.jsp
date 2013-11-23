<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html><head>
    <%@ include file="head.jsp" %>
</head>

<body class="bgcolor2 topframe" style="margin:0.4em 1em 0 1em; ">

<fmt:message key="top.home" var="home"/>
<fmt:message key="top.now_playing" var="nowPlaying"/>
<fmt:message key="top.starred" var="starred"/>
<fmt:message key="top.settings" var="settings"/>
<fmt:message key="top.status" var="status"/>
<fmt:message key="top.podcast" var="podcast"/>
<fmt:message key="top.more" var="more"/>
<fmt:message key="top.help" var="help"/>
<fmt:message key="top.search" var="search"/>

<table style="margin:0;">
    <tr>
        <td></td>
        <td colspan="13" style="padding:0;margin:0">
            <c:choose>
                <c:when test="${not model.musicFoldersExist}">
                    <span class="warning"><fmt:message key="top.missing"/></span>
                </c:when>
                <c:when test="${model.newVersionAvailable}">
                    <span class="warning">
                        <fmt:message key="top.upgrade"><fmt:param value="${model.brand}"/><fmt:param value="${model.latestVersion}"/></fmt:message>
                    </span>
                </c:when>
            </c:choose>
        </td>
    </tr>
    <tr>
        <td style="padding-right:2em;">
            <a href="help.view?" target="main"><img src="<spring:theme code="logoImage"/>" title="${help}" alt=""></a>
        </td>
        <td style="min-width:4em;padding-right:1.5em;text-align: center">
            <a href="home.view?" target="main"><img src="<spring:theme code="homeImage"/>" title="${home}" alt="${home}"></a>
            <div class="topHeader"><a href="home.view?" target="main">${home}</a></div>
        </td>
        <td style="min-width:4em;padding-right:1.5em;text-align: center">
            <a href="nowPlaying.view?" target="main"><img src="<spring:theme code="nowPlayingImage"/>" title="${nowPlaying}" alt="${nowPlaying}"></a>
            <div class="topHeader"><a href="nowPlaying.view?" target="main">${nowPlaying}</a></div>
        </td>
        <td style="min-width:4em;padding-right:1.5em;text-align: center">
            <a href="starred.view?" target="main"><img src="<spring:theme code="starredImage"/>" title="${starred}" alt="${starred}"></a>
            <div class="topHeader"><a href="starred.view?" target="main">${starred}</a></div>
        </td>
        <td style="min-width:4em;padding-right:1.5em;text-align: center">
            <a href="podcastReceiver.view?" target="main"><img src="<spring:theme code="podcastLargeImage"/>" title="${podcast}" alt="${podcast}"></a>
            <div class="topHeader"><a href="podcastReceiver.view?" target="main">${podcast}</a></div>
        </td>
        <c:if test="${model.user.settingsRole}">
            <td style="min-width:4em;padding-right:1.5em;text-align: center">
                <a href="settings.view?" target="main"><img src="<spring:theme code="settingsImage"/>" title="${settings}" alt="${settings}"></a>
                <div class="topHeader"><a href="settings.view?" target="main">${settings}</a></div>
            </td>
        </c:if>
        <td style="min-width:4em;padding-right:1.5em;text-align: center">
            <a href="status.view?" target="main"><img src="<spring:theme code="statusImage"/>" title="${status}" alt="${status}"></a>
            <div class="topHeader"><a href="status.view?" target="main">${status}</a></div>
        </td>
        <td style="min-width:4em;padding-right:1.5em;text-align: center">
            <a href="more.view?" target="main"><img src="<spring:theme code="moreImage"/>" title="${more}" alt="${more}"></a>
            <div class="topHeader"><a href="more.view?" target="main">${more}</a></div>
        </td>
        <td style="min-width:4em;padding-right:1.5em;text-align: center">
            <a href="help.view?" target="main"><img src="<spring:theme code="helpImage"/>" title="${help}" alt="${help}"></a>
            <div class="topHeader"><a href="help.view?" target="main">${help}</a></div>
        </td>

        <td style="padding-left:1em">
            <form method="post" action="search.view" target="main" name="searchForm">
                <td><input type="text" name="query" id="query" size="28" placeholder="${search}" onclick="select();"></td>
                <td><a href="javascript:document.searchForm.submit()"><img src="<spring:theme code="searchImage"/>" alt="${search}" title="${search}"></a></td>
            </form>
        </td>

        <td style="padding-left:15pt;vertical-align:middle;text-align: center;">
            <div class="detail">
                <c:if test="${not model.licenseInfo.licenseValid}">
                    <a href="premium.view" target="main"><img src="<spring:theme code="donateSmallImage"/>" alt="">
                        <fmt:message key="top.getpremium"/></a>
                    <c:if test="${model.licenseInfo.trialDaysLeft gt 0}">
                        <br>
                        <a href="premium.view" target="main"><fmt:message key="top.trialdaysleft"><fmt:param value="${model.licenseInfo.trialDaysLeft}"/></fmt:message></a>
                    </c:if>
                </c:if>
            </div>
        </td>

        <td style="padding-left:15pt;padding-right:5pt;vertical-align: middle;width: 100%;text-align: right">
            <span class="detail">
                <a href="j_acegi_logout" target="_top"><fmt:message key="top.logout"><fmt:param value="${model.user.username}"/></fmt:message></a>
            </span>
        </td>

    </tr></table>

</body></html>