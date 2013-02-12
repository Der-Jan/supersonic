<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>
<%--@elvariable id="command" type="net.sourceforge.subsonic.command.DonateCommand"--%>
<html>
<head>
    <%@ include file="head.jsp" %>
</head>
<body class="mainframe bgcolor1">

<h1>
    <img src="<spring:theme code="donateImage"/>" alt=""/>
    <fmt:message key="premium.title"/>
</h1>
<c:if test="${not empty command.path}">
    <sub:url value="main.view" var="backUrl">
        <sub:param name="path" value="${command.path}"/>
    </sub:url>
    <div class="back"><a href="${backUrl}">
        <fmt:message key="common.back"/>
    </a></div>
    <br/>
</c:if>

<div style="width:50em; max-width:50em">

<fmt:message key="premium.text"/>

    <p style="font-size:1.2em;padding-left:2em"><b><a href="http://subsonic.org/pages/premium.jsp" target="_blank">&raquo; <fmt:message key="premium.getpremium"/></a></b></p>

    <c:choose>
    <c:when test="${command.licenseValid}">
        <p>
            <b>
                <fmt:formatDate value="${command.licenseDate}" dateStyle="long" var="licenseDate"/>
                <fmt:message key="premium.licensed">
                    <fmt:param value="${command.emailAddress}"/>
                    <fmt:param value="${licenseDate}"/>
                    <fmt:param value="${command.brand}"/>
                </fmt:message>
        </p>
    </c:when>
    <c:otherwise>

        <p><fmt:message key="premium.register"/></p>

        <form:form commandName="command" method="post" action="donate.view">
            <form:hidden path="path"/>
            <table>
                <tr>
                    <td><fmt:message key="premium.register.email"/></td>
                    <td>
                        <form:input path="emailAddress" size="40"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="premium.register.license"/></td>
                    <td>
                        <form:input path="license" size="40"/>
                    </td>
                    <td><input type="submit" value="<fmt:message key="common.ok"/>"/></td>
                </tr>
                <tr>
                    <td/>
                    <td class="warning"><form:errors path="license"/></td>
                </tr>
            </table>
        </form:form>

        <p><fmt:message key="premium.resend"/></p>

    </c:otherwise>
</c:choose>

</div>
</body>
</html>