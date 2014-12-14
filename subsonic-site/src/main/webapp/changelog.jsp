<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>
<%--
  ~ This file is part of Subsonic.
  ~
  ~  Subsonic is free software: you can redistribute it and/or modify
  ~  it under the terms of the GNU General Public License as published by
  ~  the Free Software Foundation, either version 3 of the License, or
  ~  (at your option) any later version.
  ~
  ~  Subsonic is distributed in the hope that it will be useful,
  ~  but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~  GNU General Public License for more details.
  ~
  ~  You should have received a copy of the GNU General Public License
  ~  along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.
  ~
  ~  Copyright 2014 (C) Sindre Mehus
  --%>

<!DOCTYPE HTML>
<html>
<%@ include file="head.jsp" %>

<body>

<c:import url="header.jsp"/>

<section id="main" class="container">
<header>
    <h2>Change Log</h2>
</header>
<div class="row">
<div class="12u">

<section class="box">
    <a name="5.0"><h3>Subsonic 5.0 - Sep 21, 2014</h3></a>
    <ul>
        <li><strong>Bugfix:</strong> Use UTF-8 encoding for filenames in ZIP archives. (Requires Java 7+)</li>
        <li><strong>Bugfix:</strong> Fixed problem with unresponsive dialogs in web interface.</li>
        <li><strong>Bugfix:</strong> Use 2000 kbps as default video bitrate.</li>
        <li><strong>Bugfix:</strong> Sort playlists alphabetically.</li>
        <li><strong>Bugfix:</strong> Fixed some sorting issues (e.g., for same artist found in multiple media folders).</li>
        <li><strong>Bugfix:</strong> Make password recovery work with https.</li>
    </ul>

    <a name="5.0.beta2"><h3>Subsonic 5.0.beta2 - Aug 26, 2014</h3></a>
    <ul>
        <li><strong>New:</strong> Support casting to remote Chromecasts (requires *.subsonic.org address)</li>
        <li><strong>New:</strong> Added video bitrate selector.</li>
        <li><strong>New:</strong> Make DLNA media server name configurable.</li>
        <li><strong>New:</strong> Updated Czech translation, courtesy of Trottel.</li>
        <li><strong>New:</strong> Enable compression for XML, JSON and JSONP in the REST API</li>
        <li><strong>New:</strong> Set X-Content-Duration to support opus encoding.</li>
        <li><strong>Bugfix:</strong> Fixed problem with whole tab being sent to Chromecast rather than just the video.</li>
        <li><strong>Bugfix:</strong> Access-Control-Allow-Origin header sometimes missing in REST responses.</li>
        <li><strong>Bugfix:</strong> Fixed DLNA recursion bug.</li>
        <li><strong>Bugfix:</strong> Fixed mixed content errors when changing cover art, and using https (courtesy of daneren2005)</li>
        <li><strong>Bugfix:</strong> Fixed errors on certain podcast feeds where they were being incorrectly labeled as video files (courtesy of daneren2005)</li>
        <li><strong>Bugfix:</strong> Updated ffmpeg commands to fix some transcoding issues.</li>
        <li><strong>Bugfix:</strong> Fixed bug in REST method changePassword</li>
        <li><strong>Bugfix:</strong> Avoid creating duplicate players when switching from Chromecast to local.</li>
    </ul>

    <%@ include file="changelog-older.jsp" %>


</section>
</div>
</div>
</section>

<%@ include file="footer.jsp" %>

</body>
</html>