<script type="text/javascript">
/**
 * global variables
 */
var mediaSession = null;
var currentVolume = 0.5;
var progressFlag = 1;
var mediaCurrentTime = 0;
var castSession = null;
var playing = true;

/*
  TODO: Play/pause img
  TODO: Only init if player type is "web".
  TODO: Host google js locally.
  TODO: Use similar graphics for next/prev buttons.
 */

/**
 * Call initialization
 */
if (!chrome.cast || !chrome.cast.isAvailable) {
    setTimeout(initializeCastApi, 1000);
}

/**
 * initialization
 */
function initializeCastApi() {
    // default app ID to the default media receiver app
    // optional: you may change it to your own app ID/receiver
    var applicationID = chrome.cast.media.DEFAULT_MEDIA_RECEIVER_APP_ID;
    var sessionRequest = new chrome.cast.SessionRequest(applicationID);
    var apiConfig = new chrome.cast.ApiConfig(sessionRequest, sessionListener, receiverListener);

    chrome.cast.initialize(apiConfig, onInitSuccess, onError);
}

/**
 * initialization success callback
 */
function onInitSuccess() {
    log("init success");
}

/**
 * initialization error callback
 */
function onError() {
    log("error");
}

/**
 * callback on success for stopping app
 */
function onStopAppSuccess() {
    log('Session stopped');
    setImage("castIcon", "<c:url value="/icons/cast/cast_icon_idle.png"/>");
    setCastControlsVisible(false);
}

function setImage(id, image) {
    document.getElementById(id).src = image;
}

function setCastControlsVisible(visible) {
    if (visible) {
        $("#flashPlayer").hide();
        $("#castPlayer").show();
    } else {
        $("#castPlayer").hide();
        $("#flashPlayer").show();
    }
}

/**
 * session listener during initialization
 */
function sessionListener(e) {
    log('New session ID:' + e.sessionId);
    castSession = e;
    if (castSession.media.length != 0) {
        log('Found ' + castSession.media.length + ' existing media sessions.');
        onMediaDiscovered('onRequestSessionSuccess_', castSession.media[0]);
    }
    castSession.addMediaListener(onMediaDiscovered.bind(this, 'addMediaListener'));
    castSession.addUpdateListener(sessionUpdateListener.bind(this));
}

/**
 * session update listener
 */
function sessionUpdateListener(isAlive) {
    var message = isAlive ? 'Session Updated' : 'Session Removed';
    message += ': ' + castSession.sessionId;
    log(message);
    if (!isAlive) {
        castSession = null;
        setImage("castIcon", "<c:url value="/icons/cast/cast_icon_idle.png"/>");
//        var playpauseresume = document.getElementById("playpauseresume");
//        playpauseresume.innerHTML = 'Play';
    }
}

/**
 * receiver listener during initialization
 */
function receiverListener(e) {
    if (e === 'available') {
        log("receiver found");
        setImage("castIcon", "<c:url value="/icons/cast/cast_icon_idle.png"/>");
    }
    else {
        // TODO: Remove
        setCastControlsVisible(true);
        log("receiver list empty");
    }
}

/**
 * select a media URL
 * @param {string} m An index for media URL
 */
//function selectMedia(m) {
//    log("media selected" + m);
//    currentMediaURL = mediaURLs[m];
//    var playpauseresume = document.getElementById("playpauseresume");
//    document.getElementById('thumb').src = mediaThumbs[m];
//}

/**
 * launch app and request session
 */
function launchCastApp() {
    log("launching app...");
    chrome.cast.requestSession(onRequestSessionSuccess, onLaunchError);
}

/**
 * callback on success for requestSession call
 * @param {Object} e A non-null new session.
 */
function onRequestSessionSuccess(e) {
    log("session success: " + e.sessionId);
    castSession = e;
    setImage("castIcon", "<c:url value="/icons/cast/cast_icon_active.png"/>");
    setCastControlsVisible(true);
    castSession.addUpdateListener(sessionUpdateListener.bind(this));
}

