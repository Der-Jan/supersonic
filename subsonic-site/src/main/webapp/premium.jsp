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

            <h3 style="padding-top: 1em;padding-bottom: 1em">Upgrade to Subsonic Premium to enjoy these extra features:</h3>

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

            <p>The basic version of Subsonic is free. When you first install Subsonic, the premium features are available for 30 days so
                you can try them out before deciding to upgrade.</p>

            <table style="padding-top:1em;padding-bottom:1.7em;width:90%">
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

            <table style="padding-left:3em; padding-bottom: 2em">
                <tr>
                    <th style="padding-bottom: 0.6em;padding-right: 3em">$2 per month</th>
                    <th style="padding-bottom: 0.6em;padding-right: 3em">$9 per year</th>
                    <th style="padding-bottom: 0.6em">$39 for lifetime</th>
                </tr>
                <tr>
                    <td style="padding-right: 3em">
                        <form action="https://www.paypal.com/cgi-bin/webscr" method="post">
                            <input type="hidden" name="cmd" value="_s-xclick">
                            <input type="hidden" name="hosted_button_id" value="FZS3NADTYABNC">
                            <input type="image" src="https://www.paypalobjects.com/en_US/NO/i/btn/btn_subscribeCC_LG.gif" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!">
                            <img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" width="1" height="1">
                        </form>
                    </td>
                    <td style="padding-right: 3em">
                        <form action="https://www.paypal.com/cgi-bin/webscr" method="post">
                            <input type="hidden" name="cmd" value="_s-xclick">
                            <input type="hidden" name="hosted_button_id" value="SDHSJ5T5E2DC4">
                            <input type="image" src="https://www.paypalobjects.com/en_US/NO/i/btn/btn_subscribeCC_LG.gif" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!">
                            <img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" width="1" height="1">
                        </form>
                    </td>
                    <td>
                        <form action="https://www.paypal.com/cgi-bin/webscr" method="post">
                            <input type="hidden" name="cmd" value="_s-xclick">
                            <input type="hidden" name="hosted_button_id" value="PQRZ7FEEXDAKA">
                            <input type="image" src="https://www.paypalobjects.com/en_US/NO/i/btn/btn_buynowCC_LG.gif" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!">
                            <img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" width="1" height="1">
                        </form>

                    </td>
                </tr>
            </table>

            <p>
                All payment options include free upgrades to new Subsonic versions.
            </p>
            <p>
                Note: The Subsonic Premium license is valid for personal, non-commercial use. For commercial use, please <a href="mailto:mail@subsonic.org">contact us</a> for licensing options.
            </p>

            <p>
                If you have any questions, please send an email to <a href="mailto:mail@subsonic.org">mail@subsonic.org</a>
            </p>

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
