<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false" %>
<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>

<div style="float:left;padding-right:20px;">
<div>
    <a title="" href="">
        <span><%=currentFacet.displayName%></span>
    </a>
</div>    
        <%
        try {
            //if (currentFacet.name.contains("abeceda")) {
            //    Collections.sort(currentFacet.infos, czechComparator);
            //}
            //Iterator stepper = currentFacet.infos.iterator();
            //while (stepper.hasNext()) {
            //    FacetInfo current = (FacetInfo) stepper.next();
            for (int j = 0; j < abecedaArray.length; j++) {
                FacetInfo current = currentFacet.getFacetInfoByName(abecedaArray[j]);
                FacetInfo current_page = facets.get("abeceda_title_page").getFacetInfoByName(abecedaArray[j]);
                if(current!=null){
        %>
        <div>
            <a title="<fmt:message >Add_navigator</fmt:message>" 
               class="sn3 "
               href="<%=current.url%>"><%=current.displayName%></a> (<%=current.count%>/<%
                    
                    if(current_page!=null){
                        out.print(current_page.count);
                    }else{
                        out.print(0);
                    }
                    %>)
        </div>
        <%

            }else{
         %>
        <div>
            <span><%=abecedaArray[j]%>(0/0)</span>
        </div>
        <%            
            }
                }
        } catch (Exception ex) {
            out.println(ex);
        }
        %>
    </div>

