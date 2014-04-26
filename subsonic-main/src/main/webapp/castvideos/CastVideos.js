
// TODO: Seek = set offset + load
// TODO: Initial volume
// TODO: Emulate youtube controls.
// TODO: Use jquery
// TODO: Remote seeking not implemented.
// TODO: Integrate with videoPlayer.jsp
// TODO: Smaller play/pause buttons
// TODO: Fonts
// TODO: Proper handling of EOM
// TODO: Test on other browsers
// TODO: Starts playing locally when session times out.
// TODO: Sometimes playing both locally and remote.


(function () {
    'use strict';

    var MEDIA_SOURCE_URL = "http://192.168.10.140:4040/stream?player=1&id=6&maxBitRate=2000";
    var DURATION = 137; // TODO

    /**
     * Constants of states for Chromecast device
     **/
    var DEVICE_STATE = {
        'NOT_PRESENT': 0,
        'IDLE': 1,
        'ACTIVE': 2,
        'WARNING': 3,
        'ERROR': 4
    };

    var PLAYER_STATE = {
        'IDLE': 'IDLE',
        'LOADING': 'LOADING',
        'LOADED': 'LOADED',
        'PLAYING': 'PLAYING',
        'PAUSED': 'PAUSED',
        'STOPPED': 'STOPPED',
        'SEEKING': 'SEEKING',
        'ERROR': 'ERROR'
    };

    /**
     * Cast player object
     * main variables:
     *  - deviceState for Cast mode:
     *    IDLE: Default state indicating that Cast extension is installed, but showing no current activity
     *    ACTIVE: Shown when Chrome has one or more local activities running on a receiver
     *    WARNING: Shown when the device is actively being used, but when one or more issues have occurred
     *    ERROR: Should not normally occur, but shown when there is a failure
     *  - Cast player variables for controlling Cast mode media playback
     *  - Local player variables for controlling local mode media playbacks
     *  - Current media variables for transition between Cast and local modes
     */
    var CastPlayer = function () {

        /* device variables */

        // @type {DEVICE_STATE} A state for device
        this.deviceState = DEVICE_STATE.NOT_PRESENT;

        /* Cast player variables */

        // @type {Object} a chrome.cast.media.Media object
        this.currentMediaSession = null;

        // @type {Number} volume between 0.0 and 1.0
        this.currentVolume = 0.5;

        // @type {Boolean} A flag for autoplay after load
        this.autoplay = true;

        // @type {string} a chrome.cast.Session object
        this.session = null;

        // @type {PLAYER_STATE} A state for Cast media player
        this.castPlayerState = PLAYER_STATE.IDLE;

        /* Local player variables */

        // @type {PLAYER_STATE} A state for local media player
        this.localPlayerState = PLAYER_STATE.IDLE;

        // @type {jwplayer} local player
        this.localPlayer = null;

        /* Current media variables */

        // @type {Boolean} Muted audio
        this.muted = false;

        // @type {Number} A number for current media offset
        this.currentMediaOffset = 0;

        // @type {Number} A number for current media time, relative to offset
        this.currentMediaTime = 0;

        // @type {Number} A number for current media duration
        this.currentMediaDuration = DURATION;

        // @type {Timer} A timer for tracking progress of media
        this.timer = null;

        // @type {Boolean} A boolean to stop timer update of progress when triggered by media status event
        this.seekInProgress = false;

        // @type {Number} A number in milliseconds for minimal progress update
        this.timerStep = 1000;

        this.updateDurationLabel();
        this.initializeCastPlayer();
        this.initializeLocalPlayer();
    };

    /**
     * Initialize local media player
     */
    CastPlayer.prototype.initializeLocalPlayer = function () {
        jwplayer("jwplayer").setup({
            flashplayer: "/flash/jw-player-5.10.swf",
            height: 360,
            width: 640,
            skin: "/flash/jw-player-subsonic-skin.zip",
            screencolor: "000000",
            controlbar: "over",
            autostart: "false",
            bufferlength: 3,
            provider: "video",
            events: {
                onTime: this.updateLocalProgress.bind(this),
                onPlay: this.updateLocalState.bind(this),
                onPause: this.updateLocalState.bind(this),
                onIdle: this.updateLocalState.bind(this)
            }
        });
        this.localPlayer = jwplayer();
    };

    CastPlayer.prototype.updateLocalProgress = function (event) {
        var newTime = Math.round(event.position);
        if (newTime != this.currentMediaTime && !this.seekInProgress) {
            this.currentMediaTime = newTime;
            console.log(this.currentMediaTime + " (updateLocalProgress)");
            this.updateProgressBar();
        }
    };

    CastPlayer.prototype.updateLocalState = function () {
        if (this.localPlayer.getState() == "PLAYING" || this.localPlayer.getState() == "BUFFERING") {
            this.localPlayerState = PLAYER_STATE.PLAYING;
        } else if (this.localPlayer.getState() == "PAUSED") {
            this.localPlayerState = PLAYER_STATE.PAUSED;
        } else if (this.localPlayer.getState() == "IDLE") {
            this.localPlayerState = PLAYER_STATE.IDLE;
        }
        this.updateMediaControlUI();
    };

    /**
     * Initialize Cast media player
     * Initializes the API. Note that either successCallback and errorCallback will be
     * invoked once the API has finished initialization. The sessionListener and
     * receiverListener may be invoked at any time afterwards, and possibly more than once.
     */
    CastPlayer.prototype.initializeCastPlayer = function () {

        if (!chrome.cast || !chrome.cast.isAvailable) {
            setTimeout(this.initializeCastPlayer.bind(this), 1000);
            return;
        }
        // default set to the default media receiver app ID
        // optional: you may change it to point to your own
//        var applicationID = chrome.cast.media.DEFAULT_MEDIA_RECEIVER_APP_ID; // TODO
        var applicationID = "644BA8AC"; // Custom receiver

        // request session
        var sessionRequest = new chrome.cast.SessionRequest(applicationID);
        var apiConfig = new chrome.cast.ApiConfig(sessionRequest,
            this.sessionListener.bind(this),
            this.receiverListener.bind(this));

        chrome.cast.initialize(apiConfig, this.onInitSuccess.bind(this), this.onError.bind(this));

        this.initializeUI();
    };

    /**
     * Callback function for init success
     */
    CastPlayer.prototype.onInitSuccess = function () {
        console.log("init success");
        this.updateMediaControlUI();
    };

    /**
     * Generic error callback function
     */
    CastPlayer.prototype.onError = function () {
        console.log("error");
    };

    /**
     * @param {!Object} e A new session
     * This handles auto-join when a page is reloaded
     * When active session is detected, playback will automatically
     * join existing session and occur in Cast mode and media
     * status gets synced up with current media of the session
     */
    CastPlayer.prototype.sessionListener = function (e) {
        this.session = e;
        if (this.session) {
            this.deviceState = DEVICE_STATE.ACTIVE;
            if (this.session.media[0]) {
                this.onMediaDiscovered('activeSession', this.session.media[0]);
            }
            else {
                this.loadMedia();
            }
            this.session.addUpdateListener(this.sessionUpdateListener.bind(this));
        }
    };

    /**
     * @param {string} e Receiver availability
     * This indicates availability of receivers but
     * does not provide a list of device IDs
     */
    CastPlayer.prototype.receiverListener = function (e) {
        if (e === 'available') {
            console.log("receiver found");
            this.deviceState = DEVICE_STATE.IDLE;
        }
        else {
            console.log("receiver list empty");
            this.deviceState = DEVICE_STATE.NOT_PRESENT;
        }
        this.updateMediaControlUI();
    };

    /**
     * session update listener
     */
    CastPlayer.prototype.sessionUpdateListener = function (isAlive) {
        if (!isAlive) {
            this.session = null;
            this.deviceState = DEVICE_STATE.IDLE;
            this.castPlayerState = PLAYER_STATE.IDLE;
            this.currentMediaSession = null;
            clearInterval(this.timer);

            // continue to play media locally
            console.log("current time (sessionUpdateListener): " + this.currentMediaTime);
            this.playMediaLocally(this.currentMediaOffset + this.currentMediaTime);
            this.updateMediaControlUI();
        }
    };

    /**
     * Requests that a receiver application session be created or joined. By default, the SessionRequest
     * passed to the API at initialization time is used; this may be overridden by passing a different
     * session request in opt_sessionRequest.
     */
    CastPlayer.prototype.launchApp = function () {
        console.log("launching app...");
        chrome.cast.requestSession(this.onRequestSessionSuccess.bind(this), this.onLaunchError.bind(this));
        if (this.timer) {
            clearInterval(this.timer);
        }
    };

    /**
     * Callback function for request session success
     * @param {Object} e A chrome.cast.Session object
     */
    CastPlayer.prototype.onRequestSessionSuccess = function (e) {
        console.log("session success: " + e.sessionId);
        this.session = e;
        this.deviceState = DEVICE_STATE.ACTIVE;
        this.stopMediaLocally();
        this.loadMedia();
        this.session.addUpdateListener(this.sessionUpdateListener.bind(this));
    };

    /**
     * Callback function for launch error
     */
    CastPlayer.prototype.onLaunchError = function () {
        console.log("launch error");
        this.deviceState = DEVICE_STATE.ERROR;
    };

    /**
     * Stops the running receiver application associated with the session.
     */
    CastPlayer.prototype.stopApp = function () {
        this.session.stop(this.onStopAppSuccess.bind(this, 'Session stopped'),
            this.onError.bind(this));

    };

    /**
     * Callback function for stop app success
     */
    CastPlayer.prototype.onStopAppSuccess = function (message) {
        console.log(message);
        this.deviceState = DEVICE_STATE.IDLE;
        this.castPlayerState = PLAYER_STATE.IDLE;
        this.currentMediaSession = null;
        clearInterval(this.timer);

        // continue to play media locally
        console.log("current time (onStopAppSuccess): " + this.currentMediaTime);
        this.playMediaLocally(this.currentMediaOffset + this.currentMediaTime);
        this.updateMediaControlUI();
    };

    /**
     * Loads media into a running receiver application
     */
    CastPlayer.prototype.loadMedia = function () {
        if (!this.session) {
            console.log("no session");
            return;
        }
        var offset = this.currentMediaOffset + this.currentMediaTime;
        this.currentMediaOffset = offset;
        this.currentMediaTime = 0;
        console.log(this.currentMediaTime + " (loadMedia cast)");

        var url = MEDIA_SOURCE_URL + "&format=mkv&timeOffset=" + offset;  // TODO: mkv
        console.log("loading..." + url);
        var mediaInfo = new chrome.cast.media.MediaInfo(url);
        mediaInfo.contentType = 'video/x-matroska'; // TODO
        // TODO: Add metadata.
        var request = new chrome.cast.media.LoadRequest(mediaInfo);
        request.autoplay = this.autoplay;
        request.currentTime = 0;

        this.castPlayerState = PLAYER_STATE.LOADING;
        this.session.loadMedia(request,
            this.onMediaDiscovered.bind(this, 'loadMedia'),
            this.onLoadMediaError.bind(this));
    };

    /**
     * Callback function for loadMedia success
     * @param {Object} mediaSession A new media object.
     * @param {String} how How the session was discovered.
     */
    CastPlayer.prototype.onMediaDiscovered = function (how, mediaSession) {
        console.log("new media session ID:" + mediaSession.mediaSessionId + ' (' + how + ')');
        this.currentMediaSession = mediaSession;
        if (how == 'loadMedia') {
            this.castPlayerState = this.autoplay ? PLAYER_STATE.PLAYING : this.castPlayerState = PLAYER_STATE.LOADED;
        }

        if (how == 'activeSession') {
            this.castPlayerState = this.session.media[0].playerState;
            this.currentMediaTime = Math.round(this.session.media[0].currentTime);
            console.log(this.currentMediaTime + " (media time from device)");
        }

        if (this.castPlayerState == PLAYER_STATE.PLAYING) {
            this.startProgressTimer(this.incrementMediaTime);
        }

        this.currentMediaSession.addUpdateListener(this.onMediaStatusUpdate.bind(this));

        // update UI
        this.updateMediaControlUI();
    };

    /**
     * Callback function when media load returns error
     */
    CastPlayer.prototype.onLoadMediaError = function (e) {
        console.log("media error");
        this.castPlayerState = PLAYER_STATE.IDLE;
        this.updateMediaControlUI();
    };

    /**
     * Callback function for media status update from receiver
     * @param {!Boolean} e true/false
     */
    CastPlayer.prototype.onMediaStatusUpdate = function (e) {
        if (!e) {
//            this.currentMediaTime = 0;
            this.castPlayerState = PLAYER_STATE.IDLE;
        }
        console.log("updating media");
        this.updateProgressBar();
        this.updateMediaControlUI();
    };

    /**
     * Helper function
     * Increment media current position by 1 second
     */
    CastPlayer.prototype.incrementMediaTime = function () {
        if (this.castPlayerState == PLAYER_STATE.PLAYING) {
            if (this.currentMediaOffset + this.currentMediaTime < this.currentMediaDuration) {
                this.currentMediaTime += 1;
                console.log(this.currentMediaTime + " (incrementMediaTime)");
                this.updateProgressBar();
            }
            else {
//                this.currentMediaTime = 0;
                clearInterval(this.timer);
            }
        }
    };

    /**
     * Updates the duration label.
     */
    CastPlayer.prototype.updateDurationLabel = function () {
        document.getElementById("duration").innerHTML = this.formatDuration(this.currentMediaDuration);
    };

    CastPlayer.prototype.formatDuration = function (duration) {
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
    };

    /**
     * Play media in Cast mode
     */
    CastPlayer.prototype.playMedia = function () {
        if (!this.currentMediaSession) {
            this.playMediaLocally(0);
            return;
        }

        switch (this.castPlayerState) {
            case PLAYER_STATE.LOADED:
            case PLAYER_STATE.PAUSED:
                this.currentMediaSession.play(null,
                    this.mediaCommandSuccessCallback.bind(this, "playing started for " + this.currentMediaSession.sessionId),
                    this.onError.bind(this));
                this.currentMediaSession.addUpdateListener(this.onMediaStatusUpdate.bind(this));
                this.castPlayerState = PLAYER_STATE.PLAYING;
                this.startProgressTimer(this.incrementMediaTime);
                break;
            case PLAYER_STATE.IDLE:
            case PLAYER_STATE.LOADING:
            case PLAYER_STATE.STOPPED:
                this.loadMedia();
                this.currentMediaSession.addUpdateListener(this.onMediaStatusUpdate.bind(this));
                this.castPlayerState = PLAYER_STATE.PLAYING;
                break;
            default:
                break;
        }
        this.updateMediaControlUI();
    };

    /**
     * Play media in local player
     * @param {Number} offset A number for media current position
     */
    CastPlayer.prototype.playMediaLocally = function (offset) {

        // Resume?
        if (this.localPlayerState == PLAYER_STATE.PLAYING || this.localPlayerState == PLAYER_STATE.PAUSED) {
            this.localPlayer.play();
        } else {
            this.currentMediaOffset = offset;
            this.currentMediaTime = 0;
            console.log(this.currentMediaTime + " (playMediaLocally)");

            this.localPlayer.load({
                file: MEDIA_SOURCE_URL + "&timeOffset=" + offset,
                duration: this.currentMediaDuration,
                provider: "video"
            });
            this.localPlayer.play();
            this.seekInProgress = false;
        }
        this.updateMediaControlUI();
    };

    /**
     * Pause media playback in Cast mode
     */
    CastPlayer.prototype.pauseMedia = function () {
        if (!this.currentMediaSession) {
            this.pauseMediaLocally();
            return;
        }

        if (this.castPlayerState == PLAYER_STATE.PLAYING) {
            this.castPlayerState = PLAYER_STATE.PAUSED;
            this.currentMediaSession.pause(null,
                this.mediaCommandSuccessCallback.bind(this, "paused " + this.currentMediaSession.sessionId),
                this.onError.bind(this));
            this.updateMediaControlUI();
            clearInterval(this.timer);
        }
    };

    /**
     * Pause media playback in local player
     */
    CastPlayer.prototype.pauseMediaLocally = function () {
        this.localPlayer.pause();
        this.updateMediaControlUI();
        clearInterval(this.timer);
    };

    /**
     * Stop media playback in local player
     */
    CastPlayer.prototype.stopMediaLocally = function () {
        this.localPlayer.stop();
        this.updateMediaControlUI();
    };

    /**
     * Set media volume in local or Cast mode
     * @param {Boolean} mute A boolean
     */
    CastPlayer.prototype.setReceiverVolume = function (mute) {
        this.currentVolume = parseInt(document.getElementById("volume_slider").value);

        if (!this.currentMediaSession) {
            this.localPlayer.setMute(mute);
            if (!mute) {
                this.localPlayer.setVolume(this.currentVolume);
            }
            return;
        }

        if (mute) {
            this.session.setReceiverMuted(true,
                    this.mediaCommandSuccessCallback.bind(this),
                    this.onError.bind(this));
        } else {
            this.session.setReceiverVolumeLevel(this.currentVolume / 100.0,
                    this.mediaCommandSuccessCallback.bind(this),
                    this.onError.bind(this));
        }
        this.updateMediaControlUI();
    };

    /**
     * Toggle mute in either Cast or local mode
     */
    CastPlayer.prototype.muteMedia = function () {
        this.muted = !this.muted;
        this.setReceiverVolume(this.muted);
        document.getElementById('audio_on').style.display = this.muted ? 'none' : 'block';
        document.getElementById('audio_off').style.display = this.muted ? 'block' : 'none';
        this.updateMediaControlUI();
    };

    /**
     * media seek function in either Cast or local mode
     */
    CastPlayer.prototype.seekMedia = function () {

        var offset = parseInt(document.getElementById("progress_slider").value);
        this.seekInProgress = true;
        this.currentMediaOffset = offset;
        this.currentMediaTime = 0;
        console.log(this.currentMediaTime + " (seekMedia)");


        if (this.localPlayerState == PLAYER_STATE.PLAYING || this.localPlayerState == PLAYER_STATE.PAUSED) {
            this.localPlayerState = PLAYER_STATE.SEEKING;
            this.playMediaLocally(offset);
            return;
        }

        if (this.castPlayerState != PLAYER_STATE.PLAYING && this.castPlayerState != PLAYER_STATE.PAUSED) {
            return;
        }

        this.castPlayerState = PLAYER_STATE.SEEKING;
        this.loadMedia();

        this.updateMediaControlUI();
    };

    /**
     * Callback function for media command success
     */
    CastPlayer.prototype.mediaCommandSuccessCallback = function (info, e) {
        this.currentMediaTime = Math.round(this.session.media[0].currentTime);
        console.log(this.currentMediaTime + " (mediaCommandSuccessCallback)");
        console.log(info);
    };

    /**
     * Update progress bar with the current media time.
     */
    CastPlayer.prototype.updateProgressBar = function () {
        document.getElementById("progress_slider").value = this.currentMediaOffset + this.currentMediaTime;
        document.getElementById("progress").innerHTML = this.formatDuration(this.currentMediaOffset + this.currentMediaTime);
    };

    CastPlayer.prototype.updateDebug = function () {
        var debug = "<br>currentMediaOffset: " + this.currentMediaOffset + "<br>"
                + "currentMediaTime: " + this.currentMediaTime + "<br>"
                + "localPlayerState: " + this.localPlayerState + "<br>"
                + "castPlayerState: " + this.castPlayerState;
        document.getElementById("debug").innerHTML = debug;
    };

    /**
     * Update media control UI components based on localPlayerState or castPlayerState
     */
    CastPlayer.prototype.updateMediaControlUI = function () {

        if (this.deviceState == DEVICE_STATE.NOT_PRESENT) {
            document.getElementById("casticonactive").style.display = 'none';
            document.getElementById("casticonidle").style.display = 'none';
            document.getElementById("overlay_text").style.display = 'none';
            document.getElementById("overlay").style.display = 'none';
            var playerState = this.localPlayerState;
        } else if (this.deviceState == DEVICE_STATE.ACTIVE) {
            document.getElementById("casticonactive").style.display = 'block';
            document.getElementById("casticonidle").style.display = 'none';
            document.getElementById("overlay_text").style.display = 'block';
            document.getElementById("overlay").style.display = 'block';
            var playerState = this.castPlayerState;
        } else {
            document.getElementById("casticonactive").style.display = 'none';
            document.getElementById("casticonidle").style.display = 'block';
            document.getElementById("overlay_text").style.display = 'none';
            var loaded = this.localPlayer.getPlaylist().length > 0;
            document.getElementById("overlay").style.display = loaded ? 'none' : 'block';
            var playerState = this.localPlayerState;
        }

        switch (playerState) {
            case PLAYER_STATE.LOADED:
            case PLAYER_STATE.PLAYING:
                document.getElementById("play").style.display = 'none';
                document.getElementById("pause").style.display = 'block';
                break;
            case PLAYER_STATE.PAUSED:
            case PLAYER_STATE.IDLE:
            case PLAYER_STATE.LOADING:
            case PLAYER_STATE.STOPPED:
                document.getElementById("play").style.display = 'block';
                document.getElementById("pause").style.display = 'none';
                break;
            default:
                break;
        }
    };

    /**
     * Initialize UI components and add event listeners
     */
    CastPlayer.prototype.initializeUI = function () {

        document.getElementById("progress_slider").max = DURATION;

        // add event handlers to UI components
        document.getElementById("casticonidle").addEventListener('click', this.launchApp.bind(this));
        document.getElementById("casticonactive").addEventListener('click', this.stopApp.bind(this));
        document.getElementById("progress_slider").addEventListener('mouseup', this.seekMedia.bind(this));
        document.getElementById("volume_slider").addEventListener('change', this.setReceiverVolume.bind(this, false));
        document.getElementById("audio_on").addEventListener('click', this.muteMedia.bind(this));
        document.getElementById("audio_off").addEventListener('click', this.muteMedia.bind(this));
        document.getElementById("play").addEventListener('click', this.playMedia.bind(this));
        document.getElementById("pause").addEventListener('click', this.pauseMedia.bind(this));

        setInterval(this.updateDebug.bind(this), 100);
    };

    /**
     * @param {function} callback A callback function for the fucntion to start timer
     */
    CastPlayer.prototype.startProgressTimer = function (callback) {
        if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
        }

        // start progress timer
        this.timer = setInterval(callback.bind(this), this.timerStep);
    };

    window.CastPlayer = CastPlayer;
})();
