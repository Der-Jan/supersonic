<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
    <link type="text/css" rel="stylesheet" href="<c:url value="/script/webfx/luna.css"/>">
    <script type="text/javascript" src="<c:url value="/script/scripts-2.0.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/interface/nowPlayingService.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/interface/playQueueService.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/interface/playlistService.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/engine.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/dwr/util.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/jwplayer-7.2.4/jwplayer.js"/>"></script>
    <script type="text/javascript">jwplayer.key="fnCY1zPzsH/DE/Uo+pvsBes6gTdfOCcLCCnD6g==";</script>
    <script type="text/javascript" src="<c:url value="/script/cast_sender-v1.js"/>"></script>
    <%@ include file="playQueueCast.jsp" %>
    <link type="text/css" rel="stylesheet" href="<c:url value="/script/webfx/luna.css"/>">
    <style type="text/css">
        .ui-slider .ui-slider-handle {
            width: 12px; height: 12px; cursor: pointer;
            background: #E65100;
            display:none;
            border:none;
        }
        .ui-slider {
            cursor: pointer;
            border:none;
        }
        .ui-slider-range-min {
            background: #E65100;
        }
        .ui-slider-handle:focus {
            outline:none;
        }
        #startButton, #stopButton {
            cursor:pointer; font-size:30px; color:#E65100
        }
        #bufferButton {
            font-size:30px; color:#E65100
        }
        #previousButton, #nextButton {
            cursor:pointer; font-size:18px; padding:10px; margin-left:10px; margin-right:10px
        }
        #muteOn, #muteOff {
            cursor:pointer; font-size:20px; padding:8px
        }
        #songName {
            cursor:pointer; font-weight: 500;
        }
        #artistName {
            cursor:pointer; font-weight: 300;
        }
        #coverArt {
            cursor:pointer; width:80px; height:80px
        }
    </style>
</head>

<body class="bgcolor2 playlistframe" onload="init()">

<span id="dummy-animation-target" style="max-width:50px;display:none" class="bgcolor1"></span>

