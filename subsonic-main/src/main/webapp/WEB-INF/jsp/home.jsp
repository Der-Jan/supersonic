<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html><head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
    <link href="<c:url value="/style/shadow.css"/>" rel="stylesheet">

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
    <c:forTokens items="random newest starred highest frequent recent decade genre alphabetical users" delims=" " var="cat" varStatus="loopStatus">
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

<table>
    <tr>
        <c:choose>
            <c:when test="${model.listType eq 'random'}">
                <td><div class="forward"><a href="home.view?listType=random"><fmt:message key="common.more"/></a></div></td>
            </c:when>
            <c:when test="${model.listType ne 'users'}">
                <sub:url value="home.view" var="previousUrl">
                    <sub:param name="listType" value="${model.listType}"/>
                    <sub:param name="listOffset" value="${model.listOffset - model.listSize}"/>
                    <sub:param name="genre" value="${model.genre}"/>
                    <sub:param name="decade" value="${model.decade}"/>
                </sub:url>
                <sub:url value="home.view" var="nextUrl">
                    <sub:param name="listType" value="${model.listType}"/>
                    <sub:param name="listOffset" value="${model.listOffset + model.listSize}"/>
                    <sub:param name="genre" value="${model.genre}"/>
                    <sub:param name="decade" value="${model.decade}"/>
                </sub:url>

                <td style="padding-right:0.5em">
                    <fmt:message key="home.albums">
                        <fmt:param value="${model.listOffset + 1}"/>
                        <fmt:param value="${model.listOffset + fn:length(model.albums)}"/>
                    </fmt:message>
                </td>

                <c:if test="${model.listOffset gt 0}">
                    <td><a href="${previousUrl}"><img src="<spring:theme code="backImage"/>" alt=""></a></td>
                </c:if>

                <c:if test="${fn:length(model.albums) eq model.listSize}">
                    <td><a href="${nextUrl}"><img src="<spring:theme code="forwardImage"/>" alt=""></a></td>
                </c:if>

                <c:if test="${model.listType eq 'decade'}">
                    <td style="padding-left: 2em">
                        <fmt:message key="home.decade.text"/>
                    </td>
                    <td>
                        <select name="decade"
                                onchange="location='home.view?listType=${model.listType}&amp;listOffset=${model.listOffset}&amp;decade=' + options[selectedIndex].value">
                            <c:forEach items="${model.decades}" var="decade">
                                <option
                                    ${decade eq model.decade ? "selected" : ""} value="${decade}">${decade}</option>
                            </c:forEach>
                        </select>
                    </td>
                </c:if>
                <c:if test="${model.listType eq 'genre'}">
                    <td style="padding-left: 2em">
                        <fmt:message key="home.genre.text"/>
                    </td>
                    <td>
                        <select name="genre" onchange="location='home.view?listType=${model.listType}&amp;listOffset=${model.listOffset}&amp;genre=' + options[selectedIndex].value">
                            <c:forEach items="${model.genres}" var="genre">
                                <option ${genre eq model.genre ? "selected" : ""} value="${genre}">${genre}</option>
                            </c:forEach>
                        </select>
                    </td>
                </c:if>
            </c:when>
        </c:choose>
    </tr>
</table>

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

                    <div id="albumList">
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

</body></html>
