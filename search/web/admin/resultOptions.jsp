<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<c:set var="urlBiblioMods" >
    <c:out value="${fedoraHost}" />/get/<c:out value="${uuid}" />/BIBLIO_MODS
</c:set>
<c:set var="urlSolr" >
    <c:out value="${fedoraSolr}" />?q=PID:"<c:out value="${uuid}" />"
</c:set>
<c:set var="urlReindex" >
    <c:out value="${indexerHost}" />/rest?operation=updateIndex&action=fromKrameriusModel&value=<c:out value="${uuid}" />
</c:set>

<a href='<c:out value="${urlBiblioMods}" />' target="biblio_mods"><img src="img/admin/biblio_mods.png" alt="view biblio mods" title="view biblio mods" border="0" /></a> 
<a href='<c:out value="${urlSolr}" />' target="solr"><img src="img/admin/solr.png" alt="view in solr" title="view in solr" border="0" /></a>
<a href='<c:out value="${urlReindex}" />' target="reindex"><img src="img/admin/reindex.png" alt="reindex" title="reindex" border="0" /></a>
