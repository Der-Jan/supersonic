<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<%! String current = "changelog"; %>
<%@ include file="header.jsp" %>

<body>

<a name="top"/>

<div id="container">
<%@ include file="menu.jsp" %>

<div id="content">
<div id="main-col">
<h1 class="bottomspace">Subsonic Change Log</h1>

<a name="4.8"><h2 class="div">Subsonic 4.8 - Apr 20, 2013</h2></a>
<ul>
    <li><span class="bugid">New: </span>Introduced <a href="premium.jsp">Subsonic Premium</a>.</li>
    <li><span class="bugid">New: </span>Re-import playlists if file timestamp has changed.</li>
    <li><span class="bugid">New: </span>Make playlist folder setting visible again.</li>
    <li><span class="bugid">New: </span>Changed bitrate to video resolution mapping.</li>
    <li><span class="bugid">New: </span>Added Norwegion Nynorsk translation, courtesy of Kevin Brubeck Unhammer.</li>
    <li><span class="bugid">New: </span>Updated Dutch translation, courtesy of W. van der Heijden.</li>
    <li><span class="bugid">New: </span>Updated German translation, courtesy of deejay2302.</li>
    <li><span class="bugid">New: </span>Updated French translation, courtesy of Yoann Spicher.</li>
    <li><span class="bugid">New: </span>Updated Simplified Chinese translation, courtesy of Zhenghao Zhu.</li>
    <li><span class="bugid">Bugfix: </span>Settings &gt; Network doesn't show error if a subsonic.org address is in use.</li>
    <li><span class="bugid">Bugfix: </span>Improved speed of tag editing.</li>
    <li><span class="bugid">Bugfix: </span>Ogg dates not always parsed properly.</li>
    <li><span class="bugid">Bugfix: </span>Sort songs by filename if track number is missing.</li>
    <li><span class="bugid">Bugfix: </span>Fix init exception in podcast bean.</li>
    <li><span class="bugid">Bugfix: </span>Links to minisub and apps icons doesn't honor context path.</li>
    <li><span class="bugid">Bugfix: </span>Less aggressive removal of track number from title.</li>
    <li><span class="bugid">Bugfix: </span>HLS broken with context path.</li>
    <li><span class="bugid">Bugfix: </span>Video player didn't require authentication.</li>
    <li><span class="bugid">Bugfix: </span>Download cover to replace in-metadata image results in renaming music file (".old").</li>
    <li><span class="bugid">REST: </span>Added Podcast methods.</li>
    <li><span class="bugid">REST: </span>Added bookmark methods.</li>
    <li><span class="bugid">REST: </span>Added getInternetRadioStations.</li>
    <li><span class="bugid">REST: </span>Added getGenres.</li>
    <li><span class="bugid">REST: </span>Added getSongsByGenre.</li>
    <li><span class="bugid">REST: </span>Added option to disable transcoding when streaming.</li>
    <li><span class="bugid">REST: </span>Fixed a bug in getAlbumList which caused it to return non-albums in some cases.</li>
    <li><span class="bugid">REST: </span>Support CORS.</li>
    <li><span class="bugid">REST: </span>Support "parent" attribute in getMusicDirectory.</li>
    <li><span class="bugid">Tech: </span>Install Java 7 rather than Java 6.</li>
</ul>

<a name="4.7"><h2 class="div">Subsonic 4.7 - Sep 13, 2012</h2></a>
<ul>
    <li><span class="bugid">New: </span>Auto-import playlists and update playlist statistics after scan.</li>
    <li><span class="bugid">New: </span>Upgraded to JW Player 5.10.</li>
    <li><span class="bugid">Bugfix: </span>Rescan looses comments and play statistics.</li>
    <li><span class="bugid">Bugfix: </span>Play queue scrolls to the top when clicking on links.</li>
    <li><span class="bugid">Bugfix: </span>MiniSub link uses wrong context path.</li>
    <li><span class="bugid">Bugfix: </span>Not possible to create random playlist with a "-" in the genre.</li>
    <li><span class="bugid">Bugfix: </span>Fails to import playlist if case is wrong.</li>
    <li><span class="bugid">REST: </span>Add timestamp to scrobble method, and support multiple entries.</li>
</ul>

<%@ include file="changelog-older.jsp" %>


</div>