<script type="text/javascript" language="javascript">
    var songs = null;
    var currentStreamUrl = null;
    var repeatEnabled = false;
    var castPlayer = new CastPlayer();
    var jwPlayer;
    var jukeboxPlayer = false;
    var ignore = false;

    function init() {

        jukeboxPlayer = ${model.player.jukebox};
        initMouseListener();

        dwr.engine.setErrorHandler(null);

        $("#dialog-select-playlist").dialog({resizable: true, height: 220, autoOpen: false,
            buttons: {
                "<fmt:message key="common.cancel"/>": function() {
                    $(this).dialog("close");
                }
            }});

        $("#progress").slider({max: 0, animate: "fast", range: "min"});
        $("#volume").slider({max: 100, value: 50, animate: "fast", range: "min"});
        $("#volume").on("slidestop", onVolumeChanged);
        $("#progress").on("slidestop", onProgressChanged);
        $(".ui-slider").css("background", $("#dummy-animation-target").css("background-color"));

        $("#playlistBody").sortable({
            stop: function(event, ui) {
                var indexes = [];
                $("#playlistBody").children().each(function() {
                    var id = $(this).attr("id").replace("pattern", "");
                    if (id.length > 0) {
                        indexes.push(parseInt(id) - 1);
                    }
                });
                onRearrange(indexes);
            },
            cursor: "move",
            axis: "y",
            containment: "parent",
            helper: function(e, tr) {
                var originals = tr.children();
                var trclone = tr.clone();
                trclone.children().each(function(index) {
                    // Set cloned cell sizes to match the original sizes
                    $(this).width(originals.eq(index).width());
                    $(this).css("maxWidth", originals.eq(index).width());
                    $(this).css("border-top", "1px solid black");
                    $(this).css("border-bottom", "1px solid black");
                });
                return trclone;
            }
        });

        <c:if test="${model.player.web}">createPlayer();</c:if>

        if (!jwPlayer) {
            startTimer();
            $("#progress").hide();
            $("#progress-and-duration").hide();
        }

        getPlayQueue();
    }

    function initMouseListener() {
        $(window).mouseleave(function (event) {
            if (event.clientY < 30) {
                setFrameHeight(95);
                $(".ui-slider-handle").fadeOut();
            }
        });

        $(window).mouseenter(function () {
            var height = $("body").height() + 25;
            height = Math.min(height, window.top.innerHeight * 0.8);
            setFrameHeight(height);
            $(".ui-slider-handle").fadeIn();
        });
    }

    function setFrameHeight(height) {
        <c:if test="${model.autoHide}">
        <%-- Disable animation in Chrome. It stopped working in Chrome 44. --%>
        var duration = navigator.userAgent.indexOf("Chrome") != -1 ? 0 : 400;

        $("#dummy-animation-target").stop();
        $("#dummy-animation-target").animate({"max-width": height}, {
            step: function (now, fx) {
                top.document.getElementById("playQueueFrameset").rows = "42,*," + now;
            },
            duration: duration
        });
        </c:if>
    }

    function startTimer() {
        <!-- Periodically check if the current song has changed. -->
        nowPlayingService.getNowPlayingForCurrentPlayer(nowPlayingCallback);
        setTimeout("startTimer()", 10000);
    }

    function nowPlayingCallback(nowPlayingInfo) {
        if (nowPlayingInfo != null && nowPlayingInfo.streamUrl != currentStreamUrl) {
            getPlayQueue();
            if (!jwPlayer) {
                currentStreamUrl = nowPlayingInfo.streamUrl;
                updateCurrentImage();
            }
        }
    }
    function createPlayer() {
        jwPlayer = jwplayer("jwplayer");
        jwPlayer.setup({
            file: "foo.mp3",
            height: 0,
            width: 0
        });

        jwPlayer.on("complete", function() {onNext(repeatEnabled)});
        jwPlayer.on("idle", function() {updateControls()});
        jwPlayer.on("buffer", function() {updateControls()});
        jwPlayer.on("play", function() {updateControls()});
        jwPlayer.on("pause", function() {updateControls()});
        jwPlayer.on("mute", function() {updateControls()});
        jwPlayer.on("time", function(event) {updateProgressBar(event.position, event.duration)});
        $("#volume").slider("option", "value", jwPlayer.getVolume());
    }
    function updateControls() {
        var state = jwPlayer.getState();
        var playing = state == "playing";
        var buffering = state == "buffering";
        $("#startButton").toggle(!playing && !buffering);
        $("#stopButton").toggle(playing && !buffering);
        $("#bufferButton").toggle(buffering);
        $(".fa-circle-o-notch").toggleClass("fa-spin", playing);

        var muted = jwPlayer.getMute();
        $("#muteOn").toggle(!muted);
        $("#muteOff").toggle(muted);
    }
    function updateProgressBar(position, duration) {
        $("#progress").slider("option", "max", Math.round(duration * 1000));
        $("#progress").slider("option", "value", Math.round(position * 1000));
        $("#progress-text").html(formatDuration(Math.round(position)));
        $("#duration-text").html(formatDuration(Math.round(duration)));
    }
    function formatDuration(duration) {
        var hours = Math.floor(duration / 3600);
        duration = duration % 3600;
        var minutes = Math.floor(duration / 60);
        var seconds = Math.floor(duration % 60);

        var result = "";
        if (hours > 0) {
            result += hours + ":";
            if (minutes < 10) {
                result += "0";
            }
        }
        result += minutes + ":";
        if (seconds < 10) {
            result += "0";
        }
        result += seconds;

        return result;
    }
    function getPlayQueue() {
        playQueueService.getPlayQueue(playQueueCallback);
    }

    function onClear() {
        var ok = true;
    <c:if test="${model.partyMode}">
        ok = confirm("<fmt:message key="playlist.confirmclear"/>");
    </c:if>
        if (ok) {
            playQueueService.clear(playQueueCallback);
        }
    }
    function onStart() {
        if (castPlayer.castSession) {
         castPlayer.playCast();
        } else if (jwPlayer) {
            if (jwPlayer.getPlaylistItem().file == "foo.mp3") {
                skip(0);
            } else {
                jwPlayer.play(true);
            }

        } else {
            playQueueService.start(playQueueCallback);
        }
    }
    function onStop() {
        if (castPlayer.castSession) {
            castPlayer.pauseCast();
        } else if (jwPlayer) {
            jwPlayer.pause(true);
        } else {
            playQueueService.stop(playQueueCallback);
        }
    }
    function onVolumeChanged() {
        var value = parseInt($("#volume").slider("option", "value"));
        if (castPlayer.castSession) {
            castPlayer.setCastVolume(value / 100, false);
        } else if (jwPlayer) {
            jwPlayer.setVolume(value);
        } else if (jukeboxPlayer) {
            playQueueService.setGain(value / 100);
        }
    }
    function onProgressChanged() {
        var value = parseInt($("#progress").slider("option", "value") / 1000);
        if (jwPlayer) {
            jwPlayer.seek(value);
        }
    }
    function onMute(mute) {
        if (castPlayer.castSession) {
            castPlayer.castMute(mute);
        } else if (jwPlayer) {
            jwPlayer.setMute(mute);
        }
    }
    function onSkip(index) {
        if (jwPlayer) {
            skip(index);
        } else {
//            currentStreamUrl = songs[index].streamUrl;
            playQueueService.skip(index, playQueueCallback);
        }
    }
    function onNext(wrap) {
        var index = parseInt(getCurrentSongIndex()) + 1;
        if (wrap) {
            index = index % songs.length;
        }
        skip(index);
    }
    function onPrevious() {
        skip(parseInt(getCurrentSongIndex()) - 1);
    }
    function onPlay(id) {
        playQueueService.play(id, playQueueCallback);
    }
    function onPlayShuffle(albumListType, offset, size, genre, decade) {
        playQueueService.playShuffle(albumListType, offset, size, genre, decade, playQueueCallback);
    }
    function onPlayPlaylist(id, index) {
        playQueueService.playPlaylist(id, index, playQueueCallback);
    }
    function onPlayTopSong(id, index) {
        playQueueService.playTopSong(id, index, playQueueCallback);
    }
    function onPlayPodcastChannel(id) {
        playQueueService.playPodcastChannel(id, playQueueCallback);
    }
    function onPlayPodcastEpisode(id) {
        playQueueService.playPodcastEpisode(id, playQueueCallback);
    }
    function onPlayNewestPodcastEpisode(index) {
        playQueueService.playNewestPodcastEpisode(index, playQueueCallback);
    }
    function onPlayStarred() {
        playQueueService.playStarred(playQueueCallback);
    }
    function onPlayRandom(id, count) {
        playQueueService.playRandom(id, count, playQueueCallback);
    }
    function onPlaySimilar(id, count) {
        playQueueService.playSimilar(id, count, playQueueCallback);
    }
    function onAdd(id) {
        playQueueService.add(id, playQueueCallback);
    }
    function onAddNext(id) {
        playQueueService.addAt(id, getCurrentSongIndex() + 1, playQueueCallback);
    }
    function onShuffle() {
        playQueueService.shuffle(playQueueCallback);
    }
    function onStar(index) {
        playQueueService.toggleStar(index, playQueueCallback);
    }
    function onRemove(index) {
        playQueueService.remove(index, playQueueCallback);
    }
    function onRemoveSelected() {
        var indexes = new Array();
        var counter = 0;
        for (var i = 0; i < songs.length; i++) {
            var index = i + 1;
            if ($("#songIndex" + index).is(":checked")) {
                indexes[counter++] = i;
            }
        }
        playQueueService.removeMany(indexes, playQueueCallback);
    }

    function onRearrange(indexes) {
        playQueueService.rearrange(indexes, playQueueCallback);
    }
    function onToggleRepeat() {
        playQueueService.toggleRepeat(playQueueCallback);
    }
    function onUndo() {
        playQueueService.undo(playQueueCallback);
    }
    function onSortByTrack() {
        playQueueService.sortByTrack(playQueueCallback);
    }
    function onSortByArtist() {
        playQueueService.sortByArtist(playQueueCallback);
    }
    function onSortByAlbum() {
        playQueueService.sortByAlbum(playQueueCallback);
    }
    function onSavePlayQueue() {
        var positionMillis = jwPlayer ? Math.round(1000.0 * jwPlayer.getPosition()) : 0;
        playQueueService.savePlayQueue(getCurrentSongIndex(), positionMillis);
        $().toastmessage("showSuccessToast", "<fmt:message key="playlist.toast.saveplayqueue"/>");
    }
    function onLoadPlayQueue() {
        playQueueService.loadPlayQueue(playQueueCallback);
    }
    function onSavePlaylist() {
        playlistService.createPlaylistForPlayQueue(function (playlistId) {
            top.main.location.href = "playlist.view?id=" + playlistId;
            $().toastmessage("showSuccessToast", "<fmt:message key="playlist.toast.saveasplaylist"/>");
        });
    }
    function onAppendPlaylist() {
        playlistService.getWritablePlaylists(playlistCallback);
    }
    function playlistCallback(playlists) {
        $("#dialog-select-playlist-list").empty();
        for (var i = 0; i < playlists.length; i++) {
            var playlist = playlists[i];
            $("<p class='dense'><b><a href='#' onclick='appendPlaylist(" + playlist.id + ")'>" + escapeHtml(playlist.name)
                    + "</a></b></p>").appendTo("#dialog-select-playlist-list");
        }
        $("#dialog-select-playlist").dialog("open");
    }
    function appendPlaylist(playlistId) {
        $("#dialog-select-playlist").dialog("close");

        var mediaFileIds = new Array();
        for (var i = 0; i < songs.length; i++) {
            if ($("#songIndex" + (i + 1)).is(":checked")) {
                mediaFileIds.push(songs[i].id);
            }
        }
        playlistService.appendToPlaylist(playlistId, mediaFileIds, function (){
            top.main.location.href = "playlist.view?id=" + playlistId;
            $().toastmessage("showSuccessToast", "<fmt:message key="playlist.toast.appendtoplaylist"/>");
        });
    }

    function playQueueCallback(playQueue) {
        songs = playQueue.entries;
        repeatEnabled = playQueue.repeatEnabled;

        if ($("#toggleRepeat")) {
            var text = repeatEnabled ? "<fmt:message key="playlist.repeat_on"/>" : "<fmt:message key="playlist.repeat_off"/>";
            $("#toggleRepeat").html(text);
        }

        if (songs.length == 0) {
            $("#songCountAndDuration").html("");
            $("#empty").show();
        } else {
            $("#songCountAndDuration").html(songs.length + " <fmt:message key="playlist2.songs"/>&nbsp;&nbsp;&bull;&nbsp;&nbsp;" + playQueue.durationAsString);
            $("#empty").hide();
        }

        // Delete all the rows except for the "pattern" row
        dwr.util.removeAllRows("playlistBody", { filter:function(tr) {
            return (tr.id != "pattern");
        }});

        // Create a new set cloned from the pattern row
        for (var i = 0; i < songs.length; i++) {
            var song  = songs[i];
            var id = i + 1;
            dwr.util.cloneNode("pattern", { idSuffix:id });
            if ($("#trackNumber" + id)) {
                $("#trackNumber" + id).html(song.trackNumber);
            }
            $("#starSong" + id).addClass(song.starred ? "fa-star starred" : "fa-star-o");
            if ($("#title" + id)) {
                $("#title" + id).html(song.title);
                $("#title" + id).attr("title", song.title);
            }
            if ($("#titleUrl" + id)) {
                $("#titleUrl" + id).html(song.title);
                $("#titleUrl" + id).attr("title", song.title);
                $("#titleUrl" + id).click(function () {onSkip(this.id.substring(8) - 1)});
            }
            if ($("#album" + id)) {
                $("#album" + id).html(song.album);
                $("#album" + id).attr("title", song.album);
                $("#albumUrl" + id).attr("href", song.albumUrl);
            }
            if ($("#artist" + id)) {
                $("#artist" + id).html(song.artist);
                $("#artist" + id).attr("title", song.artist);
            }
            if ($("#genre" + id)) {
                $("#genre" + id).html(song.genre);
            }
            if ($("#year" + id)) {
                $("#year" + id).html(song.year);
            }
            if ($("#bitRate" + id)) {
                $("#bitRate" + id).html(song.bitRate);
            }
            if ($("#duration" + id)) {
                $("#duration" + id).html(song.durationAsString);
            }
            if ($("#format" + id)) {
                $("#format" + id).html(song.format);
            }
            if ($("#fileSize" + id)) {
                $("#fileSize" + id).html(song.fileSize);
            }

            // Note: show() method causes page to scroll to top.
            $("#pattern" + id).css("display", "table-row");
        }

        if (playQueue.sendM3U) {
            parent.frames.main.location.href="play.m3u?";
        }

//        todo
//        var jukeboxVolume = $("#jukeboxVolume");
//        if (jukeboxVolume) {
//            jukeboxVolume.slider("option", "value", Math.floor(playQueue.gain * 100));
//        }

        if (jwPlayer) {
            triggerPlayer(playQueue.startPlayerAt, playQueue.startPlayerAtPosition);
        } else {
            $("#startButton").toggle(!playQueue.stopEnabled);
            $("#stopButton").toggle(playQueue.stopEnabled);
        }
    }

    function triggerPlayer(index, positionMillis) {
        skip(index);
        if (positionMillis != 0) {
            jwPlayer.seek(positionMillis / 1000);
        }
        updateCurrentImage();
        if (songs.length == 0) {
            jwPlayer.stop();
            jwPlayer.load({file:"foo.mp3"});
            updateCoverArt(null);
            updateProgressBar(0, 0);
        }
    }

    function updateCoverArt(song) {
        var showAlbum = function () {
            parent.frames.main.location.href = "main.view?id=" + song.id
        };
        $("#coverArt").attr("src", song ? "coverArt.view?id=" + song.id + "&size=80" : "");
        $("#songName").text(song ? song.title : "");
        $("#artistName").text(song ? song.artist : "");
        $("#songName").off("click");
        $("#artistName").off("click");
        $("#coverArt").off("click");
        if (song) {
            $("#songName").click(showAlbum);
            $("#artistName").click(showAlbum);
            $("#coverArt").click(showAlbum);
        }
    }

    function skip(index, position) {
        if (index < 0 || index >= songs.length) {
            return;
        }

        var song = songs[index];
        currentStreamUrl = song.streamUrl;
        updateCurrentImage();

        if (castPlayer.castSession) {
            castPlayer.loadCastMedia(song, position);
        } else if (jwPlayer) {
            jwPlayer.load({
                file: song.streamUrl,
                type: song.format
            });
            jwplayer().play();
            console.log(song.streamUrl);
        } else if (jukeboxPlayer) {
            console.log("TODO: Update jukebox");
        }

        updateWindowTitle(song);
        updateCoverArt(song);

        if (${model.notify}) {
            showNotification(song);
        }
    }

    function updateWindowTitle(song) {
        top.document.title = song.title + " - " + song.artist + " - Subsonic";
    }

    function showNotification(song) {
        if (!("Notification" in window)) {
            return;
        }
        if (Notification.permission === "granted") {
            createNotification(song);
        }
        else if (Notification.permission !== 'denied') {
            Notification.requestPermission(function (permission) {
                Notification.permission = permission;
                if (permission === "granted") {
                    createNotification(song);
                }
            });
        }
    }

    function createNotification(song) {
        var n = new Notification(song.title, {
            tag: "subsonic",
            body: song.artist + " - " + song.album,
            icon: "coverArt.view?id=" + song.id + "&size=110"
        });
        n.onshow = function() {
            setTimeout(function() {n.close()}, 5000);
        }
    }

    function updateCurrentImage() {
        for (var i = 0; i < songs.length; i++) {
            var song  = songs[i];
            var id = i + 1;
            var image = $("#currentImage" + id);

            if (image) {
                image.toggle(song.streamUrl == currentStreamUrl);
            }
        }
    }

    function getCurrentSongIndex() {
        for (var i = 0; i < songs.length; i++) {
            if (songs[i].streamUrl == currentStreamUrl) {
                return i;
            }
        }
        return -1;
    }

    <!-- actionSelected() is invoked when the users selects from the "More actions..." combo box. -->
    function actionSelected(id) {
        var selectedIndexes = getSelectedIndexes();
        if (id == "top") {
            return;
        } else if (id == "savePlayQueue") {
            onSavePlayQueue();
        } else if (id == "loadPlayQueue") {
            onLoadPlayQueue();
        } else if (id == "savePlaylist") {
            onSavePlaylist();
        } else if (id == "downloadPlaylist") {
            location.href = "download.view?player=${model.player.id}";
        } else if (id == "sharePlaylist") {
            parent.frames.main.location.href = "createShare.view?player=${model.player.id}&" + getSelectedIndexes();
        } else if (id == "sortByTrack") {
            onSortByTrack();
        } else if (id == "sortByArtist") {
            onSortByArtist();
        } else if (id == "sortByAlbum") {
            onSortByAlbum();
        } else if (id == "selectAll") {
            selectAll(true);
        } else if (id == "selectNone") {
            selectAll(false);
        } else if (id == "removeSelected") {
            onRemoveSelected();
        } else if (id == "download" && selectedIndexes != "") {
            location.href = "download.view?player=${model.player.id}&" + selectedIndexes;
        } else if (id == "appendPlaylist" && selectedIndexes != "") {
            onAppendPlaylist();
        }
        $("#moreActions").prop("selectedIndex", 0);
    }

    function getSelectedIndexes() {
        var result = "";
        for (var i = 0; i < songs.length; i++) {
            if ($("#songIndex" + (i + 1)).is(":checked")) {
                result += "i=" + i + "&";
            }
        }
        return result;
    }

    function selectAll(b) {
        for (var i = 0; i < songs.length; i++) {
            if (b) {
                $("#songIndex" + (i + 1)).attr("checked", "checked");
            } else {
                $("#songIndex" + (i + 1)).removeAttr("checked");
            }
        }
    }

