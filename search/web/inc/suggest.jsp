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
<table width="100%">
<tr>
    <td><div style="float:left; margin:5px;"><div class="suggestTitle"><div><fmt:message key="Hlavní název" /></div></div>    
            <div class="suggest_box"><div style="position:relative;" class="facetItem">
            <input name="queryT" id="queryT" type="text" size="20" class="suggest_input"
            onkeyup="doAutocomplete(this.value, 'root_title', event , '#autocompleteT', '#queryT');" /><br/>
            </div>
            </div>
                <div id="autocompleteT" class="autocomplete" >
                </div>
        </div> <div style="float:left; margin:5px;">
            <div class="suggestTitle"><div><fmt:message key="Autor" /></div></div>    
            <div class="suggest_box"><div style="position:relative;" class="facetItem">
            <input name="queryA" id="queryA" size="20" type="text" class="suggest_input"
            onkeyup="doAutocomplete(this.value, 'facet_autor', event , '#autocompleteA', '#queryA');" /><br/>
            </div>
            </div></div>
                <div id="autocompleteA" class="autocomplete" >
                </div>
</td>
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
</tr></table>
</form>