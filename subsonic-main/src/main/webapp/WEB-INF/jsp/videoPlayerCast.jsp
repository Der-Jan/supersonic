<script type="text/javascript">

var castSession = null;
var mediaSession = null;
var playing = true;
var volume = 1.0;
var muted = false;

if (!chrome.cast || !chrome.cast.isAvailable) {
    setTimeout(initializeCastApi, 1000);
}

function initializeCastApi() {
//    var applicationID = chrome.cast.media.DEFAULT_MEDIA_RECEIVER_APP_ID;
//    var applicationID = "4FBFE470";  // Styled receiver
    var applicationID = "644BA8AC"; // Custom receiver
    var sessionRequest = new chrome.cast.SessionRequest(applicationID);
    var apiConfig = new chrome.cast.ApiConfig(sessionRequest, sessionListener, receiverListener);

    chrome.cast.initialize(apiConfig, onInitSuccess, onError);
}

/**
 * session listener during initialization
 */
function sessionListener(s) {
    log('New session ID:' + s.sessionId);
    castSession = s;
    setCastControlsVisible(true);
    if (castSession.media.length > 0) {
        log('Found ' + castSession.media.length + ' existing media sessions.');
        onMediaDiscovered('onRequestSessionSuccess_', castSession.media[0]);
    }
    castSession.addMediaListener(onMediaDiscovered.bind(this, 'addMediaListener'));
    castSession.addUpdateListener(sessionUpdateListener.bind(this));
    syncControls();
    play();
}

/**
 * receiver listener during initialization
 */
function receiverListener(e) {
    if (e === 'available') {
        log("receiver found");
        setImage("castIcon", "<spring:theme code="castOffImage"/>");
    }
    else {
        log("receiver list empty");
        setImage("castIcon", "");
    }
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
        setCastControlsVisible(false);
    }
}

function onInitSuccess() {
    log("init success");
}

function onError() {
    log("error");
}

function setCastControlsVisible(visible) {
    if (visible) {
        $("#flashPlayer").hide();
        $("#castPlayer").show();
        setImage("castIcon", "<spring:theme code="castOnImage"/>");
    } else {
        $("#castPlayer").hide();
        $("#flashPlayer").show();
        setImage("castIcon", "<spring:theme code="castOffImage"/>");
    }
}

/**
 * launch app and request session
 */
function launchCastApp() {
    log("launching app...");
    chrome.cast.requestSession(onRequestSessionSuccess, onLaunchError);
}

/**
 * callback on success for requestSession call
 * @param {Object} s A non-null new session.
 */
function onRequestSessionSuccess(s) {
    log("session success: " + s.sessionId);
    castSession = s;

    var position = -1;
    if (jwplayer().getState() == "PLAYING") {
        position = jwplayer().getPosition();
    }

    setCastControlsVisible(true);
    castSession.addUpdateListener(sessionUpdateListener.bind(this));
    syncControls();

    // Continue song at same position?
//    if (position != -1) {
//        skip(getCurrentSongIndex(), position);
//    }
}

function onLaunchError() {
    log("launch error");
}

function loadCastMedia(video) {
    if (!castSession) {
        log("no session");
        return;
    }
    log("loading..." + video.remoteStreamUrl);
    var mediaInfo = new chrome.cast.media.MediaInfo(video.remoteStreamUrl);
    mediaInfo.contentType = video.contentType;
//    mediaInfo.streamType = chrome.cast.media.StreamType.BUFFERED;  //TODO: Use LIVE?
    mediaInfo.streamType = chrome.cast.media.StreamType.LIVE;  //TODO: Use LIVE?
    mediaInfo.duration = video.duration;
    mediaInfo.metadata = new chrome.cast.media.MovieMediaMetadata();
    mediaInfo.metadata.metadataType = chrome.cast.media.MetadataType.MOVIE;
    mediaInfo.metadata.title = video.title;
//    mediaInfo.metadata.images = [new chrome.cast.Image(video.remoteCoverArtUrl + "&size=384")];
    mediaInfo.metadata.releaseYear = video.year;

    var request = new chrome.cast.media.LoadRequest(mediaInfo);
    request.autoplay = true;
    request.currentTime = 0;

    castSession.loadMedia(request,
            onMediaDiscovered.bind(this, 'loadMedia'),
            onMediaError);
}

/**
 * callback on success for loading media
 */
function onMediaDiscovered(how, ms) {
    mediaSession = ms;
    log("new media session ID:" + mediaSession.mediaSessionId + ' (' + how + ')');
    log(ms);
    mediaSession.addUpdateListener(onMediaStatusUpdate);
}

/**
 * callback on media loading error
 * @param {Object} e A non-null media object
 */
function onMediaError(e) {
    log("media error");
//    TODO: icon
    setImage("castIcon", "<c:url value="/icons/cast/cast_icon_warning.png"/>");
}

/**
 * callback for media status event
 */
function onMediaStatusUpdate(isAlive) {
    log(mediaSession.playerState);
    if (mediaSession.playerState === chrome.cast.media.PlayerState.IDLE && mediaSession.idleReason === "FINISHED") {
        onNext(repeatEnabled);
    }
    syncControls();
}

function playPauseCast() {
    if (!mediaSession) {
        log("No session");
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
 * set receiver volume
 * @param {Number} level A number for volume level
 * @param {Boolean} mute A true/false for mute/unmute
 */
function setCastVolume(level, mute) {
    if (!castSession)
        return;

    muted = mute;

    if (!mute) {
        castSession.setReceiverVolumeLevel(level, mediaCommandSuccessCallback.bind(this, 'media set-volume done'), onError);
        volume = level;
        setImage("castMute", "<spring:theme code="volumeImage"/>");
    }
    else {
        castSession.setReceiverMuted(true, mediaCommandSuccessCallback.bind(this, 'media set-volume done'), onError);
        setImage("castMute", "<spring:theme code="muteImage"/>");
    }
}

function toggleCastMute() {
    setCastVolume(volume, !muted);
}

/**
 * callback on success for media commands
 * @param {string} info A message string
 */
function mediaCommandSuccessCallback(info) {
    log(info);
}

function syncControls() {
    if (castSession.receiver.volume) {
        volume = castSession.receiver.volume.level;
        muted = castSession.receiver.volume.muted;
        setImage("castMute", muted ? "<spring:theme code="muteImage"/>" : "<spring:theme code="volumeImage"/>");
        document.getElementById("castVolume").value = volume * 100;
    }
    playing = castSession.media.length > 0 && castSession.media[0].playerState === chrome.cast.media.PlayerState.PLAYING;
    setImage("castPlayPause", playing ? "<spring:theme code="castPauseImage"/>" : "<spring:theme code="castPlayImage"/>");
}

function setImage(id, image) {
    document.getElementById(id).src = image;
}

function log(message) {
    console.log(message);
    $("#debugmessage").html($("#debugmessage").html() + "\n" + JSON.stringify(message));
}
</script>