<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="detail ellipsis">
    <i class="fa fa-folder-open icon"></i>
    <a href="artists.view?musicFolderId=${musicFolder.id}">${fn:escapeXml(musicFolder.name)}</a>
    <c:forEach items="${ancestors}" var="ancestor">
        &nbsp;&bull;&nbsp; <a href="main.view?id=${ancestor.id}">${fn:escapeXml(ancestor.name)}</a>
    </c:forEach>
</div>
