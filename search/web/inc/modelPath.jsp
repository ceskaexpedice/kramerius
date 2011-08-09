<%@ page contentType="text/plain" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            pageContext.setAttribute("kconfig", kconfig);
            FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));   
%>
<%--
<c:url var="url" value="${kconfig.solrHost}/select/" >
    <c:param name="q" >pid_path:"${param.pid_path}"</c:param>
    <c:param name="rows" value="1" />
    <c:param name="fl" value="pid_path" />
</c:url>
<c:catch var="exceptions">
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
    <x:parse var="doc" xml="${xml}"  />
    <c:set var="pid_path"><x:out select="$doc/response/result/doc/str[@name='pid_path']" /></c:set>
    <jsp:useBean id="pid_path" type="java.lang.String" />
</c:catch>
--%>


    <%
    // Muzu to vyhodit z jspecka a dat do javy? 
        String model_path = "";   
String pid_path = request.getParameter("pid_path");
        String[] pids = pid_path.split("/");
        for(int i=0; i<pids.length; i++){
            if(i>0) model_path += "/";
            model_path += fedoraAccess.getKrameriusModelName(pids[i]);
        }
        out.clear();
        out.print(model_path);
    %>