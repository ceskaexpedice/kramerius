<%@ page import="java.util.*, cz.incad.Kramerius.*, cz.incad.Solr.*" %>
<%
if(session.getAttribute("configuration")==null)
{
        String configfile = getServletContext().getRealPath("WEB-INF/config/config.xml");
        KConfiguration kconfig =  new KConfiguration(configfile);
	session.setAttribute( "configuration", kconfig);
	session.setAttribute( "fedoraSolr", kconfig.getProperty("fedoraSolr") );
	session.setAttribute( "fedoraHost", kconfig.getProperty("fedoraHost"));
	session.setAttribute( "indexerHost", kconfig.getProperty("indexerHost"));
}
KConfiguration kconfig = (KConfiguration) getServletContext().getAttribute("configuration");



%> 