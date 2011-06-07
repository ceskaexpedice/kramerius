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
<%
            try {
%>
<style type="text/css">
    #split {
        height: 700px;
        width: 1000px;
    }
    #preview {
        overflow: auto;
    }
    #rightPanel {
        overflow: hidden;
        width:330px;
    }

    #rightPanel ul{
        margin: 2px;
        padding-left: 12px;
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
    .vsplitbar {
        width: 5px;
        background: silver;
    }
</style>
<div id="thumbs">
    <%@ include file="thumbs.jsp" %>
</div>
<%--
<div id="split" style="position:relative;">
    <div id="rightPanel" class="box" style="float:right;right:0;z-index:3335;position:absolute;">
        <%@ include file="tree.jsp" %>
    </div>
    <div id="preview" style="">
        <%@include  file="tabs/container.jsp" %>
    </div>
    <div style="clear:both;display:block;"></div>
</div>
--%>

<div id="split" class="viewer" style="position:relative;">
    <div id="preview" class="ui-layout-center" >
        <%@include  file="tabs/container.jsp" %>
    </div>
    <div id="rightPanel"  class="ui-layout-east" >
        <%@ include file="tree.jsp" %>
    </div>
    <div style="clear:both;display:block;"></div>
</div>
<script type="text/javascript">
    var sp;
    $(document).ready(function(){
       sp = $("#split").layout({
            east:{
                size:340,
                spacing_closed:	20,
                spacing_open:	20,
                togglerLength_closed:	'100%',
                togglerLength_open:	'100%',
                togglerAlign_open:	"top",
                togglerAlign_closed:	"top",
                togglerTip_closed: '<fmt:message bundle="${lctx}">item.showhide</fmt:message>',
                togglerTip_open: '<fmt:message bundle="${lctx}">item.showhide</fmt:message>',
                togglerContent_closed:	'<div id="showHideRightMenu" class="shadow" ><span class="ui-state-default ui-icon ui-icon-triangle-1-w"></span></div>',
                togglerContent_open:	'<div id="showHideRightMenu" class="shadow" ><span class="ui-state-default ui-icon ui-icon-triangle-1-e"></span></div>',
                
                onopen_end: function(){
                    positionAlto();
                },
                onclose_end: function(){
                    positionAlto();
                }
            },
            center:{
                onresize_end: function(){
                    positionAlto();
                }
            }
        });
        $('#split.viewer').bind('viewReady', function(event, viewerOptions){
            resizeSplit(viewerOptions.fullid);
        });
    });
    function resizeSplit(){
        var h = Math.max(800, $('#centralContent').height()+80);
        //alert(h);
        $('#split').css('height', h);
        sp.resizeAll();
    }
</script>
<%            } catch (Exception exc) {
                response.sendRedirect(kconfig.getApplicationURL() + "?error=item_error");
                return;
            }
%>