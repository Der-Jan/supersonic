<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>

    <style>
        #overlay {
            position: absolute;
            z-index: 10;
            font-size: 24px;
            color: white;
        }
        #overlay-text {
            padding-top: 20px;
            padding-left: 20px;
        }
    </style>

    <link type="text/css" rel="stylesheet" href="<c:url value="/script/flowplayer-6.0.5/skin/functional.css"/>">
    <script type="text/javascript" src="<c:url value="/script/flowplayer-6.0.5/flowplayer.min.js"/>"></script>

    <script type="text/javascript">
        var player;

        function init() {

            var playlist = [];
            var metadata = [];

            <c:forEach items="${model.stations}" var="station" varStatus="loopStatus">
            metadata[${loopStatus.count - 1}] = {stationName: "${fn:escapeXml(station.name)}"};

            playlist[${loopStatus.count - 1}] = {
                sources: [{ src: "internetRadioStream.view?id=${station.id}",
                    type: "audio/mpeg",
                }]};

            </c:forEach>

            player = flowplayer("#player", {
                playlist: playlist,
                live: true,
                keyboard: false,
                ratio: false,
                fullscreen: false,
                embed: false,
                native_fullscreen: false
            });

            player.on("pause resume", function() {
                var i = player.video.index;
                $(".fa-circle-o-notch").hide();
                $("#spinner-" + i).show().toggleClass("fa-spin", player.playing);
                $("#overlay-text").html(metadata[i].stationName);
            });

            $(window).resize(resizeOverlay);
            resizeOverlay();
        }

        function play(index) {
            player.play(index);
        }

        function resizeOverlay() {
            $("#overlay")
                    .width($("#player").width())
                    .height($("#player").height());
        }

    </script>
</head>

<body class="mainframe bgcolor1" onload="init()">

<h1 style="padding-bottom: 1em">
    <i class="fa fa-globe fa-lg icon"></i>&nbsp;&nbsp;<fmt:message key="left.radio"/>
</h1>

<c:if test="${empty model.stations}">
    <p><em><fmt:message key="internetradio.empty"/></em></p>
</c:if>

<c:if test="${not empty model.stations}">

    <div style="width:600px; margin-left:auto; margin-right:auto;" class="dropshadow">
        <div>
            <div id="overlay">
                <div id="overlay-text"></div>
            </div>
            <div id="player" style="height:150px; background-color: #353535"></div>
        </div>

        <table class="music">
            <c:forEach items="${model.stations}" var="station" varStatus="loopStatus">
                <tr>
                    <td class="truncate" style="padding-left:1em">
                        <i id="spinner-${loopStatus.count - 1}" class="fa fa-circle-o-notch fa-spin icon"
                           style="display:none; margin-right:0.5em"></i>
                        <span class="songTitle" style="cursor:pointer"><a onclick="play(${loopStatus.count - 1})">${fn:escapeXml(station.name)}</a></span>
                    </td>
                    <td class="truncate">
                        <c:if test="${not empty station.homepageUrl}">
                            <a href="${station.homepageUrl}" target="_blank">${station.homepageUrl}</a>
                        </c:if>
                    </td>
                    <td class="fit" style="padding-right: 1em">
                        <i class="material-icons clickable" onclick="window.open('${station.streamUrl}', '_blank');">open_in_new</i>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>
</c:if>

<c:if test="${model.user.adminRole}">
    <div style="padding-top:2em;">
        <i class="fa fa-cog fa-lg fa-fw icon"></i>&nbsp;&nbsp;<a href="internetRadioSettings.view"><fmt:message key="internetradio.settings"/></a>
    </div>
</c:if>

<div style="padding-top:2em"></div>

</body>
</html>
