<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>
<%--@elvariable id="model" type="Map"--%>

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
    <style>
        th {
            text-align: left;
        }
    </style>
</head>
<body class="mainframe bgcolor1">

<c:set var="category" value="videoConversion"/>
<c:set var="toast" value="${model.toast}"/>
<c:set var="user" value="${model.user}"/>
<%@ include file="settingsHeader.jsp" %>

<form method="post" action="videoConversionSettings.view">

    <table>
        <tr>
            <td style="padding-right:0.5em"><fmt:message key="videoconversionsettings.directory"/></td>
            <td colspan="2"><input name="directory" value="${model.directory}" style="width:100%"></td>
        </tr>
        <tr>
            <td style="padding-right:0.5em"><fmt:message key="videoconversionsettings.limit"/></td>
            <td style="padding-right:0.5em">
                <select name="diskLimit">
                    <option value="0" ${model.diskLimit eq 0 ? "selected" : ""}><fmt:message key="videoconversionsettings.nolimit"/></option>
                    <c:forTokens items="1 2 3 5 10 20 30 50 100 200 300 500 1000 2000 3000 5000" delims=" " var="limit">
                        <option value="${limit}" ${model.diskLimit eq limit ? "selected" : ""}>${limit} GB</option>
                    </c:forTokens>
                </select>
            </td>
            <td class="detail"><fmt:message key="videoconversionsettings.used"/> <sub:formatBytes bytes="${model.bytesUsed}"/></td>
        </tr>
    </table>

    <c:if test="${not empty model.conversionInfos}">
        <h2><fmt:message key="videoconversionsettings.convertedvideos"/></h2>
        <table class="music indent">
            <tr>
                <th class="truncate"><fmt:message key="videoconversionsettings.source"/></th>
                <th class="truncate"><fmt:message key="videoConverter.details.targetfile"/></th>
                <th class="truncate"><fmt:message key="personalsettings.filesize"/></th>
                <th class="truncate"><fmt:message key="videoConverter.details.status"/></th>
                <th class="truncate"><fmt:message key="personalsettings.bitrate"/></th>
                <th class="truncate"><fmt:message key="usersettings.username"/></th>
                <th class="fit" style="text-align:center"><fmt:message key="common.delete"/></th>
            </tr>

            <c:forEach items="${model.conversionInfos}" var="conversionInfo">
                <tr>
                    <td class="truncate" style="max-width:150px"><a href="videoPlayer.view?id=${conversionInfo.video.id}">${conversionInfo.video.name}</a></td>
                    <td class="truncate" style="max-width:150px" title="${conversionInfo.conversion.targetFile}">${conversionInfo.conversion.targetFile}</td>
                    <td class="truncate"><sub:formatBytes bytes="${conversionInfo.size}"/></td>
                    <td class="truncate"><fmt:message key="videoConverter.status.${fn:toLowerCase(conversionInfo.conversion.status)}"/></td>
                    <td class="truncate"><c:if test="${not empty conversionInfo.conversion.bitRate}">${conversionInfo.conversion.bitRate} Kbps</c:if></td>
                    <td class="truncate">${conversionInfo.conversion.username}</td>
                    <td class="fit" style="text-align:center"><input type="checkbox" name="delete[${conversionInfo.conversion.id}]" class="checkbox"/></td>
                </tr>
            </c:forEach>
        </table>
    </c:if>

    <c:set var="licenseInfo" value="${model.licenseInfo}"/>
    <%@ include file="licenseNotice.jsp" %>

    <p style="padding-top:1em">
        <input type="submit" value="<fmt:message key="common.save"/>" style="margin-right:0.3em">
        <input type="button" value="<fmt:message key="common.cancel"/>" onclick="location.href='nowPlaying.view'" style="margin-right:2.0em">
    </p>

</form>

</body></html>