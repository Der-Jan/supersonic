<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>
<html>
<head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
    <link type="text/css" rel="stylesheet" href="<c:url value="/script/flowplayer-6.0.5/skin/functional.css"/>">
    <link type="text/css" rel="stylesheet" href="<c:url value="/style/videoPlayer.css"/>">
    <script type="text/javascript" src="<c:url value="/dwr/engine.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/interface/starService.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/flowplayer-6.0.5/flowplayer.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/flowplayer-6.0.5/flowplayer.hlsjs.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/flowplayer-6.0.5/flowplayer.thumbnails.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/cast_sender-v1.js"/>"></script>
    <%@ include file="videoPlayerCast.jsp" %>

    <script type="text/javascript" language="javascript">
        function toggleStar(mediaFileId, element) {
            starService.star(mediaFileId, !$(element).hasClass("fa-star"));
            $(element).toggleClass("fa-star fa-star-o starred");
        }

        function keyboardShortcut(action) {
            if (action == "togglePlayPause") {
                if ($("#play").is(":visible")) {
                    $("#play").click();
                } else if ($("#pause").is(":visible")) {
                    $("#pause").click();
                }
            } else if (action == "volumeDown") {
                var volume = parseInt($("#volume-slider").slider("option", "value"));
                $("#volume-slider").slider("option", "value", Math.max(0, volume - 5));
                castPlayer.setVolume(false);
            } else if (action == "volumeUp") {
                var volume = parseInt($("#volume-slider").slider("option", "value"));
                $("#volume-slider").slider("option", "value", Math.min(100, volume + 5));
                castPlayer.setVolume(false);
            } else if (action == "seekForward" && $("#pause").is(":visible")) {
                var position = parseInt($("#progress-slider").slider("option", "value"));
                var duration = parseInt($("#progress-slider").slider("option", "max"));
                $("#progress-slider").slider("option", "value", Math.min(duration, position + duration / 100));
                castPlayer.seekMedia();
            } else if (action == "seekBackward" && $("#pause").is(":visible")) {
                var position = parseInt($("#progress-slider").slider("option", "value"));
                var duration = parseInt($("#progress-slider").slider("option", "max"));
                $("#progress-slider").slider("option", "value", Math.max(0, position - duration / 100));
                castPlayer.seekMedia();
            } else if (action == "star") {
                $("#star").click();
            }
        }

    </script>
</head>

<body class="mainframe bgcolor1" >

<c:set var="licenseInfo" value="${model.licenseInfo}"/>
<%@ include file="licenseNotice.jsp" %>

<c:if test="${licenseInfo.licenseOrTrialValid}">
    <div>
        <div id="overlay">
            <i class="material-icons" style="font-size:30px">cast_connected</i>&nbsp;&nbsp;<span><fmt:message key="videoPlayer.chromecast"/></span>
        </div>
        <div id="localPlayer" style="width:100%; height:calc(100vh - 125px)"></div>
        <div id="media-control" class="bgcolor2">
            <div class="ellipsis" style="flex-grow:1">
                <div id="progress-slider"></div>

                <div class="ellipsis" style="display:flex; align-items:center; margin-left:10px">
                    <div class="ellipsis" style="flex:1">
                        <div id="title" class="ellipsis">
                            <i id="star" class="fa ${not empty model.video.starredDate ? 'fa-star starred' : 'fa-star-o'} fa-lg clickable"
                               onclick="toggleStar(${model.video.id}, this)" style="padding-right:0.25em"></i>&nbsp;${fn:escapeXml(model.video.title)}
                        </div>

                        <c:if test="${empty model.position}">
                            <div class="detail ellipsis" style="margin-top:0.4em">
                                <a href="artists.view?musicFolderId=${model.musicFolder.id}">${fn:escapeXml(model.musicFolder.name)}</a>
                                <c:forEach items="${model.ancestors}" var="ancestor">
                                    &nbsp;&bull;&nbsp; <a href="main.view?id=${ancestor.id}">${fn:escapeXml(ancestor.name)}</a>
                                </c:forEach>
                            </div>
                        </c:if>
                    </div>

                    <span id="play" class="fa-stack fa-lg">
                        <i class="fa fa-circle fa-stack-2x fa-inverse"></i>
                        <i class="fa fa-play-circle fa-stack-2x"></i>
                    </span>
                    <span id="pause" class="fa-stack fa-lg">
                        <i class="fa fa-circle fa-stack-2x fa-inverse"></i>
                        <i class="fa fa-pause-circle fa-stack-2x"></i>
                    </span>
                    <span id="buffer" class="fa-stack fa-lg">
                        <i class="fa fa-circle fa-stack-2x"></i>
                        <i class="fa fa-refresh fa-stack-1x fa-inverse fa-spin"></i>
                    </span>
                    <div style="flex:1;">
                        <div style="display:flex; align-items: center">
                            <span style="flex-grow:1"></span>
                            <i id="cast-idle" class="material-icons" title="<fmt:message key="videoPlayer.cast"/>">cast</i>
                            <i id="cast-active" class="material-icons" title="<fmt:message key="videoPlayer.cast"/>">cast_connected</i>
                            <i id="cc-on" class="material-icons" title="<fmt:message key="videoPlayer.subtitles"/>">closed_caption</i>
                            <i id="cc-off" class="material-icons" title="<fmt:message key="videoPlayer.subtitles"/>">closed_caption</i>
                            <i id="fullscreen" class="material-icons" title="<fmt:message key="videoPlayer.fullscreen"/>">fullscreen</i>
                            <i id="new-window" class="material-icons" title="<fmt:message key="videoPlayer.newwindow"/>">open_in_new</i>
                            <i id="share" class="material-icons" title="<fmt:message key="main.sharealbum"/>">share</i>
                            <i id="download" class="material-icons" title="<fmt:message key="common.download"/>">file_download</i>

                            <c:if test="${fn:length(model.audioTracks) gt 1}">
                                <select id="audio-track">
                                    <c:forEach items="${model.audioTracks}" var="track">
                                        <option value="${track.id}">${track.languageName}</option>
                                    </c:forEach>
                                </select>
                            </c:if>

                            <span id="progress-and-duration" class="detail">
                                <span id="progress">0:00</span> /
                                <span id="duration">0:00</span>
                            </span>
                            <i id="audio-on" class="fa fa-volume-up fa-fw"></i>
                            <i id="audio-off" class="fa fa-volume-off fa-fw"></i>
                            <div id="volume-slider"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <c:if test="${not model.converted and not model.streamable and model.user.videoConversionRole and empty model.position}">
            <p class="detail space-above">
                <i class="fa fa-lightbulb-o fa-lg fa-fw icon"></i>
                <a href="videoConverter.view?id=${model.video.id}"><fmt:message key="videoPlayer.optimize"/></a>
            </p>
        </c:if>
    </div>

    <%--<div id="debug"></div>--%>

    <script type="text/javascript">
        var castPlayer = new CastPlayer();
    </script>
</c:if>

</body>
</html>
