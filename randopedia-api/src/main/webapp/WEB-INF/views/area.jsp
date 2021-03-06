<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta name="fragment" content="!">
    <!-- Set the viewport width to device width for mobile -->
    <meta name="viewport" content="width=device-width" />
    
    <title><c:out value="${area.name}"/> | Randopedia - The ski tour encyclopedia</title>
    
    <link rel="stylesheet" href="/css/foundation.min.css">
    <link rel="stylesheet" href="/css/general_foundicons.css">
    <link rel="stylesheet" href="/css/bjqs.css">
    <link rel="stylesheet" href="/css/normalize.css">
    <link rel="stylesheet" href="/css/randopedia.css">
    <link rel="stylesheet" href="/css/randopedia-icons.css">
    <script src="/js/libs/custom.modernizr.js"></script>
</head>


<body class="off-canvas hide-extras">

<%@ include file="/WEB-INF/views/header.jsp" %>

<div class="body-wrapper">
    <div class="row">
        <div class="small-12 columns main-content">
            <h3><c:out value="${area.name}"/><span class="secondary-text"><a href="../#!/areas/<c:out value="${parent.clientId}"/>"><c:out value="${parent.name}"/></a></span></h3>
            <div>
                <c:out value="${description}" escapeXml="false"/>
            </div>
            <h4 class="subheader">Areas</h4>
            <div>
                <ul class="inline-list">
                <c:forEach items="${subAreas}" var="area">
                    <li>
                        <a href="../#!/areas/<c:out value="${area.clientId}"/>"><c:out value="${area.name}"/></a>
                    </li>
                </c:forEach>
                </ul>
            </div>
            
            
            <h4 class="subheader">Tours</h4>
            <c:forEach items="${tours}" var="tour">
                <div class="row">
                    <div class="large-12 columns">
                        <a href="../#!/tours/<c:out value="${tour.clientId}"/>"><c:out value="${tour.name}"/></a> 
                        <p>
                        <c:out value="${tour.shortDescription}"/>
                        </p>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
</body>
</html>