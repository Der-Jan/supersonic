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

<body onload="setTimeout('redirect()', 1500);">
<!-- Google Code for Download Conversion Page -->
<script type="text/javascript">
    /* <![CDATA[ */
    var google_conversion_id = 1068027807;
    var google_conversion_language = "en";
    var google_conversion_format = "3";
    var google_conversion_color = "ffffff";
    var google_conversion_label = "REx2CIW81AEQn5-j_QM";
    var google_conversion_value = 0;
    /* ]]> */
</script>
<script type="text/javascript" src="http://www.googleadservices.com/pagead/conversion.js">
</script>
<noscript>
    <div style="display:inline;">
        <img height="1" width="1" style="border-style:none;" alt="" src="http://www.googleadservices.com/pagead/conversion/1068027807/?label=REx2CIW81AEQn5-j_QM&amp;guid=ON&amp;script=0"/>
    </div>
</noscript>

<script type="text/javascript">
    function redirect() {
        window.location = "http://prdownloads.sourceforge.net/subsonic/<%=request.getParameter("target")%>";
    }
</script>

<c:import url="header.jsp"/>

<section id="main" class="container">
<header>
    <h3>Please wait, contacting SourceForge download center...</h3>
</header>

</section>
</body>
</html>