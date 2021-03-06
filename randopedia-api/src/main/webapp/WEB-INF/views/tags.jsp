<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta name="fragment" content="!">
    <!-- Set the viewport width to device width for mobile -->
    <meta name="viewport" content="width=device-width" />
    
    <title>Randopedia - The ski tour encyclopedia</title>
    
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
    <h3>
            Here are the top tags for randopedia.net!
        </h3>
    <c:forEach items="${tags}" var="tag">
        <a href="../#!/tags/<c:out value="${tag.name}"/>">#<c:out value="${tag.name}"/></a>
    </c:forEach>

</div>
</div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
</body>
</html>