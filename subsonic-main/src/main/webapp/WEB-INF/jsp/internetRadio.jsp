<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<%--@elvariable id="model" type="java.util.Map"--%>

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
    <link type="text/css" rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">

    <style>
        audio {
            margin-left: 2em;
            margin-right: 2em;
        }
    </style>
</head>

<body class="mainframe bgcolor1">

<h1 style="padding-bottom: 1em">
    <i class="fa fa-globe fa-lg icon"></i>&nbsp;&nbsp;<fmt:message key="left.radio"/>
</h1>

<c:if test="${empty model.stations}">
    <p><em><fmt:message key="internetradio.empty"/></em></p>
</c:if>

<c:if test="${not empty model.stations}">
    <table class="music indent" style="line-height: 400%;">
        <c:forEach items="${model.stations}" var="station">
            <tr>
                <td class="fit">
                    <span style="margin-left: 2em"><b>${fn:escapeXml(station.name)}</b></span>
                </td>
                <td class="fit">
                    <audio controls preload="none" src="${station.streamUrl}"></audio>
                </td>
                <td class="fit">
                    <i class="material-icons clickable" onclick="window.open('${station.streamUrl}', '_blank');">open_in_new</i>
                </td>
                <td class="truncate"></td>
            </tr>
        </c:forEach>
    </table>
</c:if>


<c:if test="${model.user.adminRole}">
    <div style="padding-top:2em;">
        <i class="fa fa-cog fa-lg fa-fw icon"></i>&nbsp;&nbsp;<a href="internetRadioSettings.view"><fmt:message key="internetradio.settings"/></a>
    </div>
</c:if>

<div style="padding-top:2em"></div>

</body>
</html>
