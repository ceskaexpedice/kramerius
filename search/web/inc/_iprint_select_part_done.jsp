<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@page trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>


<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>


<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="cz.incad.kramerius.utils.RESTHelper"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager"%>
<%@page import="cz.incad.kramerius.processes.DefinitionManager"%>


<view:object name="disPicture" clz="cz.incad.Kramerius.views.localprint.DisectionPrepareViewObject"></view:object>


<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

    <script src="../js/jquery-1.5.1.min.js" type="text/javascript" ></script>
    <script src="../js/jquery-ui-1.8.11.custom.min.js" language="javascript" type="text/javascript"></script>

    <script src="../js/jquery.mousewheel.js" type="text/javascript" ></script>
    <script src="../js/jquery.splitter.js" type="text/javascript" ></script>
<style type="text/css">

body 
    {
        background-color:#FFFFFF; 
        margin: 0px;  /* the margin on the content before printing */
}

@media print
{
    .image {
        text-align:center;
    }

    .image img {
        margin:auto;
    }


    @page {margin: 0mm;}

    <c:forEach items="${disPicture.styles}" var="style" varStatus="status">
            <c:out value="${style}"></c:out>
    </c:forEach>    
}
@media screen
{
    <c:forEach items="${disPicture.styles}" var="style" varStatus="status">
            <c:out value="${style}"></c:out>
    </c:forEach>    
}
</style>

<script language="JavaScript" type="text/javascript">
    $(document).ready(function(){
        window.print();
    });
</script>

</head>
    <body>
        <c:forEach items="${disPicture.imgelements}" var="imgelm" varStatus="status">
            <div class="image">
                ${imgelm}
            </div>
        </c:forEach>
    </body>
</html>