</script>

<div class="bgcolor2" style="position:fixed;bottom:0;width:100%;z-index: 2">
    <div id="jwplayer"></div>

    <table border="0" style="width:100%;padding-right:20px;padding-top:5px;padding-bottom:0px">
        <tr>
            <td rowspan="2"><img id="coverArt"></td>
            <td colspan="9" style="padding-left:20px">
                <div id="progress" style="width:100%;height:3px"></div>
            </td>
        </tr>
        <tr>
            <td style="padding-left:20px;width:50%">
                <div id="songName"></div>
                <div id="artistName"></div>
            </td>
            <td>
                <i id="previousButton" class="fa fa-step-backward" onclick="onPrevious()"></i>
            </td>
            <td>
                <span id="startButton" class="fa-stack fa-lg" onclick="onStart()">
                    <i class="fa fa-circle fa-stack-2x fa-inverse"></i>
                    <i class="fa fa-play-circle fa-stack-2x"></i>
                </span>
                <span id="stopButton" class="fa-stack fa-lg" onclick="onStop()" style="display:none">
                    <i class="fa fa-circle fa-stack-2x fa-inverse"></i>
                    <i class="fa fa-pause-circle fa-stack-2x"></i>
                </span>
                <span id="bufferButton" class="fa-stack fa-lg" style="display:none">
                    <i class="fa fa-circle fa-stack-2x"></i>
                    <i class="fa fa-refresh fa-stack-1x fa-inverse fa-spin"></i>
                </span>
            </td>
            <td>
                <i id="nextButton" class="fa fa-step-forward" onclick="onNext(repeatEnabled)"></i>
            </td>
            <td style="text-align:center;padding-left:20px">
                <img id="castOn" src="<spring:theme code="castIdleImage"/>" onclick="castPlayer.launchCastApp()" style="cursor:pointer;display:none">
                <img id="castOff" src="<spring:theme code="castActiveImage"/>" onclick="castPlayer.stopCastApp()" style="cursor:pointer;display:none">
            </td>
            <td style="width:50%;padding-right:20px">
                <div id="progress-and-duration" class="detail" style="text-align:right">
                    <span id="progress-text">0:00</span> /
                    <span id="duration-text">0:00</span>
                </div>
            </td>
            <td>
                <i id="muteOn" class="fa fa-volume-up fa-fw" onclick="onMute(true)"></i>
                <i id="muteOff" class="fa fa-volume-off fa-fw" onclick="onMute(false)" style="display:none"></i>
            </td>
            <td>
                <div id="volume" style="width:100px;height:3px"></div>
            </td>
        </tr>
    </table>
