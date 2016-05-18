<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>

<html>
<head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>

    <style type="text/css">
        .ui-progressbar-value {
            background: #E65100;
        }
    </style>
    <script type="text/javascript" src="<c:url value="/dwr/engine.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/interface/multiService.js"/>"></script>

    <script type="text/javascript" language="javascript">

        var authorized = ${model.user.videoConversionRole};
        var updateInterval = 2000;

        function init() {
            $("#conversion-progressbar").progressbar({
                max: ${model.video.durationSeconds}
            });
            dwr.engine.setErrorHandler(null);
            updateConversionStatus();
        }
        function updateConversionStatus() {
            multiService.getVideoConversionStatus(${model.video.id}, conversionStatusCallback);
            setTimeout(updateConversionStatus, updateInterval);
        }
        function conversionStatusCallback(conversionStatus) {
            $("#conversion-progressbar").progressbar("option", "value", conversionStatus == null ? 0 : conversionStatus.progressSeconds);
            $("#conversion-status-new").toggle(conversionStatus != null && conversionStatus.statusNew);
            $("#conversion-status-in-progress").toggle(conversionStatus != null && conversionStatus.statusInProgress);
            $("#conversion-status-completed").toggle(conversionStatus != null && conversionStatus.statusCompleted);
            $("#conversion-status-error").toggle(conversionStatus != null && conversionStatus.statusError);

            $("#conversion-start").toggle(authorized && (conversionStatus == null || conversionStatus.statusError));
            $("#conversion-audio-track-container").toggle(authorized && (conversionStatus == null || conversionStatus.statusError));
            $("#conversion-bit-rate-container").toggle(authorized && (conversionStatus == null || conversionStatus.statusError));
            $("#conversion-cancel").toggle(authorized && (conversionStatus != null && (conversionStatus.statusNew || conversionStatus.statusInProgress)));

            $("#conversion-target-file").text(conversionStatus == null ? "" : conversionStatus.targetFile);
            $("#conversion-log-file").text(conversionStatus == null ? "" : conversionStatus.logFile);

            if (conversionStatus != null && conversionStatus.statusInProgress) {
                $("#conversion-thumb").attr("src", "coverArt.view?id=${model.video.id}&auth=${model.video.hash}&size=120&offset=" + conversionStatus.progressSeconds);
                $("#conversion-status-progress").text(conversionStatus.progressString);
                updateInterval = 1000;
            } else {
                updateInterval = 2000;
            }
        }
        function startConversion() {
            var audioTrackId = null;
            if ($("#conversion-audio-track").length > 0) {
                audioTrackId = parseInt($("#conversion-audio-track").val());
            }
            var bitRate = parseInt($("#conversion-bit-rate").val());
            if (bitRate == 0) {
                bitRate = null;
            }
            console.log(bitRate);
            multiService.startVideoConversion(${model.video.id}, audioTrackId, bitRate, conversionStatusCallback);
        }
        function cancelConversion() {
            multiService.cancelVideoConversion(${model.video.id}, conversionStatusCallback);
        }
        function back() {
            if (document.referrer) {
                location.href = document.referrer;
            } else {
                history.back();
            }
        }
    </script>
</head>

<body class="mainframe bgcolor1" style="padding-bottom:0.5em" onload="init()">

<div class="ellipsis" style="margin-bottom:0.5em">
    <c:set var="musicFolder" value="${model.musicFolder}"/>
    <c:set var="ancestors" value="${model.ancestors}"/>
    <%@ include file="indexLink.jsp" %>
</div>

<h1><i class="fa fa-film fa-fw icon"></i>&nbsp;&nbsp;<fmt:message key="videoConverter.title"/></h1>

<p style="margin-top:1.5em;max-width:70%">
    <fmt:message key="videoConverter.intro"/>
</p>