/**
 * callback on launch error
 */
function onLaunchError() {
    log("launch error");
}

/**
 * stop app/session
 */
function stopApp() {
    castSession.stop(onStopAppSuccess, onError);
}

function loadMedia(song) {
    if (!castSession) {
        log("no session");
        return;
    }
    log("loading..." + song.remoteStreamUrl);
    var mediaInfo = new chrome.cast.media.MediaInfo(song.remoteStreamUrl);
    mediaInfo.contentType = song.contentType;
    mediaInfo.streamType = chrome.cast.media.StreamType.BUFFERED;
    mediaInfo.duration = song.duration;
    mediaInfo.metadata = new chrome.cast.media.MusicTrackMediaMetadata();
    mediaInfo.metadata.metadataType = chrome.cast.media.MetadataType.MUSIC_TRACK;
    mediaInfo.metadata.songName = song.title;
    mediaInfo.metadata.title = song.title;
    mediaInfo.metadata.albumName = song.album;
    mediaInfo.metadata.artist = song.artist;
    mediaInfo.metadata.trackNumber = song.trackNumber;
    mediaInfo.metadata.images = [new chrome.cast.Image(song.remoteCoverArtUrl)];
    mediaInfo.metadata.releaseYear = song.year;

    var request = new chrome.cast.media.LoadRequest(mediaInfo);
    request.autoplay = true;
    request.currentTime = 0;

    castSession.loadMedia(request,
            onMediaDiscovered.bind(this, 'loadMedia'),
            onMediaError);
}

/**
 * callback on success for loading media
 * @param {Object} e A non-null media object
 */
function onMediaDiscovered(how, ms) {
    mediaSession = ms;
    log("new media session ID:" + mediaSession.mediaSessionId + ' (' + how + ')');
    mediaSession.addUpdateListener(onMediaStatusUpdate);
    mediaCurrentTime = mediaSession.currentTime;
//    playpauseresume.innerHTML = 'Play';
    setImage("castIcon", "<c:url value="/icons/cast/cast_icon_active.png"/>");
}

/**
 * callback on media loading error
 * @param {Object} e A non-null media object
 */
function onMediaError(e) {
    log("media error");
    setImage("castIcon", "<c:url value="/icons/cast/cast_icon_warning.png"/>");
}

/**
 * callback for media status event
 * @param {Object} e A non-null media object
 */
function onMediaStatusUpdate(isAlive) {
    log(mediaSession);
    if (mediaSession.playerState === chrome.cast.media.PlayerState.IDLE && mediaSession.idleReason === "FINISHED") {
        onNext(repeatEnabled);
    }
//    if (progressFlag) {
//        document.getElementById("progress").value = parseInt(100 * mediaSession.currentTime / mediaSession.media.duration);
//    }
//    document.getElementById("playerstate").innerHTML = mediaSession.playerState;
}


function playPauseCast() {
    if (!mediaSession) {
        return;
    }
    if (playing) {
        mediaSession.pause(null, mediaCommandSuccessCallback.bind(this, "paused " + mediaSession.sessionId), onError);
        setImage("castPlayPause", "<spring:theme code="castPlayImage"/>");
    } else {
        mediaSession.play(null, mediaCommandSuccessCallback.bind(this, "playing started for " + mediaSession.sessionId), onError);
        setImage("castPlayPause", "<spring:theme code="castPauseImage"/>");
    }
    playing = !playing;
}

/**
 * play media
 */