</div>

<h2 style="float:left"><fmt:message key="playlist.more.playlist"/></h2>
<h2 id="songCountAndDuration" style="float:right;padding-right:1em"></h2>
<div style="clear:both"></div>
<p id="empty"><em><fmt:message key="playlist.empty"/></em></p>

<table class="music indent" style="cursor:pointer">
    <tbody id="playlistBody">
        <tr id="pattern" style="display:none;margin:0;padding:0;border:0">
            <td class="fit">
                <i id="starSong" class="fa clickable" onclick="onStar(this.id.substring(8) - 1)"></i>
            </td>
            <td class="fit">
                <i id="removeSong" class="fa fa-remove clickable icon" onclick="onRemove(this.id.substring(10) - 1)" title="<fmt:message key="playlist.remove"/>"></i>
            </td>
            <td class="fit"><input type="checkbox" class="checkbox" id="songIndex"></td>
            <c:if test="${model.visibility.trackNumberVisible}">
                <td class="fit rightalign"><span class="detail" id="trackNumber">1</span></td>
            </c:if>

            <td class="truncate">
                <i id="currentImage" class="fa fa-circle-o-notch fa-spin icon" style="display:none;margin-right:0.5em"></i>
                <c:choose>
                    <c:when test="${model.player.externalWithPlaylist}">
                        <span id="title" class="songTitle">Title</span>
                    </c:when>
                    <c:otherwise>
                        <span class="songTitle"><a id="titleUrl" href="javascript:void(0)">Title</a></span>
                    </c:otherwise>
                </c:choose>
            </td>

            <c:if test="${model.visibility.albumVisible}">
                <td class="truncate"><a id="albumUrl" target="main"><span id="album" class="detail">Album</span></a></td>
            </c:if>
            <c:if test="${model.visibility.artistVisible}">
                <td class="truncate"><span id="artist" class="detail">Artist</span></td>
            </c:if>
            <c:if test="${model.visibility.genreVisible}">
                <td class="truncate"><span id="genre" class="detail">Genre</span></td>
            </c:if>
            <c:if test="${model.visibility.yearVisible}">
                <td class="fit rightalign"><span id="year" class="detail">Year</span></td>
            </c:if>
            <c:if test="${model.visibility.formatVisible}">
                <td class="fit rightalign"><span id="format" class="detail">Format</span></td>
            </c:if>
            <c:if test="${model.visibility.fileSizeVisible}">
                <td class="fit rightalign"><span id="fileSize" class="detail">Format</span></td>
            </c:if>
            <c:if test="${model.visibility.durationVisible}">
                <td class="fit rightalign"><span id="duration" class="detail">Duration</span></td>
            </c:if>
            <c:if test="${model.visibility.bitRateVisible}">
                <td class="fit rightalign"><span id="bitRate" class="detail">Bit Rate</span></td>
            </c:if>
        </tr>
    </tbody>
