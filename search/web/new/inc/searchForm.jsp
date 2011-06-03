<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${empty param.q}" >
        <c:set var="qtext" ><fmt:message bundle="${lctx}">form.search</fmt:message></c:set>
        <c:set var="qclass" >searchQuery ui-corner-all</c:set>
    </c:when>
    <c:otherwise>
        <c:set var="qtext" ><c:out value="${param.q}" /></c:set>
        <c:set var="qclass" >searchQuery ui-corner-all searching</c:set>
    </c:otherwise>
</c:choose>
<form name="searchForm" id="searchForm" method="GET" action="r.jsp" onsubmit="return checkQuery()">
    <input id="debug" name="debug" type="hidden"
           value="${param.debug}" /> <input type="text"
           alt="" name="q" id="q"
           value="${qtext}" size="50"
           class="${qclass}" type="text" onclick="checkSearchInput();"> &nbsp;
    <input class="submit" title="Vyhledat" type="submit" value="" />
    <span><a href="javascript:toggleAdv();"
       title="<fmt:message bundle="${lctx}">Pokročilé vyhledávání</fmt:message>"><fmt:message bundle="${lctx}">Pokročilé vyhledávání</fmt:message></a>
    </span><%@ include file="advancedSearch.jsp"%>
</form>
<script type="text/javascript">

    var inputSearchInitialized = false;
    function checkSearchInput(){
        var iniVal = '<fmt:message bundle="${lctx}">form.search</fmt:message>';
        var q = $('#q').val();
        if(!inputSearchInitialized && iniVal == q){
            inputSearchInitialized = true;
            $('#q').val('');
            $('#q').addClass('searching');
        }
    }
    function checkQuery(){
        if ($('#q.searching').length==0) {
            //$('#q').val('');
            return false;
        }
        return true;
    }
</script>