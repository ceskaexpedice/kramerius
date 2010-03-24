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
    <div id="suggestDiv" ><div class="suggestTitle"><div><fmt:message key="Procházet" /></div></div>
    <div class="facet"><div style="position:relative;" class="facetItem">    
            <fmt:message key="Hlavní název" /><br/>
            <input name="queryT" id="queryT" type="text" size="20" class="suggest_input"
                   onkeyup="doAutocomplete(this, 'root_title', event, '#queryT');"
                   onfocus="$(autoCompleteDiv).hide();" /><br/>
            <fmt:message key="Autor" /><br/>
            <input name="queryA" id="queryA" size="20" type="text" class="suggest_input"
                   onkeyup="doAutocomplete(this, 'facet_autor', event, '#queryA');" /><br/><br/>
        </div>
    </div>
    <div id="autocomplete" class="autocomplete" >
    </div>
    </div>
    <%--
<td>
        <div class="suggestTitle"><div><fmt:message key="Datum" /></div></div>    
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