<div style="margin: 2em 3em;">

    <table style="float:left; margin-right:5em">
        <tr>
            <td colspan="2">
                <img id="conversion-thumb" src="coverArt.view?id=${model.video.id}&auth=${model.video.hash}&size=120" height="120" width="213">
            </td>
        </tr>
        <tr>
            <td colspan="2" style="padding-top:0.5em; padding-bottom:1em">
                <div id="conversion-progressbar" style="width:100%; height:7px"></div>
            </td>
        </tr>
        <tr id="conversion-bit-rate-container">
            <td class="detail"><b><fmt:message key="personalsettings.bitrate"/></b></td>
            <td>
                <select id="conversion-bit-rate" style="width:100%">
                    <option value="0"><fmt:message key="common.default"/></option>
                    <c:forEach items="${model.bitRates}" var="bitRate">
                        <option value="${bitRate}">${bitRate} Kbps</option>
                    </c:forEach>
                </select>
            </td>
        </tr>

        <c:if test="${fn:length(model.audioTracks) gt 1}">
            <tr id="conversion-audio-track-container">
                <td class="detail"><b><fmt:message key="videoConverter.audiotrack"/></b></td>
                <td>
                    <select id="conversion-audio-track" style="width:100%">
                        <c:forEach items="${model.audioTracks}" var="track">
                            <option value="${track.id}">${track.languageName}</option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
        </c:if>

        <c:if test="${model.licenseInfo.licenseOrTrialValid}">
            <tr>
                <td colspan="2" style="text-align:center; padding-top:1em">
                    <input id="conversion-start" style="display:none; cursor:pointer" type="button" value="<fmt:message key="videoConverter.start"/>" onclick="startConversion()">
                    <input id="conversion-cancel" style="display:none; cursor:pointer" type="button" value="<fmt:message key="videoConverter.cancel"/>" onclick="cancelConversion()">
                </td>
            </tr>
        </c:if>
    </table>

    <table class="detail" style="float:left">
        <tr><td style="padding-right:1em"><b><fmt:message key="videoConverter.details.title"/></b></td><td>${model.video.title}</td></tr>
        <tr><td style="padding-right:1em"><b><fmt:message key="personalsettings.format"/></b></td><td>${model.video.format}</td></tr>
        <tr><td style="padding-right:1em"><b><fmt:message key="videoConverter.details.videocodec"/></b></td><td>${model.videoCodecs}</td></tr>
        <tr><td style="padding-right:1em"><b><fmt:message key="videoConverter.details.audiocodec"/></b></td><td>${model.audioCodecs}</td></tr>
        <tr><td style="padding-right:1em"><b><fmt:message key="personalsettings.duration"/></b></td><td>${model.video.durationString}</td></tr>
        <tr><td style="padding-right:1em"><b><fmt:message key="personalsettings.bitrate"/></b></td><td>${model.video.bitRate} Kbps</td></tr>
        <tr><td style="padding-right:1em"><b><fmt:message key="personalsettings.filesize"/></b></td><td><sub:formatBytes bytes="${model.video.fileSize}"/></td></tr>
        <tr><td style="padding-right:1em"><b><fmt:message key="videoConverter.details.dimension"/></b></td><td>${model.video.width} &times; ${model.video.height}</td></tr>
        <tr><td style="padding-right:1em"><b><fmt:message key="videoConverter.details.status"/></b></td><td>
            <span style="display:none" id="conversion-status-new"><fmt:message key="videoConverter.status.new"/></span>
            <span style="display:none" id="conversion-status-in-progress"><i class="fa fa-refresh fa-spin"></i>&nbsp;&nbsp;<fmt:message key="videoConverter.status.in_progress"/>
                (<span id="conversion-status-progress"></span>)</span>
            <span style="display:none" id="conversion-status-completed"><fmt:message key="videoConverter.status.completed"/></span>
            <span style="display:none" id="conversion-status-error"><fmt:message key="videoConverter.status.error"/></span>
        </td></tr>
        <tr><td style="padding-right:1em"><b><fmt:message key="videoConverter.details.targetfile"/></b></td><td><span id="conversion-target-file"></span></td></tr>
        <tr><td style="padding-right:1em"><b><fmt:message key="videoConverter.details.logfile"/></b></td><td><span id="conversion-log-file"></span></td></tr>
    </table>
</div>

<div style="clear:both"></div>

<c:set var="licenseInfo" value="${model.licenseInfo}"/>
<%@ include file="licenseNotice.jsp" %>

<p style="margin-top:2em; max-width:70%">
    <c:choose>
        <c:when test="${model.user.videoConversionRole}">
            <fmt:message key="videoConverter.info"/>
        </c:when>
        <c:otherwise>
            <span class="warning"><fmt:message key="videoConverter.notallowed"/></span>
        </c:otherwise>
    </c:choose>
</p>

<p>
    <i class="fa fa-chevron-left icon"></i>&nbsp;<a href="videoPlayer.view?id=${model.video.id}"><fmt:message key="common.back"/></a>

    <c:if test="${model.user.adminRole}">
        <span style="margin-left:3em">
            <i class="fa fa-cog fa-lg icon"></i>&nbsp;<a href="videoConversionSettings.view"><fmt:message key="top.settings"/></a>
        </span>
    </c:if>
</p>
</body>
</html>
