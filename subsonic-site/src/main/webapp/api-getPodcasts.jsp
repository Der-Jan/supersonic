<h2 class="div"><a name="getPodcasts"></a>getPodcasts</h2>

<p>
    <code>http://your-server/rest/getPodcasts.view</code>
    <br>Since <a href="#versions">1.6.0</a>
</p>

<p>
    Returns all podcast channels the server subscribes to and their episodes. Takes no extra parameters.
</p>

<p>
    Returns a <code>&lt;subsonic-response&gt;</code> element with a nested <code>&lt;podcasts&gt;</code>
    element on success. <a href="https://sourceforge.net/p/subsonic/code/HEAD/tree/trunk/subsonic-main/src/main/webapp/xsd/podcasts_example_1.xml">Example</a>.
</p>

<h2 class="div">getShares</h2>
<p>
    <code>http://your-server/rest/getShares.view</code>
    <br>Since <a href="#versions">1.6.0</a>
</p>
<p>
    Returns information about shared media this user is allowed to manage. Takes no extra parameters.
</p>
<p>
    Returns a <code>&lt;subsonic-response&gt;</code> element with a nested <code>&lt;shares&gt;</code>
    element on success. <a href="https://sourceforge.net/p/subsonic/code/HEAD/tree/trunk/subsonic-main/src/main/webapp/xsd/shares_example_1.xml">Example</a>.
</p>