</table>

<table style="white-space:nowrap;">
    <tr style="white-space:nowrap;">
        <c:if test="${model.user.settingsRole and fn:length(model.players) gt 1}">
            <td style="padding-right: 5px"><select name="player" onchange="location='playQueue.view?player=' + options[selectedIndex].value;">
                <c:forEach items="${model.players}" var="player">
                    <option ${player.id eq model.player.id ? "selected" : ""} value="${player.id}">${player.shortDescription}</option>
                </c:forEach>
            </select></td>
        </c:if>

        <td style="white-space:nowrap;"><span class="header"><a href="javascript:onClear()"><fmt:message key="playlist.clear"/></a></span> |</td>
        <td style="white-space:nowrap;"><span class="header"><a href="javascript:onShuffle()"><fmt:message key="playlist.shuffle"/></a></span> |</td>

        <c:if test="${model.player.web or model.player.jukebox or model.player.external}">
            <td style="white-space:nowrap;"><span class="header"><a href="javascript:onToggleRepeat()"><span id="toggleRepeat"><fmt:message key="playlist.repeat_on"/></span></a></span>  |</td>
        </c:if>

        <td style="white-space:nowrap;"><span class="header"><a href="javascript:onUndo()"><fmt:message key="playlist.undo"/></a></span>  |</td>

        <c:if test="${model.user.settingsRole}">
            <td style="white-space:nowrap;"><span class="header"><a href="playerSettings.view?id=${model.player.id}" target="main"><fmt:message key="playlist.settings"/></a></span>  |</td>
        </c:if>

        <td style="white-space:nowrap;"><select id="moreActions" onchange="actionSelected(this.options[selectedIndex].id)">
            <option id="top" selected="selected"><fmt:message key="playlist.more"/></option>
            <optgroup label="<fmt:message key="playlist.more.playlist"/>">
                <option id="savePlayQueue"><fmt:message key="playlist.saveplayqueue"/></option>
                <option id="loadPlayQueue"><fmt:message key="playlist.loadplayqueue"/></option>
                <option id="savePlaylist"><fmt:message key="playlist.save"/></option>
                <c:if test="${model.user.downloadRole}">
                    <option id="downloadPlaylist"><fmt:message key="common.download"/></option>
                </c:if>
                <c:if test="${model.user.shareRole}">
                    <option id="sharePlaylist"><fmt:message key="main.more.share"/></option>
                </c:if>
                <option id="sortByTrack"><fmt:message key="playlist.more.sortbytrack"/></option>
                <option id="sortByAlbum"><fmt:message key="playlist.more.sortbyalbum"/></option>
                <option id="sortByArtist"><fmt:message key="playlist.more.sortbyartist"/></option>
            </optgroup>
            <optgroup label="<fmt:message key="playlist.more.selection"/>">
                <option id="selectAll"><fmt:message key="playlist.more.selectall"/></option>
                <option id="selectNone"><fmt:message key="playlist.more.selectnone"/></option>
                <option id="removeSelected"><fmt:message key="playlist.remove"/></option>
                <c:if test="${model.user.downloadRole}">
                    <option id="download"><fmt:message key="common.download"/></option>
                </c:if>
                <option id="appendPlaylist"><fmt:message key="playlist.append"/></option>
            </optgroup>
        </select>
        </td>

    </tr></table>

<div style="height:100px"></div>

<div id="dialog-select-playlist" title="<fmt:message key="main.addtoplaylist.title"/>" style="display: none;">
    <p><fmt:message key="main.addtoplaylist.text"/></p>
    <div id="dialog-select-playlist-list"></div>
</div>

</body></html>
