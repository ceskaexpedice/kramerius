<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<form name="suggestForm" method="GET" action="./" autocomplete="Off">
    <div id="suggestDiv" class="ui-tabs ui-widget ui-widget-content ui-corner-all" >
        <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all" style="padding:0 0.1em 0 0;">
            <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active " style="width:100%;"><a class="box"><fmt:message bundle="${lctx}" key="Procházet" /></a></li>
        </ul>
        <div id="suggestBody" class="ui-tabs-panel ui-widget-content ui-corner-bottom">    
            <fmt:message bundle="${lctx}" key="Hlavní název" /><br/>
            <input name="queryT" id="queryT" type="text" size="20" class="suggest_input"
                   onkeyup="doAutocomplete(this, 'root_title', event, '#queryT');"
                   onfocus="$(autoCompleteDiv).hide();" /><br/>
            <fmt:message bundle="${lctx}" key="Autor" /><br/>
            <input name="queryA" id="queryA" size="20" type="text" class="suggest_input"
                   onkeyup="doAutocomplete(this, 'facet_autor', event, '#queryA');" />
        </div>
        <div id="autocomplete" class="autocomplete" >
        </div>
    </div>
    <%--
<td>
        <div class="suggestTitle"><div><fmt:message bundle="${lctx}" key="Datum" /></div></div>    
            <div class="suggest_box"><div style="position:relative;" class="facetItem">
            <input name="queryD" id="queryD" size="20" type="text"  class="suggest_input"
            onkeyup="doAutocomplete(this.value, 'datum', event , '#autocompleteD', '#queryD');" /><br/>
                <div id="autocompleteD" class="autocomplete" >
                </div>
            </div>
            </div>
    </td>
    --%>
</form>
<script language="javascript">
    var canHide = true;
    function hideDelayed(){
        if(canHide){
            $(autoCompleteDiv).hide();
        }
    }
    function breakHide(){
        canHide = false;
    }
    function fireHide(){
        $(autoCompleteDiv).hide();
        //canHide = true;
        //setTimeout('hideDelayed()', 1000);
    }
    $(document).ready(function(){
        $('body').click(function() {
          $(autoCompleteDiv).hide();
        });
    });
</script>