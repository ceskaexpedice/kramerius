<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.service.*"%>

<c:set var="wt" scope="request">xml</c:set>
    <% pageContext.setAttribute("carriageReturn", "\r"); %> 
<% pageContext.setAttribute("newLine", "\n"); %> 
<c:set var="singleQuotes">'</c:set>
<c:set var="singleQuotesReplace">\'</c:set>
<c:set var="doubleQuotes">"</c:set>
<c:set var="doubleQuotesReplace">\"</c:set>
<c:set var="exactDay">false</c:set>
<c:if test="${param.exactDay  == 'true'}"><c:set var="exactDay">true</c:set></c:if> 
<script src="js/underscore-min.js" type="text/javascript" ></script>
<script src="js/utils.js" type="text/javascript" ></script>
<script src="js/jcanvas.js" type="text/javascript" ></script>
<script src="js/canvasDa.js" type="text/javascript" ></script>
<script type="text/javascript">
    var data = '<c:out escapeXml="false" value="${fn:replace(fn:replace(fn:replace(fn:replace(xml,carriageReturn,' '),newLine,' '), singleQuotes, singleQuotesReplace),doubleQuotes,doubleQuotesReplace)}" />';
    var da;
    $(document).ready(function(){ 
        da = new Da('#canvasda', data, {isXml: true});
        da.render();
        da.setDatePicker();
        da.scrollToMax();
        $('#exactDay').attr('checked', ${exactDay});
    });
    
    function toggleExact(){
      if($('#exactDay').attr('checked')){
	$('.exactbox').hide();
      } else {
	$('.exactbox').show();
      }
    }
    
    function checkValid(event, obj){
        var minDate = $("#f1").datepicker('option', 'getDate');
        var maxDate = $("#f2").datepicker('option', 'getDate');
        var value = $(obj).val().replace(/\./g, "-");
        var dateValue = Date.parse(value);
        if(isNaN(dateValue) || dateValue > maxDate || dateValue < minDate){
            //return false;
        }
        $(obj).datepicker('option', 'setDate', value);
        if( event.keyCode !== 13){
            //return false;
        }
        return true;
    }
</script>
<div id="da-inputs" >
    <span class="exactbox" style="float:left;"><fmt:message bundle="${lctx}">Od</fmt:message>:&nbsp;</span>
    <input style="float:left;" class="da_input" id="f1" size="10" type="text" value="" onchange="checkValid(event, this)"  />
    <div class="exactbox" style="float:left; <c:if test="${param.exactDay  == 'true'}">display:none;</c:if> /> ">
    <span style="float:left;">&nbsp;<fmt:message bundle="${lctx}">Do</fmt:message>:&nbsp;</span>
    <input style="float:left;" class="da_input" id="f2" size="10" type="text" value="" onchange="checkValid(event, this)"  /> 
    </div>
    <a href="javascript:doFilter();" style="float:right; width:16px;overflow:hidden;" ><span class="ui-icon ui-icon-search" title="<fmt:message bundle="${lctx}">dateaxis.use</fmt:message>" >a</span></a>

</div>
    <div>
    <label for="exactDay">konkrétní den</label><input type="checkbox" id="exactDay" name="exactDay" onchange="toggleExact()"
	<c:if test="${param.exactDay  == 'true'}">checked="checked"</c:if> />
    </div>
<div style="overflow:hidden; width:100%; height:100%;position: relative;left:0px;top:0px;padding:0px;">
<div id="canvasda" class="years" style="overflow:auto; position: relative;">
<div class="info"></div><div class="yearLabel"></div><div class="bar"><div class="sel"></div></div>
<canvas width="100" height="130" style="position: relative;"></canvas>
</div>
</div>