<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
<c:if test="${!empty param.q || param.da_od != null && param.da_od != '' || !empty paramValues.fq ||
      !empty param.issn || !empty param.title || !empty param.author || !empty param.rok || !empty param.keywords ||
      !empty param.udc ||!empty param.ddc || !param.shelfLocator || !param.physicalLocation || !empty param.onlyPublic || param.suggest=='true' ||
      cols.current != null}" >
<ul>
    <li style="border-color: rgba(0, 30, 60, 0.9);border-right: 1px solid rgba(0, 30, 60, 0.9);">
        <span class="ui-icon ui-icon-triangle-1-e folder"></span>
        <span style="text-indent:0px;width:auto;font-weight: bold;"><fmt:message bundle="${lctx}" key="filter.used" /></span><div class="clear"> </div>
        <ul id="usedFilters">
            <c:if test="${cols.current != null}">
            <li>
                 <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeVirtualCollection();">
                     <fmt:message bundle="${lctx}" key="filter.collection" />: ${cols.current.label}
                 </a>
            </li>
            </c:if>
            <c:if test="${!empty param.q}" >
            <li>
                 <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeQuery();"><c:out value="${param.q}" /></a>
                 </li>
            </c:if>
            <%-- datum --%>
            <c:if test="${param.da_od != null && param.da_do != ''}">
                <li>
		  <c:choose>
		      <c:when test="${param.exactDay == 'true'}" >
			<a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeDateAxisFilter();">
			<fmt:message bundle="${lctx}" key="common.date" />: <c:out value="${param.da_od}" /></a>
		      </c:when>
		      <c:otherwise>
			<a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeDateAxisFilter();">
			<fmt:message bundle="${lctx}" key="common.date" />: <c:out value="${param.da_od}" /> - <c:out value="${param.da_do}" /></a>
		      </c:otherwise>
		  </c:choose>
                </li>
            </c:if>

            <%-- filter queries --%>
            <c:forEach varStatus="status" var="fqs" items="${paramValues.fq}">
                <c:set var="js">${fn:replace(fqs, "\"", "")}</c:set>
                <c:set var="facetName">${fn:substringBefore(fqs,':')}</c:set>
                <c:set var="facetName">${fn:replace(facetName, "\"", "")}</c:set>
                <c:set var="facetValue"><c:out value="${fn:substringAfter(fqs,':')}" escapeXml="false" /></c:set>
                <c:set var="facetValue">${fn:replace(facetValue, "\"", "")}</c:set>
                <c:set var="facetValueDisp"><c:out value="${facetValue}" /></c:set>
                <c:if test="${facetName == 'fedora.model' || facetName == 'document_type'}">
                    <c:set var="facetValueDisp"><fmt:message bundle="${lctx}" >fedora.model.${facetValueDisp}</fmt:message></c:set>
                    <c:if test="${fn:startsWith(facetValueDisp, '???')}"><c:set var="facetValueDisp">${facetValue}</c:set></c:if>
                </c:if>
                <c:if test="${facetName == 'model_path'}">
                    <c:set var="facetValueDisp"><fmt:message bundle="${lctx}" >fedora.model.${fn:substringBefore(facetValueDisp, '*')}</fmt:message></c:set>
                    <c:if test="${fn:startsWith(facetValueDisp, '???')}"><c:set var="facetValueDisp">${facetValue}</c:set></c:if>
                </c:if>
                <c:if test="${facetName == 'dostupnost'}">
                    <c:set var="facetValueDisp"><fmt:message bundle="${lctx}" >dostupnost.${facetValueDisp}</fmt:message></c:set>
                    <c:if test="${fn:startsWith(facetValueDisp, '???')}"><c:set var="facetValueDisp">${facetValue}</c:set></c:if>
                </c:if>
                    
                <li>
                <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeFacet(${status.count});">
                <fmt:message bundle="${lctx}" >facet.<c:out value="${facetName}" /></fmt:message>: <c:out value="${facetValueDisp}"/></a>
                <input type="hidden" name="fq" id="fq<c:out value="${status.count}" />" value="<c:out value="${facetName}" />:<c:out value="${facetValue}" />" />
                </li>

            </c:forEach>

            <%-- suggest params --%>
            <c:if test="${param.suggest=='true'}">
                    <li>
                    <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeSuggest();">
                    <fmt:message bundle="${lctx}">filter.maintitle</fmt:message>: <c:out value="${param.browse_title}"/></a>
                    </li>
            </c:if>

            <%-- advanced params --%>
            <c:if test="${!empty param.issn}">
                <li> <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeAdvFilter('issn', '<c:out value="${param.issn}" />');">
                <fmt:message bundle="${lctx}" key="filter.query.isbnissn" />: <c:out value="${param.issn}"/></a>
                </li>
            </c:if>
            <c:if test="${!empty param.title}">
                <li>
                <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeAdvFilter('title', '<c:out value="${param.title}" />');">
                <fmt:message bundle="${lctx}">filter.maintitle</fmt:message>: <c:out value="${param.title}"/></a>
                </li>
            </c:if>
            <c:if test="${!empty param.author}">
                <li>
                <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeAdvFilter('author', '<c:out value="${param.author}" />');">
                <fmt:message bundle="${lctx}" key="author" /> &#160;<c:out value="${param.author}"/></a>
                </li>
            </c:if>
            <c:if test="${!empty param.rok}">
                <li>
                <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeAdvFilter('rok', '<c:out value="${param.rok}" />');">
                <fmt:message bundle="${lctx}" key="rok" />: &#160;<c:out value="${param.rok}"/></a>
                </li>
            </c:if>
            <c:if test="${!empty param.keywords}">
                <li>
                <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeAdvFilter('keywords', '<c:out value="${param.keywords}" />');">
                <fmt:message bundle="${lctx}" key="filter.keywords" />: &#160;<c:out value="${param.keywords}"/></a>
                </li>
            </c:if>
            <c:if test="${!empty param.udc}">
                <li>
                <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeAdvFilter('udc', '<c:out value="${param.udc}" />');">
                MDT: &#160;<c:out value="${param.udc}"/></a>
                </li>
            </c:if>
            <c:if test="${!empty param.ddc}">
                <li>
                <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeAdvFilter('ddc', '<c:out value="${param.ddc}" />');">
                DDT: &#160;<c:out value="${param.ddc}"/></a>
                </li>
            </c:if>
             

            <c:if test="${!empty param.shelfLocator}">
                <li>
                <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeAdvFilter('shelfLocator', '<c:out value="${param.shelfLocator}" />');">
               <fmt:message bundle="${lctx}" key="filter.shelfLocator" />: &#160;<c:out value="${param.shelfLocator}"/></a>
                </li>
            </c:if>

            <c:if test="${!empty param.physicalLocation}">
                <li>
                <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeAdvFilter('physicalLocation', '<c:out value="${param.physicalLocation}" />');">
               <fmt:message bundle="${lctx}" key="filter.physicalLocation" />: &#160;<c:out value="${param.physicalLocation}"/></a>
                </li>
            </c:if>
                
            <c:if test="${!empty param.onlyPublic}">
                <li>
                <a title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />" class="mainNav" href="javascript:removeAdvFilter('onlyPublic', '<c:out value="${param.onlyPublic}" />');">
                <fmt:message bundle="${lctx}" key="Pouze veřejné dokumenty" />:&#160; <c:out value="${param.onlyPublic}"/></a>
                </li>
            </c:if>
        </ul>
    </li>
