
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@ page import="java.util.*, cz.incad.Kramerius.*, cz.incad.Solr.*, cz.incad.utils.*" %>
<%

KConfiguration kconfig = (KConfiguration) getServletContext().getAttribute(IKeys.CONFIGURATION);

Map<String, Facet> facets = new HashMap<String, Facet>();
int facetsCollapsed = 5;
String imagePid = "";
%> 