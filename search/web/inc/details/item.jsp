<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="cz.incad.Kramerius.views.item.ItemViewObject"%>
<%@page import="cz.incad.Kramerius.views.item.menu.ItemMenuViewObject"%>
<style type="text/css">
    #split {
        height: 700px;
        width: 1000px;
    }
    #preview {
        overflow: hidden;
    }
    #rightPanel {
        overflow: hidden;
        width:300px;
    }

    #rightPanel>ul{
        margin: 0;
        padding-left: 0;
    }
    #rightPanel li{
        list-style-type: none;
        margin: 0;
        padding: 0;
        line-height: 16px;
    }

    #rightPanel li>span{
        width: 16px;
        height: 16px;
        overflow:hidden;
        text-indent: -99999px;
        display:block;
        float:left;
    }
</style>
<div id="split" class="viewer" style="position:relative;"> 
<div id="thumbs"  class="ui-layout-north">
    <%@ include file="thumbs.jsp" %>
</div>

    <div id="rightPanel"  class="ui-layout-west" >
        <%@ include file="tree.jsp" %>
    </div>
    <div id="preview" class="ui-layout-center" >
        <%@include  file="tabs/container.jsp" %>
    </div>
    <div style="clear:both;display:block;"></div>
</div>

<script type="text/javascript">
    var viewerOptions = { deepZoomGenerated:false, 
                          deepZoomCofigurationEnabled:false, 
                          mimeType:'', 
                          status:200,
                          uuid:'',
                          isContentPDF:function() {return viewerOptions.mimeType=='application/pdf'},
                          isContentDJVU:function() { return viewerOptions.mimeType.indexOf('djvu')> 0 }
    };
    var sp;
    $(document).ready(function(){
        
        var w = $("#split").height() +
            $(window).height() -
            $("#main").height() - 
            $("#footer").outerHeight(true);
        $("#split").css("height", w);
        w = w -
            $("#thumbs").outerHeight(true) -
            $("#centralContent>ul").outerHeight(true) - 8 - 5 - 6;
        //w = $("#preview>div.ui-tabs-panel:first").height() +
        //    $(window).height() -
        //    $("#main").height() - 
        //    $("#footer").outerHeight(true);
        $("#rightMenuBox>div.ui-tabs-panel").css("overflow", "auto");
        $("#rightMenuBox>div.ui-tabs-panel").css("padding", "3px");
        $("#rightMenuBox>div.ui-tabs-panel").css("height", w);
        $("#centralContent>div.ui-tabs-panel").css("overflow", "auto");
        $("#centralContent>div.ui-tabs-panel").css("padding", "3px");
        $("#centralContent>div.ui-tabs-panel").css("height", w);
        
        
       sp = $("#split").layout({
           north:{
                togglerLength_closed:	'100%',
                togglerLength_open:	'100%',
                togglerTip_open: '<fmt:message bundle="${lctx}">thumbs.showhide</fmt:message>',
                onopen_end: function(){
                    //positionAlto();
                },
                onclose_end: function(){
                    //positionAlto();
                }
           },
            west:{
                size:300,
                spacing_closed:	5,
                spacing_open:	5,
                togglerLength_closed:	'100%',
                togglerLength_open:	'100%',
                togglerAlign_open:	"top",
                togglerAlign_closed:	"top",
                togglerTip_closed: '<fmt:message bundle="${lctx}">item.showhide</fmt:message>',
                togglerTip_open: '<fmt:message bundle="${lctx}">item.showhide</fmt:message>',
                //togglerContent_closed:	'<div id="showHideRightMenu" class="shadow" ><span class="ui-state-default ui-icon ui-icon-triangle-1-w"></span></div>',
                //togglerContent_open:	'<div id="showHideRightMenu" class="shadow" ><span class="ui-state-default ui-icon ui-icon-triangle-1-e"></span></div>',
                //togglerContent_open:	'<div id="showHideRightMenua" class="ui-layout-toggler ui-layout-toggler-east ui-layout-toggler-open ui-layout-toggler-east-open"></div>',
                
                onopen_end: function(){
                    //positionAlto();
                },
                onclose_end: function(){
                    //positionAlto();
                }
            },
            center:{
                onresize_end: function(){
                    //positionAlto();
                }
            }
        });
        $('#split.viewer').bind('viewReady', function(event, viewerOptions){
            resizeSplit(viewerOptions.fullid);
        });
        
        
    });
    
    
    function resizeSplit(){
        //var h = Math.max(800, $('#centralContent').height()+80);
        //$('#split').css('height', h);
        //sp.resizeAll();
    }
</script>