<div id="side-col">
    <%@ include file="google-translate.jsp" %>
    <div class="sidebox">
        <h2>Releases</h2>
        <ul class="list">
            <li><a href="#4.8">Subsonic 4.8</a></li>
            <li><a href="#4.7">Subsonic 4.7</a></li>
            <li><a href="#4.7.beta3">Subsonic 4.7.beta3</a></li>
            <li><a href="#4.7.beta2">Subsonic 4.7.beta2</a></li>
            <li><a href="#4.7.beta1">Subsonic 4.7.beta1</a></li>
            <li><a href="#4.6">Subsonic 4.6</a></li>
            <li><a href="#4.6.beta2">Subsonic 4.6.beta2</a></li>
            <li><a href="#4.6.beta1">Subsonic 4.6.beta1</a></li>
            <li><a href="#4.5">Subsonic 4.5</a></li>
            <li><a href="#4.5.beta2">Subsonic 4.5.beta2</a></li>
            <li><a href="#4.5.beta1">Subsonic 4.5.beta1</a></li>
            <li><a href="#4.4">Subsonic 4.4</a></li>
            <li><a href="#4.4.beta1">Subsonic 4.4.beta1</a></li>
            <li><a href="#4.3">Subsonic 4.3</a></li>
            <li><a href="#4.3.beta1">Subsonic 4.3.beta1</a></li>
            <li><a href="#4.2">Subsonic 4.2</a></li>
            <li><a href="#4.2.beta1">Subsonic 4.2.beta1</a></li>
            <li><a href="#4.1">Subsonic 4.1</a></li>
            <li><a href="#4.1.beta1">Subsonic 4.1.beta1</a></li>
            <li><a href="#4.0.1">Subsonic 4.0.1</a></li>
            <li><a href="#4.0">Subsonic 4.0</a></li>
            <li><a href="#4.0.beta2">Subsonic 4.0.beta2</a></li>
            <li><a href="#4.0.beta1">Subsonic 4.0.beta1</a></li>
            <li><a href="#3.9">Subsonic 3.9</a></li>
            <li><a href="#3.9.beta1">Subsonic 3.9.beta1</a></li>
            <li><a href="#3.8">Subsonic 3.8</a></li>
            <li><a href="#3.8.beta1">Subsonic 3.8.beta1</a></li>
            <li><a href="#3.7">Subsonic 3.7</a></li>
            <li><a href="#3.7.beta1">Subsonic 3.7.beta1</a></li>
            <li><a href="#3.6">Subsonic 3.6</a></li>
            <li><a href="#3.6.beta2">Subsonic 3.6.beta2</a></li>
            <li><a href="#3.6.beta1">Subsonic 3.6.beta1</a></li>
            <li><a href="#3.5">Subsonic 3.5</a></li>
            <li><a href="#3.5.beta2">Subsonic 3.5.beta2</a></li>
            <li><a href="#3.5.beta1">Subsonic 3.5.beta1</a></li>
            <li><a href="#3.4">Subsonic 3.4</a></li>
            <li><a href="#3.4">Subsonic 3.4.beta1</a></li>
            <li><a href="#3.3">Subsonic 3.3</a></li>
            <li><a href="#3.3.beta1">Subsonic 3.3.beta1</a></li>
            <li><a href="#3.2">Subsonic 3.2</a></li>
            <li><a href="#3.2.beta1">Subsonic 3.2.beta1</a></li>
            <li><a href="#3.1">Subsonic 3.1</a></li>
            <li><a href="#3.1.beta2">Subsonic 3.1.beta2</a></li>
            <li><a href="#3.1.beta1">Subsonic 3.1.beta1</a></li>
            <li><a href="#3.0">Subsonic 3.0</a></li>
            <li><a href="#3.0.beta2">Subsonic 3.0.beta2</a></li>
            <li><a href="#3.0.beta1">Subsonic 3.0.beta1</a></li>
            <li><a href="#2.9">Subsonic 2.9</a></li>
            <li><a href="#2.9.beta1">Subsonic 2.9.beta1</a></li>
            <li><a href="#2.8">Subsonic 2.8</a></li>
            <li><a href="#2.8.beta1">Subsonic 2.8.beta1</a></li>
            <li><a href="#2.7">Subsonic 2.7</a></li>
            <li><a href="#2.6">Subsonic 2.6</a></li>
            <li><a href="#2.5">Subsonic 2.5</a></li>
            <li><a href="#2.4">Subsonic 2.4</a></li>
            <li><a href="#2.3">Subsonic 2.3</a></li>
            <li><a href="#2.2">Subsonic 2.2</a></li>
            <li><a href="#2.1">Subsonic 2.1</a></li>
            <li><a href="#2.0">Subsonic 2.0</a></li>
            <li><a href="#1.0">Subsonic 1.0</a></li>
            <li><a href="#0.1">Subsonic 0.1</a></li>
        </ul>
    </div>

    <%@ include file="premium-column.jsp" %>

</div>

<div class="clear">
</div>
</div>
<hr/>
<%@ include file="footer.jsp" %>
</div>


</body>
</html>
