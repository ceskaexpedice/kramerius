<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false" %>
<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>
<%
            
            //String abeceda = "A,Á,B,C,Č,D,Ď,E,É,Ě,F,G,H,CH,I,Í,J,K,L,M,N,Ň,O,Ó,P,Q,R,Ř,S,Š,T,Ť,U,Ú,Ů,V,W,X,Y,Ý,Z,Ž,0,1,2,3,4,5,6,7,8,9";
            //String[] abecedaArray = abeceda.split(",");
            Map<String, Facet> facets = new HashMap<String, Facet>();
            String currentFacetName = "";
            Facet currentFacet = null;
%>
<x:forEach var="nav" select="$doc/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst">
    <c:set var="facetName"><x:out select="@name"/></c:set><jsp:useBean id="facetName" type="java.lang.String" /><%
            Facet facet = new Facet(facetName);
    %><x:if select="count(./int) > 1">
        <c:choose>
            <c:when test="${facetName == 'fedora.model'}">
                <c:set var="displayName">
                    <fmt:message bundle="${lctx}" ><x:out select="@name"/></fmt:message>
                </c:set><jsp:useBean id="displayName" type="java.lang.String" /><%
            facet.displayName = displayName;
                %></c:when>
        </c:choose>
        <x:forEach select="./int" var="navValue" >
            <c:set var="facetLabel" >
                <x:out select="@name"/>
            </c:set><jsp:useBean id="facetLabel" type="java.lang.String" /><c:set var="facetCount" >
                <x:out select="."/>
            </c:set><jsp:useBean id="facetCount" type="java.lang.String" /><c:set var="facetUrl" >
                javascript:addNavigation('<x:out select="$nav/@name"/>', '<x:out select="@name"/>');
            </c:set><jsp:useBean id="facetUrl" type="java.lang.String" /><%
            FacetInfo facetInfo = new FacetInfo(facetLabel, Integer.parseInt(facetCount), facetUrl);
            facet.addFacetInfo(facetInfo);
            %><c:choose>
                <c:when test="${facetName == 'fedora.model'}">
                    <c:set var="infoDisplayName">
                        <fmt:message bundle="${lctx}" ><x:out select="@name"/></fmt:message>
                    </c:set>
                    <jsp:useBean id="infoDisplayName" type="java.lang.String" /><%
            facetInfo.displayName = infoDisplayName;
                    %>
                </c:when>
            </c:choose>
            <c:set var="used" value="0" />
            <c:forEach var="fqs" items="${paramValues.fq}">
                <c:if test="${fqs == navName}">
                    <c:set var="used" value="true"  /><jsp:useBean id="used" type="java.lang.String" /><%
            facetInfo.used = Boolean.parseBoolean(used);
                    %>
                </c:if>
            </c:forEach>       
        </x:forEach>
    </x:if><%
            facets.put(facetName, facet);
    %>
</x:forEach>