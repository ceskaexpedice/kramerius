<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<% 

//remoteUserID = "ja";
        if(request.getRemoteUser()!=null){ 
%>
<c:set var="urlBiblioMods" >
    <c:out value="${kconfig.fedoraHost}" />/get/uuid:<c:out value="${uuid}" />/BIBLIO_MODS
</c:set>
<c:set var="urlSolr" >
    <c:out value="${kconfig.solrHost}" />?q=PID:"<c:out value="${uuid}" />"
</c:set>
<c:set var="urlReindex" >
    <c:out value="${kconfig.indexerHost}" />/rest?operation=updateIndex&action=fromKrameriusModel&value=uuid:<c:out value="${uuid}" />
</c:set>
<div style="float:right;">
<a href='<c:out value="${urlBiblioMods}" />' target="biblio_mods"><img src="img/admin/biblio_mods.png" alt="view biblio mods" title="view biblio mods" border="0" /></a> 
<a href='<c:out value="${urlSolr}" />' target="solr"><img src="img/admin/solr.png" alt="view in solr" title="view in solr" border="0" /></a>
<a href='<c:out value="${urlReindex}" />' target="_blank"><img src="img/admin/reindex.png" alt="reindex" title="reindex" border="0" /></a>
</div>
<%}%>
