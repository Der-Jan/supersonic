<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>
<%--<!DOCTYPE html>--%>
<html>
<head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
    <link rel="stylesheet" href="script/flowplayer-6.0.5/skin/minimalist.css">
    <script src="script/flowplayer-6.0.5/flowplayer.min.js"></script>

    <script src="script/flowplayer-6.0.5/flowplayer.hlsjs.min.js"></script>


</head>

<body>

<div id="player" style="width:25%"></div>

<script>
    var api = flowplayer("#player", {
        clip: {
            sources: [ { type: "application/x-mpegurl",
                           src:  "hls?id=6859&player=6&auth=1992788870&bitRate=4000" }
            ]
        }
    });

    // Is API ready?
    console.log(api.ready);
    console.log(api.video);

    api.on("pause", function() {
        console.log("pause");
    });

    // Handle error event. With error code.

</script>

</body>
</html>