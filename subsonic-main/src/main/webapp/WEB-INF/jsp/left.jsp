<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
    <script type="text/javascript" language="javascript">

        function init() {
            var mainLocation = top.main.location.href;
            if (${model.musicFolderChanged}) {
                if (mainLocation.indexOf("/home.view") != -1 ||
                        mainLocation.indexOf("/artists.view") != -1 ) {
                    top.main.location.href = mainLocation;
                }
            }
        }
    </script>
</head>

<body class="bgcolor2 leftframe" onload="init()">
<a name="top"></a>

<div style="padding-bottom:1.5em">
    <a href="help.view?" target="main"><img src="<spring:theme code="logoImage"/>" title="<fmt:message key="top.help"/>" alt=""></a>
</div>

<c:if test="${fn:length(model.musicFolders) > 1}">
    <div style="padding-bottom:1.0em">
    <select name="musicFolderId" style="width:100%" onchange="location='left.view?musicFolderId=' + options[selectedIndex].value;">
            <option value="-1"><fmt:message key="left.allfolders"/></option>
            <c:forEach items="${model.musicFolders}" var="musicFolder">
                <option ${model.selectedMusicFolder.id == musicFolder.id ? "selected" : ""} value="${musicFolder.id}">${fn:escapeXml(musicFolder.name)}</option>
            </c:forEach>
        </select>
    </div>
</c:if>

<div style="padding-bottom:0.5em">
    <c:choose>
        <c:when test="${model.scanning}">
            <i class="fa fa-chevron-right icon"></i>&nbsp;<a href="left.view"><fmt:message key="common.refresh"/></a>
        </c:when>
        <c:otherwise>
            <i class="fa fa-chevron-right icon"></i>&nbsp;<a href="left.view?refresh=true"><fmt:message key="common.refresh"/></a>
        </c:otherwise>
    </c:choose>
</div>

<div style="padding-bottom:0.5em">
    <c:choose>
        <c:when test="${model.showAvatar}">
            <sub:url value="avatar.view" var="avatarUrl">
                <sub:param name="username" value="${model.user.username}"/>
            </sub:url>
            <img src="${avatarUrl}" alt="" width="36" height="36">
        </c:when>
        <c:otherwise>
            <i class="fa fa-user fa-lg fa-fw icon"></i>
        </c:otherwise>
    </c:choose>
    <fmt:message key="top.logout" var="logout"><fmt:param value="${model.user.username}"/></fmt:message>
    <a href="j_acegi_logout" target="_top">${fn:escapeXml(logout)}</a>
</div>

<c:if test="${not empty model.radios}">
    <h2 class="bgcolor1" style="padding-left: 2px"><fmt:message key="left.radio"/></h2>
    <c:forEach items="${model.radios}" var="radio">
        <p class="dense" style="padding-left: 2px">
            <i class="fa fa-play icon"></i>
            <a target="hidden" href="${radio.streamUrl}">${fn:escapeXml(radio.name)}</a>
        </p>
    </c:forEach>
</c:if>

<c:if test="${model.statistics.songCount gt 0}">
    <div class="detail" style="padding-top: 0.6em; padding-left: 2px">
        <fmt:message key="left.statistics">
            <fmt:param value="${model.statistics.artistCount}"/>
            <fmt:param value="${model.statistics.albumCount}"/>
            <fmt:param value="${model.statistics.songCount}"/>
            <fmt:param value="${model.bytes}"/>
            <fmt:param value="${model.hours}"/>
        </fmt:message>
    </div>
</c:if>

</body></html>