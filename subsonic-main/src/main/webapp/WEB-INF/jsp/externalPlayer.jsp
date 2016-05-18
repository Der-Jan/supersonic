<%--@elvariable id="model" type="java.util.Map"--%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>

<html>
<head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>

    <link type="text/css" rel="stylesheet" href="<c:url value="/script/flowplayer-6.0.5/skin/functional.css"/>">
    <script type="text/javascript" src="<c:url value="/script/flowplayer-6.0.5/flowplayer.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/flowplayer-6.0.5/flowplayer.hlsjs.min.js"/>"></script>

    <style>
        #overlay {
            position: absolute;
            z-index: 10;
            background-size: cover;
        }
    </style>

    <meta name="og:type" content="album"/>

    <c:if test="${not empty model.entries}">
        <sub:url value="/coverArt.view" var="coverArtUrl">
            <sub:param name="id" value="${model.entries[0].file.id}"/>
            <sub:param name="auth" value="${model.entries[0].file.hash}"/>
            <sub:param name="size" value="600"/>
        </sub:url>
        <meta name="og:title"
              content="${fn:escapeXml(model.entries[0].file.artist)} &mdash; ${fn:escapeXml(model.entries[0].file.albumName)}"/>
        <meta name="og:image" content="${model.redirectUrl}${coverArtUrl}"/>
    </c:if>

    <script type="text/javascript">
        var player;

        function init() {

            var playlist = [];
            var metadata = [];

            <c:forEach items="${model.entries}" var="entry" varStatus="loopStatus">
                <c:url value="${entry.file.audio or entry.streamable ? '/stream' : '/hls'}" var="streamUrl">
                    <c:param name="id" value="${entry.file.id}"/>
                    <c:param name="auth" value="${entry.file.hash}"/>
                    <c:param name="player" value="${model.player}"/>
                    <c:if test="${entry.file.video}">
                        <c:if test="${entry.streamable}">
                            <c:param name="format" value="raw"/>
                            <c:param name="converted" value="${entry.converted}"/>
                        </c:if>
                        <c:if test="${not entry.streamable}">
                            <c:param name="bitRate" value="1000"/>
                            <c:param name="bitRate" value="200"/>
                            <c:param name="bitRate" value="300"/>
                            <c:param name="bitRate" value="400"/>
                            <c:param name="bitRate" value="500"/>
                            <c:param name="bitRate" value="700"/>
                            <c:param name="bitRate" value="1200"/>
                            <c:param name="bitRate" value="1500"/>
                            <c:param name="bitRate" value="1700"/>
                            <c:param name="bitRate" value="2000"/>
                            <c:param name="bitRate" value="2500"/>
                            <c:param name="bitRate" value="3000"/>
                        </c:if>
                    </c:if>
                </c:url>
                <c:url value="/coverArt.view" var="coverUrl">
                    <c:param name="id" value="${entry.file.id}"/>
                    <c:param name="auth" value="${entry.file.hash}"/>
                    <c:param name="size" value="600"/>
                </c:url>

            metadata[${loopStatus.count - 1}] = {coverArt: "${coverUrl}", isVideo: ${entry.file.video}};

            playlist[${loopStatus.count - 1}] = {
                sources: [{ src: "${streamUrl}",
                            type: "${entry.contentType}",
                            subtitles:
                          <c:choose>
                              <c:when test="${entry.captions}">
                                    [{
                                         src: "../captions.view?id=${entry.file.id}&auth=${entry.file.hash}",
                                         srclang: "en",
                                         label: "Default",
                                         kind: "subtitles",
                                         "default": true
                                     }]
                              </c:when>
                              <c:otherwise>
                                    []
                              </c:otherwise>
                          </c:choose>
                          }]};

            </c:forEach>

            player = flowplayer("#player", {
                playlist: playlist,
                keyboard: false,
                ratio: false,
                fullscreen: true,
                embed: false,
                native_fullscreen: true
            });

            player.on("pause resume", function() {
                var i = player.video.index;
                $(".fa-circle-o-notch").hide();
                $("#spinner-" + i).show().toggleClass("fa-spin", player.playing);
                $("#overlay").css("background-image", metadata[i].isVideo ? "none" : "url(" + metadata[i].coverArt + ")");
            });

            $(window).resize(resizeOverlay);
            resizeOverlay();

            if (metadata.length > 0) {
                $("#overlay").css("background-image", "url(" + metadata[0].coverArt + ")");
            }
        }

        function resizeOverlay() {
            $("#overlay")
                    .width($("#player").width())
                    .height($("#player").height());
        }

    </script>

</head>

<body class="mainframe bgcolor2" style="padding-top:2em; padding-bottom:2em" onload="init();">

<div style="margin:auto;max-width:600px" class="dropshadow">
    <div style="padding: 1em 1em 2.5em 1em" class="bgcolor1">
        <h1>
            <c:choose>
                <c:when test="${empty model.share or empty model.entries}">
                    Sorry, the content is not available.
                </c:when>
                <c:otherwise>
                    ${empty model.share.description ? model.entries[0].file.artist : fn:escapeXml(model.share.description)}
                </c:otherwise>
            </c:choose>
        </h1>

        <div style="float:left;padding-right:1.5em">
            <h2 style="margin:0;">${empty model.share.description ? model.entries[0].file.albumName : fn:escapeXml(model.share.username)}</h2>
        </div>
        <div class="detail" style="float:right">Streaming by <a href="http://subsonic.org/" target="_blank"><b>Subsonic</b></a></div>
    </div>

    <div style="clear:both">
        <div id="overlay"></div>
        <div id="player" style="height: 300px;background-color: black"></div>
    </div>

    <table class="music">
        <c:forEach items="${model.entries}" var="entry" varStatus="loopStatus">
            <tr>
                <td class="truncate">
                    <i id="spinner-${loopStatus.count - 1}" class="fa fa-circle-o-notch fa-spin icon"
                       style="display:none; margin-right:0.5em"></i>
                    <span class="songTitle" style="cursor:pointer"><a onclick="player.play(${loopStatus.count - 1})">${fn:escapeXml(entry.file.name)}</a></span>
                </td>
                <td class="truncate detail">${fn:escapeXml(entry.file.albumName)}</td>
                <td class="truncate detail">${fn:escapeXml(entry.file.artist)}</td>
                <td class="fit rightalign detail">${entry.file.durationString}</td>
            </tr>
        </c:forEach>
    </table>
</div>

</body>
</html>
