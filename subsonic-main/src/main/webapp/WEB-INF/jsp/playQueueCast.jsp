<script type="text/javascript">
(function () {
    'use strict';

    var CastPlayer = function () {

        this.castSession = null;
        this.mediaSession = null;
        this.playing = true;
        this.volume = 1.0;
        this.muted = false;

        this.initializeCastPlayer();
    };

    /*
     TODO: Premium feature
     TODO: Fix mute icon
     TODO: Only init if player type is "web".
     TODO: Use similar graphics for next/prev buttons.
     TODO: Nicer cast icons
     TODO: HLS with Media Player Library
     TODO: Debug: http://192.168.10.185:9222
     TODO: Debug: http://192.168.10.180:9222
     TODO: window.location.reload(true);
     TODO: Use play/pause icons also in jukebox mode.
     TODO: Test with https
     */

    CastPlayer.prototype.initializeCastPlayer = function () {
        if (!chrome.cast || !chrome.cast.isAvailable) {
            setTimeout(this.initializeCastPlayer.bind(this), 1000);
            return;
        }
        var applicationID = "4FBFE470";
        var sessionRequest = new chrome.cast.SessionRequest(applicationID);
        var apiConfig = new chrome.cast.ApiConfig(sessionRequest,
                this.sessionListener.bind(this),
                this.receiverListener.bind(this));
        chrome.cast.initialize(apiConfig, this.onInitSuccess.bind(this), this.onError.bind(this));
    };

    /**
     * session listener during initialization
     */
    CastPlayer.prototype.sessionListener = function (s) {
        this.log('New session ID:' + s.sessionId);
        this.castSession = s;
        this.setCastControlsVisible(true);
        if (this.castSession.media.length > 0) {
            this.log('Found ' + this.castSession.media.length + ' existing media sessions.');
            this.onMediaDiscovered('onRequestSessionSuccess_', this.castSession.media[0]);
        }
        this.castSession.addMediaListener(this.onMediaDiscovered.bind(this, 'addMediaListener'));
        this.castSession.addUpdateListener(this.sessionUpdateListener.bind(this));
        this.syncControls();
    };

    /**
     * receiver listener during initialization
     */
    CastPlayer.prototype.receiverListener = function (e) {
        if (e === 'available') {
            this.log("receiver found");
            this.setImage("castIcon", "<spring:theme code="castOffImage"/>");
        }
        else {
            this.log("receiver list empty");
            this.setImage("castIcon", "");
        }
    };

    /**
     * session update listener
     */
    CastPlayer.prototype.sessionUpdateListener = function (isAlive) {
        var message = isAlive ? 'Session Updated' : 'Session Removed';
        message += ': ' + this.castSession.sessionId;
        this.log(message);
        if (!isAlive) {
            this.castSession = null;
            this.setCastControlsVisible(false);
        }
    };

    CastPlayer.prototype.onInitSuccess = function () {
        this.log("init success");
    };

    CastPlayer.prototype.onError = function () {
        this.log("error");
    };

    CastPlayer.prototype.setCastControlsVisible = function (visible) {
        if (visible) {
            $("#flashPlayer").hide();
            $("#castPlayer").show();
            this.setImage("castIcon", "<spring:theme code="castOnImage"/>");
        } else {
            $("#castPlayer").hide();
            $("#flashPlayer").show();
            this.setImage("castIcon", "<spring:theme code="castOffImage"/>");
        }
    };

    /**
     * launch app and request session
     */
    CastPlayer.prototype.launchCastApp = function () {
        this.log("launching app...");
        chrome.cast.requestSession(this.onRequestSessionSuccess.bind(this), this.onLaunchError.bind(this));
    };

    /**
     * callback on success for requestSession call
     * @param {Object} s A non-null new session.
     */
    CastPlayer.prototype.onRequestSessionSuccess = function (s) {
        this.log("session success: " + s.sessionId);
        this.castSession = s;

        var position = -1;
        if (jwplayer().getState() == "PLAYING") {
            position = jwplayer().getPosition();
        }

        this.setCastControlsVisible(true);
        this.castSession.addUpdateListener(this.sessionUpdateListener.bind(this));
        this.syncControls();

        // Continue song at same position?
        if (position != -1) {
            skip(getCurrentSongIndex(), position);
        }
    };

    CastPlayer.prototype.onLaunchError = function () {
        this.log("launch error");
    };

    CastPlayer.prototype.loadCastMedia = function (song, position) {
        if (!this.castSession) {
            this.log("no session");
            return;
        }
        this.log("loading..." + song.remoteStreamUrl);
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
        mediaInfo.metadata.images = [new chrome.cast.Image(song.remoteCoverArtUrl + "&size=384")];
        mediaInfo.metadata.releaseYear = song.year;

        var request = new chrome.cast.media.LoadRequest(mediaInfo);
        request.autoplay = true;
        request.currentTime = position;

        this.castSession.loadMedia(request,
                this.onMediaDiscovered.bind(this, 'loadMedia'),
                this.onMediaError.bind(this));
    };

    /**
     * callback on success for loading media
     */
    CastPlayer.prototype.onMediaDiscovered = function (how, ms) {
        this.mediaSession = ms;
        this.log("new media session ID:" + this.mediaSession.mediaSessionId + ' (' + how + ')');
        this.log(ms);
        this.mediaSession.addUpdateListener(this.onMediaStatusUpdate.bind(this));
    };

    /**
     * callback on media loading error
     * @param {Object} e A non-null media object
     */
    CastPlayer.prototype.onMediaError = function (e) {
        this.log("media error");
//    TODO: icon
        this.setImage("castIcon", "<c:url value="/icons/cast/cast_icon_warning.png"/>");
    };

    /**
     * callback for media status event
     */
    CastPlayer.prototype.onMediaStatusUpdate = function () {
        this.log(this.mediaSession.playerState);
        if (this.mediaSession.playerState === chrome.cast.media.PlayerState.IDLE && this.mediaSession.idleReason === "FINISHED") {
            onNext(repeatEnabled);
        }
        this.syncControls();
    };

    CastPlayer.prototype.playPauseCast = function () {
        if (!this.mediaSession) {
            return;
        }
        if (this.playing) {
            this.mediaSession.pause(null, this.mediaCommandSuccessCallback.bind(this, "paused " + this.mediaSession.sessionId),
                    this.onError.bind(this));
            this.setImage("castPlayPause", "<spring:theme code="castPlayImage"/>");
        } else {
            this.mediaSession.play(null, this.mediaCommandSuccessCallback.bind(this, "playing started for " + this.mediaSession.sessionId),
                    this.onError.bind(this));
            this.setImage("castPlayPause", "<spring:theme code="castPauseImage"/>");
        }
        this.playing = !this.playing;
    };

    /**
     * set receiver volume
     * @param {Number} level A number for volume level
     * @param {Boolean} mute A true/false for mute/unmute
     */
    CastPlayer.prototype.setCastVolume = function (level, mute) {
        if (!this.castSession)
            return;

        this.muted = mute;

        if (!mute) {
            this.castSession.setReceiverVolumeLevel(level, this.mediaCommandSuccessCallback.bind(this, 'media set-volume done'),
                    this.onError.bind(this));
            this.volume = level;
            this.setImage("castMute", "<spring:theme code="volumeImage"/>");
        }
        else {
            this.castSession.setReceiverMuted(true, this.mediaCommandSuccessCallback.bind(this, 'media set-volume done'),
                    this.onError.bind(this));
            this.setImage("castMute", "<spring:theme code="muteImage"/>");
        }
    };

    CastPlayer.prototype.toggleCastMute = function () {
        this.setCastVolume(this.volume, !this.muted);
    };

    /**
     * callback on success for media commands
     * @param {string} info A message string
     */
    CastPlayer.prototype.mediaCommandSuccessCallback = function (info) {
        this.log(info);
    };

    CastPlayer.prototype.syncControls = function () {
        if (this.castSession.receiver.volume) {
            this.volume = this.castSession.receiver.volume.level;
            this.muted = this.castSession.receiver.volume.muted;
            this.setImage("castMute", this.muted ? "<spring:theme code="muteImage"/>" : "<spring:theme code="volumeImage"/>");
            document.getElementById("castVolume").value = this.volume * 100;
        }
        this.playing = this.castSession.media.length > 0 && this.castSession.media[0].playerState === chrome.cast.media.PlayerState.PLAYING;
        this.setImage("castPlayPause", this.playing ? "<spring:theme code="castPauseImage"/>" : "<spring:theme code="castPlayImage"/>");
    };

    CastPlayer.prototype.setImage = function (id, image) {
        document.getElementById(id).src = image;
    };

    CastPlayer.prototype.log = function (message) {
        console.log(message);
        var debug = $("#debugmessage");
        if (debug) {
            debug.html(debug.html() + "\n" + JSON.stringify(message));
        }
    };

    window.CastPlayer = CastPlayer;
})();

</script>