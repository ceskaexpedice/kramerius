<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="cz.incad.Kramerius.views.item.ItemViewObject"%>
<%
	if(pageContext.getAttribute("lctx")==null){
		pageContext.setAttribute("lctx", ((Injector)application.getAttribute(Injector.class.getName())).getProvider(LocalizationContext.class).get());
	}
	if(pageContext.getAttribute("kconfig")==null){
		pageContext.setAttribute("kconfig", ((Injector)application.getAttribute(Injector.class.getName())).getProvider(KConfiguration.class).get());
	}

	Injector ctxInj = (Injector)application.getAttribute(Injector.class.getName());
        //KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
        //pageContext.setAttribute("kconfig", kconfig);
        LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
	// view objekt pro stranku = veskera logika 
	ItemViewObject itemViewObject = new ItemViewObject();
	((Injector)application.getAttribute(Injector.class.getName())).injectMembers(itemViewObject);
        itemViewObject.init();
	pageContext.setAttribute("itemViewObject", itemViewObject);
%>
<%@ include file="inc/initVars.jsp" %>
<%@ include file="inc/details/itemMenu.jsp" %>