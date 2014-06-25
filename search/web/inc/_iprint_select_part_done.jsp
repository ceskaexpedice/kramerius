<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@page trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>


<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>


<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="cz.incad.kramerius.utils.RESTHelper"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager"%>
<%@page import="cz.incad.kramerius.processes.DefinitionManager"%>



<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

    <script src="../js/jquery-1.5.1.min.js" type="text/javascript" ></script>
    <script src="../js/jquery-ui-1.8.11.custom.min.js" language="javascript" type="text/javascript"></script>

    <script src="../js/jquery.mousewheel.js" type="text/javascript" ></script>
    <script src="../js/jquery.splitter.js" type="text/javascript" ></script>
    <link rel="stylesheet" href="../css/localprint/print.css" type="text/css" media="print"/>



<view:kconfig var="a4ratio" key="search.print.pageratio" defaultValue="1.414" />

<script language="JavaScript" type="text/javascript">


    var transcode = ${param['transcode']};
    var pid = "${param['pid']}";
    //xpos=0.3&ypos=0.3&width=0.3&height=0.2
    var width = ${param['width']};
    var height = ${param['height']};
    var xpos = ${param['xpos']};
    var ypos = ${param['ypos']};



    
    if(transcode == null) {
        transcode = false;
    }

    $(document).ready(function(){

        var divelm = $("<div/>");
        divelm.addClass("image");

        var url = "../imgcut?pid="+encodeURIComponent(pid)+"&xpos="+xpos+"&ypos="+ypos+"&width="+width+"&height="+height;
            
        var imgelm = $("<img/>",{"src":url});
        imgelm.load(function() {

            var h = this.naturalHeight;
            var w = this.naturalWidth;

            var ratio = h/w;
            var a4ratio = ${a4ratio};
            if (ratio < a4ratio) {
                // na sirku
                $(this).css('width',"100%");
            } else {
                // na vysku
                $(this).css('height',"100%");
            }
        });

        divelm.append(imgelm);
        $("body").append(divelm);

        window.print();
    });
</script>

</head>
    <body>
    </body>
</html>
