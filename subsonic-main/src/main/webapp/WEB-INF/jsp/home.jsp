<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>

    <script type="text/javascript" language="javascript">
        function init() {
            <c:if test="${model.listType eq 'random'}">
            setTimeout("refresh()", 20000);
            </c:if>
        }

        function refresh() {
            top.main.location.href = top.main.location.href;
        }
    </script>
</head>
<body class="mainframe bgcolor1" onload="init();">
<h1>
    <img src="<spring:theme code="homeImage"/>" alt="">
    <span style="vertical-align: middle">${model.welcomeTitle}</span>
</h1>

<c:if test="${not empty model.welcomeSubtitle}">
    <h2>${model.welcomeSubtitle}</h2>
</c:if>

<h2>
    <c:forTokens items="newest random starred highest frequent recent decade genre alphabetical users" delims=" " var="cat" varStatus="loopStatus">
        <c:if test="${loopStatus.count > 1}">&nbsp;|&nbsp;</c:if>
        <sub:url var="url" value="home.view">
            <sub:param name="listType" value="${cat}"/>
        </sub:url>

        <c:choose>
            <c:when test="${model.listType eq cat}">
                <span class="headerSelected"><fmt:message key="home.${cat}.title"/></span>
            </c:when>
            <c:otherwise>
                <span class="header"><a href="${url}"><fmt:message key="home.${cat}.title"/></a></span>
            </c:otherwise>
        </c:choose>

    </c:forTokens>
</h2>

<c:if test="${model.isIndexBeingCreated}">
    <p class="warning"><fmt:message key="home.scan"/></p>
</c:if>

<%@ include file="homePager.jsp" %>

<table style="width: 100%">
    <tr>
        <td style="vertical-align:top;">
            <c:choose>
                <c:when test="${model.listType eq 'users'}">
                    <table>
                        <tr>
                            <th><fmt:message key="home.chart.total"/></th>
                            <th><fmt:message key="home.chart.stream"/></th>
                        </tr>
                        <tr>
                            <td><img src="<c:url value="/userChart.view"><c:param name="type" value="total"/></c:url>" alt=""></td>
                            <td><img src="<c:url value="/userChart.view"><c:param name="type" value="stream"/></c:url>" alt=""></td>
                        </tr>
                        <tr>
                            <th><fmt:message key="home.chart.download"/></th>
                            <th><fmt:message key="home.chart.upload"/></th>
                        </tr>
                        <tr>
                            <td><img src="<c:url value="/userChart.view"><c:param name="type" value="download"/></c:url>" alt=""></td>
                            <td><img src="<c:url value="/userChart.view"><c:param name="type" value="upload"/></c:url>" alt=""></td>
                        </tr>
                    </table>

                </c:when>
                <c:otherwise>

                    <div>
                        <c:forEach items="${model.albums}" var="album" varStatus="loopStatus">

                            <div class="albumThumb">
                                <c:import url="coverArt.jsp">
                                    <c:param name="albumId" value="${album.id}"/>
                                    <c:param name="albumName" value="${album.albumTitle}"/>
                                    <c:param name="coverArtSize" value="${model.coverArtSize}"/>
                                    <c:param name="showLink" value="true"/>
                                    <c:param name="showZoom" value="false"/>
                                    <c:param name="showChange" value="false"/>
                                    <c:param name="appearAfter" value="${loopStatus.count * 30}"/>
                                </c:import>

                                <div class="detail">
                                    <c:if test="${not empty album.playCount}">
                                        <fmt:message key="home.playcount"><fmt:param value="${album.playCount}"/></fmt:message>
                                    </c:if>
                                    <c:if test="${not empty album.lastPlayed}">
                                        <fmt:formatDate value="${album.lastPlayed}" dateStyle="short" var="lastPlayedDate"/>
                                        <fmt:message key="home.lastplayed"><fmt:param value="${lastPlayedDate}"/></fmt:message>
                                    </c:if>
                                    <c:if test="${not empty album.created}">
                                        <fmt:formatDate value="${album.created}" dateStyle="short" var="creationDate"/>
                                        <fmt:message key="home.created"><fmt:param value="${creationDate}"/></fmt:message>
                                    </c:if>
                                    <c:if test="${not empty album.year}">
                                        ${album.year}
                                    </c:if>
                                    <c:if test="${not empty album.rating}">
                                        <c:import url="rating.jsp">
                                            <c:param name="readonly" value="true"/>
                                            <c:param name="rating" value="${album.rating}"/>
                                        </c:import>
                                    </c:if>
                                </div>

                                <c:choose>
                                    <c:when test="${empty album.artist and empty album.albumTitle}">
                                        <div class="detail"><fmt:message key="common.unknown"/></div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="detail"><b><str:truncateNicely lower="22" upper="22">${album.artist}</str:truncateNicely></b></div>
                                        <div class="detail"><str:truncateNicely lower="22" upper="22">${album.albumTitle}</str:truncateNicely></div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>

                </c:otherwise>
            </c:choose>
        </td>
        <c:if test="${not empty model.welcomeMessage}">
            <td style="vertical-align:top;width:20em">
                <div style="padding:0 1em 0 1em;border-left:1px solid #<spring:theme code="detailColor"/>">
                    <sub:wiki text="${model.welcomeMessage}"/>
                </div>
            </td>
        </c:if>
    </tr>
</table>

<c:if test="${model.listSize eq fn:length(model.albums)}">
    <%@ include file="homePager.jsp" %>
</c:if>

</body></html>