function playMedia() {
    if (!mediaSession)
        return;


    var playpauseresume = document.getElementById("playpauseresume");
    if (playpauseresume.innerHTML == 'Play') {
        mediaSession.play(null,
                mediaCommandSuccessCallback.bind(this, "playing started for " + mediaSession.sessionId),
                onError);
        playpauseresume.innerHTML = 'Pause';
        //mediaSession.addListener(onMediaStatusUpdate);
        log("play started");
    }
    else {
        if (playpauseresume.innerHTML == 'Pause') {
            mediaSession.pause(null,
                    mediaCommandSuccessCallback.bind(this, "paused " + mediaSession.sessionId),
                    onError);
            playpauseresume.innerHTML = 'Resume';
            log("paused");
        }
        else {
            if (playpauseresume.innerHTML == 'Resume') {
                mediaSession.play(null,
                        mediaCommandSuccessCallback.bind(this, "resumed " + mediaSession.sessionId),
                        onError);
                playpauseresume.innerHTML = 'Pause';
                log("resumed");
            }
        }
    }
}

/**
 * stop media
 */
function stopMedia() {
    if (!mediaSession)
        return;

    mediaSession.stop(null,
            mediaCommandSuccessCallback.bind(this, "stopped " + mediaSession.sessionId),
            onError);
    var playpauseresume = document.getElementById("playpauseresume");
    playpauseresume.innerHTML = 'Play';
    log("media stopped");
}

/**
 * set media volume
 * @param {Number} level A number for volume level
 * @param {Boolean} mute A true/false for mute/unmute
 */
function setMediaVolume(level, mute) {
    if (!mediaSession)
        return;

    var volume = new chrome.cast.Volume();
    volume.level = level;
    currentVolume = volume.level;
    volume.muted = mute;
    var request = new chrome.cast.media.VolumeRequest();
    request.volume = volume;
    mediaSession.setVolume(request,
            mediaCommandSuccessCallback.bind(this, 'media set-volume done'),
            onError);
}

/**
 * set receiver volume
 * @param {Number} level A number for volume level
 * @param {Boolean} mute A true/false for mute/unmute
 */
function setReceiverVolume(level, mute) {
    if (!castSession)
        return;

    if (!mute) {
        castSession.setReceiverVolumeLevel(level,
                mediaCommandSuccessCallback.bind(this, 'media set-volume done'),
                onError);
        currentVolume = level;
    }
    else {
        castSession.setReceiverMuted(true,
                mediaCommandSuccessCallback.bind(this, 'media set-volume done'),
                onError);
    }
}

/**
 * mute media
 * @param {DOM Object} cb A checkbox element
 */
function muteMedia(cb) {
    if (cb.checked == true) {
        document.getElementById('muteText').innerHTML = 'Unmute media';
        //setMediaVolume(currentVolume, true);
        setReceiverVolume(currentVolume, true);
        log("media muted");
    }
    else {
        document.getElementById('muteText').innerHTML = 'Mute media';
        //setMediaVolume(currentVolume, false);
        setReceiverVolume(currentVolume, false);
        log("media unmuted");
    }
}

/**
 * seek media position
 * @param {Number} pos A number to indicate percent
 */
//function seekMedia(pos) {
//    console.log('Seeking ' + mediaSession.sessionId + ':' +
//            mediaSession.mediaSessionId + ' to ' + pos + "%");
//    progressFlag = 0;
//    var request = new chrome.cast.media.SeekRequest();
//    request.currentTime = pos * mediaSession.media.duration / 100;
//    mediaSession.seek(request,
//            onSeekSuccess.bind(this, 'media seek done'),
//            onError);
//}

/**
 * callback on success for media commands
 * @param {string} info A message string
 * @param {Object} e A non-null media object
 */
//function onSeekSuccess(info) {
//    console.log(info);
//    log(info);
//    setTimeout(function () {
//        progressFlag = 1
//    }, 1500);
//}

/**
 * callback on success for media commands
 * @param {string} info A message string
 * @param {Object} e A non-null media object
 */
function mediaCommandSuccessCallback(info) {
    log(info);
}

/**
 * append message to debug message window
 * @param {string} message A message string
 */
function log(message) {
    console.log(message);
    $("#debugmessage").html($("#debugmessage").html() + "\n" + JSON.stringify(message));
}
</script>