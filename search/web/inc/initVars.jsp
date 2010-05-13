
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@ page import="java.util.*, cz.incad.Kramerius.*, cz.incad.Solr.*, cz.incad.utils.*" %>
<%

            KConfiguration kconfig = (KConfiguration) getServletContext().getAttribute(IKeys.CONFIGURATION);

            String remoteUserID = request.getRemoteUser();
            Map<String, Facet> facets = new HashMap<String, Facet>();
            int facetsCollapsed = 5;
            String imagePid = "";
%>
<script>
    
</script>