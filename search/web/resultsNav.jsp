<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false" %>
<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>
<!-- filtry -->


<%-- Display --%>
<%
            for (int i = 0; i < facets.size(); i++) {
                Facet currentFacet = facets.get(i);
                if (!pageType.equals("welcome") || currentFacet.name.equals("abeceda")) {
                    if (currentFacet.hasUnused()) {
%>
<li class="clear">
    <a title="" href="" class="sn2 ">
        <span><%=currentFacet.displayName%></span>
    </a>
    <ul class="sNavLevel3">
        <%
        try {

            if (currentFacet.name.equals("abeceda")) {
                Collections.sort(currentFacet.infos, czechComparator);
            }
            Iterator stepper = currentFacet.infos.iterator();
            while (stepper.hasNext()) {
                FacetInfo current = (FacetInfo) stepper.next();
        %>
        <li class="clear">
            <a title="<fmt:message >Add_navigator</fmt:message>" 
               class="sn3 "
               href="<%=current.url%>">
                <span><%=current.displayName%>(<%=current.count%>)</span>
            </a>
        </li>
        <%

            }
        } catch (Exception ex) {
            out.println(ex);
        }
        %>
    </ul>
</li>
<%}
                }
            }%>
<!-- konec filtry -->
