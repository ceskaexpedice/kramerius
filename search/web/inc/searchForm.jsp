<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<form name="searchForm" id="searchForm" method="GET" action="r.jsp" onsubmit="return checkQuery()">
<%@ page isELIgnored="false"%>

    <input type="hidden" name="forProfile" id="forProfile" value="search" />
    <input type="hidden" name="forProfile_sorting_dir" id="forProfile_sorting_dir" value="" />


    <c:forEach varStatus="status2" var="fqs2" items="${paramValues.fq}">
        <c:set var="searching" value="true" />
        <c:set var="js">${fn:replace(fqs, "\"", "")}</c:set>
        <c:set var="facetName">${fn:substringBefore(fqs2,':')}</c:set>
        <c:set var="facetName">${fn:replace(facetName, "\"", "")}</c:set>
        <c:set var="facetValue"><c:out value="${fn:substringAfter(fqs2,':')}" escapeXml="false" /></c:set>
        <c:set var="facetValue">${fn:replace(facetValue, "", "")}</c:set>
        <input type="hidden" name="fq" id="fq${status2.count}" value="<c:out value="${facetName}" />:<c:out value="${facetValue}" />" />
    </c:forEach>
    <c:if test="${!empty param.da_od}" >
        <input type="hidden" id="da_od" name="da_od" value="${param.da_od}" />
        <input type="hidden" id="da_do" name="da_do" value="${param.da_do}" />
    </c:if>
<c:choose>
    <c:when test="${empty param.q && !searching && !fieldedSearch}" >
        <c:set var="qclass" >searchQuery ui-corner-all</c:set>
    </c:when>
    <c:otherwise>
        <c:set var="qclass" >searchQuery ui-corner-all searching</c:set>
    </c:otherwise>
</c:choose>
    <input id="debug" name="debug" type="hidden" value="${param.debug}" /> 
    <input id="sort" name="sort" type="hidden" value="${param.sort}" /> 
    <input type="text"
           alt="" name="q" id="q"
           value="${param.q}" size="50"
           class="${qclass}" type="text" onclick="checkSearchInput();"> &nbsp;
    <input class="submit" title="Vyhledat" type="submit" value="" />
    <span><a href="javascript:toggleAdv();"
       title="<fmt:message bundle="${lctx}">Pokročilé vyhledávání</fmt:message>"><fmt:message bundle="${lctx}">Pokročilé vyhledávání</fmt:message></a>
    </span><%@ include file="advancedSearch.jsp"%>
</form>
<script type="text/javascript">

    var inputSearchInitialized = false;
    function checkSearchInput(){
        //var iniVal = '<fmt:message bundle="${lctx}">form.search</fmt:message>';
        var iniVal = '';
        var q = $('#q').val();
        //alert(inputSearchInitialized);
        //alert(iniVal);
        //alert(q);
        if(!inputSearchInitialized && iniVal == q){
            inputSearchInitialized = true;
            $('#q').val('');
            $('#q').addClass('searching');
        }
        //alert($('#q').hasClass('searching'));
    }
    function checkQuery(){
        //check year
        var rok = $("#rok").val();
        if (rok !== "") {
            if (isNaN(parseInt(rok, 10))) {
                alert("Rok nevalidni");
                return false;
            } else {
                $("#rok").val(parseInt(rok, 10));
            }
        }  
        return true;
    }
</script>