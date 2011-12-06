<%@page import="java.util.List"%>
<%@page import="org.fedora.api.RelationshipTuple"%>
<%@page import="cz.incad.kramerius.FedoraNamespaces"%>
<%@page import="java.nio.charset.Charset"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.InputStream"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.*"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext, cz.incad.kramerius.FedoraAccess"%>
<%
    Injector inj = (Injector) application.getAttribute(Injector.class.getName());
    LocalizationContext lctx = inj.getProvider(LocalizationContext.class).get();
    FedoraAccess fa = inj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
    pageContext.setAttribute("lctx", lctx);
    String donator = fa.getDonator(request.getParameter("uuid"));
    if(donator!=null && !donator.equals("")){
        InputStream is = fa.getDataStream("donator:"+donator, "TEXT-"+lctx.getLocale().getLanguage().toUpperCase());
        String donator_text = IOUtils.readAsString(is, Charset.forName("UTF-8"), true).trim();
        
        List<RelationshipTuple> rels = fa.getAPIM().getRelationships("donator:"+donator, FedoraNamespaces.RDF_NAMESPACE_URI + "link");
        
        String link = "";
        if(!rels.isEmpty()){
            link = rels.get(0).getObject();
        }
        
        pageContext.setAttribute("donator", donator);
        pageContext.setAttribute("donator_text", donator_text);
        pageContext.setAttribute("donator_link", link);
    
%>
<a href="${donator_link}" target="_blank"><img height="50" src="proxy?pid=donator:${donator}&dsname=LOGO" alt="${donator_text}" title="${donator_text}" /></a>
<%
    }
%>