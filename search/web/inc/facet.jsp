<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false" %>
<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>

<%
            currentFacet = facets.get(currentFacetName);
            if (currentFacet != null && currentFacet.getFacetsNumber() > 0) {

%>

<div id="facet_<%=currentFacetName%>" class="ui-tabs ui-widget ui-corner-all facet shadow10" >
    <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all" style="padding:0 0.1em 0 0;">
        <li class=" facetTitle ui-state-default ui-corner-top  ui-state-active" style="width:100%;">
        <a class="box" href="javascript:toggleFacet('<%=currentFacetName%>')"><fmt:message bundle="${lctx}"><%=currentFacet.displayName%></fmt:message></a></li>
    </ul>
    <div class="ui-tabs-panel ui-corner-bottom facetBody ">
        <%
    try {
        Iterator stepper = currentFacet.infos.iterator();
        int i = 0;
        String more = "";
        while (stepper.hasNext()) {
            FacetInfo current = (FacetInfo) stepper.next();
            if (current != null) {
                i++;
                more = (i > facetsCollapsed && currentFacet.getFacetsNumber() > facetsCollapsed) ? "moreFacets" : "";
        %>
        <div class="facetItem <%=more%> ">
            
            <a title="<fmt:message bundle="${lctx}" >facets.add_navigator</fmt:message>" 
               href="<%=current.url%>"><%
               
               if (currentFacet.name.equals("document_type")){
                   %><fmt:message bundle="${lctx}" >document.type.<%=current.displayName%></fmt:message><%
               }else if (currentFacet.name.equals("dostupnost")){
                   %><fmt:message bundle="${lctx}" >dostupnost.<%=current.displayName%></fmt:message><%
               }else if(current.displayName.equals("")) {
               %><fmt:message bundle="${lctx}" >facets.uknown</fmt:message><%
               }else{%><%=current.displayName%><%}%></a> <span class="count">(<%=current.count%>)</span>
        </div>
        <%

                } else {
        %>
        <%                    }
        }
    } catch (Exception ex) {
        out.println(ex);
    }
        %>
    </div>
</div>
<%}%>
