<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<meta name="description" content="Browse or search for ski tours all over the world. Contribute by adding your tours to the ultimate ski tour database.">
    
    <!-- Set the viewport width to device width for mobile -->
  	<meta name="viewport" content="width=device-width" />
  	
    <title>Randopedia - The ski tour encyclopedia</title>
    
    <link rel="stylesheet" href="css/foundation.min.css">
    <link rel="stylesheet" href="css/general_foundicons.css">
    <link rel="stylesheet" href="css/bjqs.css">
    <link rel="stylesheet" href="css/normalize.css">
    <link rel="stylesheet" href="css/randopedia.css">
    <link rel="stylesheet" href="css/randopedia-icons.css">
    
    
    <script src="js/libs/custom.modernizr.js"></script>
    <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAUVOlVv0_7EObpu2DbbvAKbo1Bjio9yQQ&sensor=false&libraries=drawing"></script>
    <script type="text/javascript">
        window.ENV = window.ENV || {};
        ENV.EXPERIMENTAL_CONTROL_HELPER = true;    
    </script>
</head>

<body class="off-canvas hide-extras">

    <noscript>
        Randopedia is best used with Javascript enabled.</br>
        Browse ski tours<br/>
        <c:forEach items="${areas}" var="area">
            <a href="/#!/areas/<c:out value="${area.clientId}"/>"><c:out value="${area.name}"/></a>
        </c:forEach>
    </noscript>
    <script src="http://code.jquery.com/jquery-1.9.1.min.js"></script>
    <script src="js/libs/foundation.min.js"></script>
    <script src="js/libs/bjqs-1.3.js"></script>
    <script src="js/libs/moment.min.js"></script>
    <script src="js/libs/markerclusterer.js"></script>
    <script src="js/libs/oms.min.js"></script>
    <script src="js/libs/marked.js"></script>
    <script src="js/randopedia.min.js"></script>
    <script src="js/templates.js"></script>
    
    <!-- Piwik -->
    <script type="text/javascript">
     if((document.location.hostname.indexOf("randopedia") != -1)){
         var _paq = _paq || [];
         _paq.push(['trackPageView']);
         _paq.push(['enableLinkTracking']);
         (function() {
           var u=(("https:" == document.location.protocol) ? "https" : "http") + "://donc.se/randopedia-stats/";
           _paq.push(['setTrackerUrl', u+'piwik.php']);
           _paq.push(['setSiteId', 1]);
           var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0]; g.type='text/javascript';
           g.defer=true; g.async=true; g.src=u+'piwik.js'; s.parentNode.insertBefore(g,s);
         })(); 
     }
    </script>
    <noscript><p><img src="http://donc.se/randopedia-stats/piwik.php?idsite=1" style="border:0;" alt="" /></p></noscript>
    <!-- End Piwik Code -->
</body>
</html>