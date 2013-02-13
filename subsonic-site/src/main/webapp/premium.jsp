<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<%! String current = "premium"; %>
<%@ include file="header.jsp" %>

<body>

<a name="top"/>

<div id="container">
    <%@ include file="menu.jsp" %>

    <div id="content">
        <div id="main-col">
            <h1>Subsonic Premium</h1>


            TODO:

            TRIAL
            PICTURES


            <%--TODO: Feature matrix?--%>
            <p>Upgrade to Subsonic Premium to enjoy these extra features:</p>

            <div class="floatcontainer margin10-t margin10-b">
                <ul class="stars column-left">
                    <li><a href="apps.jsp">Apps</a> for Android, iPhone, Windows Phone, PlayBook, Roku, Mac, Chrome and more*.</li>
                    <li>Video streaming.</li>
                    <li>Podcast receiver.</li>
                    <li>No ads in the web interface.</li>
                </ul>
                <ul class="stars column-right">
                    <li>Your personal server address: <em>yourname</em>.subsonic.org</li>
                    <li>Share your media on Facebook, Twitter, Google+.</li>
                    <li>Other features to be released later.</li>
                </ul>
            </div>

            <p style="font-size:9px;">* Some apps must be purchased separately or are ad-supported.</p>


            <table style="padding-top:1em;padding-bottom:2em;width:90%">
                <tr>
                    <td style="font-size:26pt;padding:20pt">1</td>
                    <td>
                        <div style="font-size:14pt">Buy</div>
                        <div style="padding-top:5pt">Select a payment option below to go to PayPal where you can pay by credit card or by using your PayPal account.</div>
                    </td>
                </tr>
                <tr>
                    <td style="font-size:26pt;padding:20pt">2</td>
                    <td>
                        <div style="font-size:14pt">Receive</div>
                        <div style="padding-top:5pt">You'll receive the license key by email within a few minutes.</div>
                    </td>
                </tr>
                <tr>
                    <td style="font-size:26pt;padding:20pt">3</td>
                    <td>
                        <div style="font-size:14pt">Register</div>
                        <div style="padding-top:5pt"><a href="getting-started.jsp#3">Register</a> the license key on your Subsonic server to unlock all the premium features.</div>
                    </td>
                </tr>

            </table>

            <p>

                As a donor you will receive a license key which is valid for personal, non-commercial use for this and all future releases of Subsonic. For commercial use, please contact us for licensing options.
            </p>

            If you have any questions, please send an email to subsonic_donation@activeobjects.no.



        </div>

        <div id="side-col">
            <%@ include file="google-translate.jsp" %>
            <%@ include file="download-subsonic.jsp" %>
        </div>

        <div class="clear">
        </div>
    </div>
    <hr/>
    <%@ include file="footer.jsp" %>
</div>


</body>
</html>