</ul>
<script type="text/javascript">
    $(document).ready(function(){
        $("#usedFilters>li>a").prepend('<span class="ui-icon ui-icon-close" title="<fmt:message bundle="${lctx}" key="filter.remove_criteria" />">remove</span>');
    });
    
    function removeFacet(index){
        $('#fq'+index).remove();
        //checkQuery();
        $('#searchForm').submit();
    }
    
    function removeSuggest(){
        $('#suggest').remove();
        $('#suggest_q').remove();
        //checkQuery();
        $('#searchForm').submit();
    }
    
    function removeDateAxisFilter(){
        $('#da_od').remove();
        $('#da_do').remove();
        //checkQuery();
        $('#searchForm').submit();
        
        //var page = new PageQuery(window.location.search);
        //page.removeParam(fromField);
        //page.removeParam(toField);
        //var newurl = "?" + page.toString();
        //document.location.href = newurl;
    }
    function removeQuery(){
    
        var page = new PageQuery(window.location.search);

        page.setValue("offset", "0");
        page.setValue("q", "");
        var url = "r.jsp?" + page.toString();
        window.location = url;
    }
    function removeAdvFilter(field, value){
        $('#'+field).val('');
        //checkQuery();
        $('#searchForm').submit();
    }
    
    
    
</script>
</c:if>

