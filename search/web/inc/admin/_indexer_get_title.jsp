<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@page import="javax.servlet.jsp.jstl.core.Config"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="cz.incad.kramerius.utils.DCUtils"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<scrd:securedContent action="reindex" sendForbidden="true">
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
            out.print(DCUtils.titleFromDC(fedoraAccess.getDC(request.getParameter("pid"))));
%>
</scrd:securedContent>
