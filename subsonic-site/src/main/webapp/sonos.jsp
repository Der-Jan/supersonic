<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>

<!DOCTYPE HTML>
<html>
<%@ include file="head.jsp" %>

<body>

<c:import url="header.jsp"/>

<section id="main" class="container">
    <header>
        <h2>Sonos</h2>

        <p>Own a Sonos? Subsonic is for you.</p>
    </header>

    <section class="box">
        <span class="image featured"><img src="inc/img/sonos/sonos-header.png" alt="" /></span>
        <p>
            Subsonic comes with built-in support for your existing Sonos players. Access your personal music library
            like any other Sonos music service like Spotify or Google Play.
        </p>

        <h3>Features</h3>
        <div class="row">
            <div class="6u 12u(3)">
                <ul>
                    <li>Works with giant music libraries. Not limited to 65,000 songs like the native Sonos controller.</li>
                    <li>Doesn't require installation of the Sonos controller.</li>
                    <li>Access your personal Subsonic playlists (as well as Sonos playlists).</li>
                    <li>Star your favorites.</li>
                    <li>Audioscrobbling and play statistics.</li>
                    <li>Powerful search feature.</li>
                </ul>
            </div>

            <div class="6u 12u(3)">
                <ul>
                    <li>The Subsonic web app shows who is playing what on Sonos.</li>
                    <li>Artist radio for playing similar songs.</li>
                    <li>Shuffle play whole library or by artist.</li>
                    <li>Album lists: Random, Recently added, Starred, Top rated, Most Played, Recently played, By decade, By genre.</li>
                    <li>Same folder structure / album art / media meta data as in the Subsonic web app</li>
                </ul>
            </div>
        </div>

        <h3>Setting it up</h3>
        <p>
            Connecting Sonos to your Subsonic server is super easy:
        </p>
        <ol>
            <li>In the Subsonic web page, enable the Sonos music service in <b>Settings &gt; Sonos</b>.</li>
            <li>In the Sonos controller, click <b>Add Music Services</b> and select Subsonic.</li>
            <li>Select <b>I already have an account</b>, then enter your Subsonic username and password.</li>
            <li>You're done. The list of music sources now includes the Subsonic music service.</li>
        </ol>

        <span class="image left"><img src="inc/img/sonos/sonos-1.png" alt=""/></span>
        <span class="image left"><img src="inc/img/sonos/sonos-2.png" alt=""/></span>
        <span class="image left"><img src="inc/img/sonos/sonos-3.png" alt=""/></span>
        <span class="image left"><img src="inc/img/sonos/sonos-4.png" alt=""/></span>

        <div style="height:800px"></div>
    </section>

</section>


<%@ include file="footer.jsp" %>

</body>
</html>