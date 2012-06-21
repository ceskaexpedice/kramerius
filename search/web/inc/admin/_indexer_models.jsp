<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ page trimDirectiveWhitespaces="true"%>

<%@page import="javax.servlet.jsp.jstl.core.Config"%>
<%@page import="cz.incad.kramerius.resourceindex.*"%>

<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>


<%@ page isELIgnored="false"%>

<scrd:securedContent action="reindex" sendForbidden="true">

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />

<%

IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
org.w3c.dom.Document doc = g.getFedoraModels();
pageContext.setAttribute("doc", doc);

%>
<c:import url="fedora_models.xsl" var="xsltPage" charEncoding="UTF-8"  />
<x:transform doc="${doc}"  xslt="${xsltPage}"  >
</x:transform>

</scrd:securedContent>
