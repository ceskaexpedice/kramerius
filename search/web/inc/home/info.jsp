<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.Kramerius.views.WellcomeViewObject"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<style type="text/css">
    #intro_text{
        font-size:1.1em;
        overflow: auto;
        height: 100%;
    }
    
    #intro_text a{
        color:rgba(0, 30, 60, 0.9);
        font-size:1.1em;
        
    }
     
         
</style>
<div id="intro_text">
<%
    Injector wellcomeInjector = (Injector)application.getAttribute(Injector.class.getName());
    WellcomeViewObject wellcomeViewObject = new WellcomeViewObject();
    wellcomeInjector.injectMembers(wellcomeViewObject);
    out.print(wellcomeViewObject.getIntro());
    //pageContext.setAttribute("wellcomeViewObject", wellcomeViewObject);
%>
</div>