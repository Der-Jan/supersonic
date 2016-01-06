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

<div style="padding-top:1em; padding-bottom:2.5em; text-align:center">
    <a href="help.view" target="main"><img src="<spring:theme code="logoImage"/>" title="<fmt:message key="top.help"/>" alt=""></a>
</div>

<c:if test="${fn:length(model.musicFolders) > 1}">
    <div style="padding-bottom:0.5em" class="topHeader">
        <i class="fa fa-folder-open-o fa-lg fa-fw icon"></i>&nbsp;<fmt:message key="settingsheader.musicFolder"/>
    </div>

    <div style="padding-bottom:2.0em">
        <select name="musicFolderId" style="width:100%" onchange="location='left.view?musicFolderId=' + options[selectedIndex].value;">
            <option value="-1"><fmt:message key="left.allfolders"/></option>
            <c:forEach items="${model.musicFolders}" var="musicFolder">
                <option ${model.selectedMusicFolder.id == musicFolder.id ? "selected" : ""} value="${musicFolder.id}">${fn:escapeXml(musicFolder.name)}</option>
            </c:forEach>
        </select>
    </div>
</c:if>

<div style="padding-bottom:0.7em" class="topHeader">
    <fmt:message key="top.logout" var="logout"><fmt:param value="${model.user.username}"/></fmt:message>
    <i class="fa fa-sign-out fa-lg fa-fw icon"></i>&nbsp;<a href="j_acegi_logout" target="_top">${fn:escapeXml(logout)}</a>
</div>

<c:if test="${model.user.settingsRole}">
    <div style="padding-bottom:0.7em" class="topHeader">
        <i class="fa fa-cog fa-lg fa-fw icon"></i>&nbsp;<a href="settings.view" target="main"><fmt:message key="top.settings"/></a>
    </div>
</c:if>

<div style="padding-bottom:1.5em" class="topHeader">
    <i class="fa fa-info-circle fa-lg fa-fw icon"></i>&nbsp;<a href="help.view" target="main"><fmt:message key="help.title"><fmt:param value="${model.brand}"/></fmt:message></a>
</div>

</body>
</html>