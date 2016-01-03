<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>

<html>
<head>
    <%@ include file="head.jsp" %>
    <script type="text/javascript" src="<c:url value="/script/jwplayer-7.2.4/jwplayer.js"/>"></script>
    <script type="text/javascript">jwplayer.key="fnCY1zPzsH/DE/Uo+pvsBes6gTdfOCcLCCnD6g==";</script>

    <script type="text/javascript">

        function createPlayer() {
            jwplayer("jwplayer").setup({
                file: "foo.mp3",
//                skin: "five",
                height: 160,
//                primary: "flash",
                width: "100%"
                //            skin: {
                //                name: "five",
                //                active: "red",
                //                inactive: "green",
                //                background: "#00121212"
                //            }
            });
        }
    </script>
</head>

<body onload="createPlayer()">

<div id="jwplayer"></div>

</body>
</html>