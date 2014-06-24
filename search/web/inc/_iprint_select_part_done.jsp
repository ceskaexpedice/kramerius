<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@page trimDirectiveWhitespaces="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>


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



<script language="JavaScript" type="text/javascript">

    var size = {
                    'a4':{
                            'portrait':{'widthstyle':'210mm','width':210, 'heightstyle':'297mm','height':297},
                            'landscape':{'widthstyle':'297mm', 'width':297,'heightstyle':'210mm','height':210}
                         },
                    'a3':{
                            'portrait':{'widthstyle':'297mm','width':297,'heightstyle':'420mm',height:420},
                            'landscape':{'widthstyle':'420mm','width':420,'heightstyle':'297mm',height:297}
                         }
    };

    var transcode = ${param['transcode']};
    var pid = "${param['pid']}";
    //xpos=0.3&ypos=0.3&width=0.3&height=0.2
    var width = ${param['width']};
    var height = ${param['height']};
    var xpos = ${param['xpos']};
    var ypos = ${param['ypos']};

    var page = "${param['page']}";
    var layout = "${param['layout']}";

    
    if(transcode == null) {
        transcode = false;
    }

    $(document).ready(function(){

        var divelm = $("<div/>");
        divelm.addClass("image");

        var url = "../imgcut?pid="+encodeURIComponent(pid)+"&xpos="+xpos+"&ypos="+ypos+"&width="+width+"&height="+height;
            
        var imgelm = $("<img/>",{"src":url});
        imgelm.load(function() {
            
            var cssPagedMedia = (function () {
                var style = document.createElement('style');
                document.head.appendChild(style);
                    return function (rule) {
                    style.innerHTML = rule;
                };
            }());


            cssPagedMedia.size = function (size) {
                cssPagedMedia('@page {size: ' + size + '; margin: 0mm;}');
            };
            var selected = size[page][layout];
            cssPagedMedia.size(selected.widthstyle + ' '+ selected.heightstyle);
            

            var h = this.naturalHeight;
            var w = this.naturalWidth;

            var pomer = h/w;
            
            console.log("selected width =="+selected.width);
            console.log("selected height =="+selected.height);
            
            var nwidth = selected.width*0.9;
            var nheight = pomer * nwidth;
            if (nheight > selected.height) {
                nheight = selected.height*0.9;
                nwidth = nheight / pomer;
            }

            $(this).css('width',nwidth+"mm");
            $(this).css('height',nheight+"mm");
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
