<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="com.google.inject.Injector"%>
<%@page
    import="javax.servlet.jsp.jstl.fmt.LocalizationContext, cz.incad.kramerius.FedoraAccess"%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>

<%@ page isELIgnored="false"%>

<%
Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
pageContext.setAttribute("kconfig", kconfig);
LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
pageContext.setAttribute("lctx", lctx);

String redirectURL =  URLDecoder.decode(request.getParameter("redirectURL")!=null ? request.getParameter("redirectURL") : "search.jsp"  ,"UTF-8");   
pageContext.setAttribute("redirectURL", redirectURL);

%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
<%@ include file="inc/html_header.jsp"%>
<body>
<script type="text/javascript">
    var errorDialog = null
    function showErrorDialog() {
        if (!errorDialog) {                
            errorDialog = $("#error_dialog").dialog({
                bgiframe: true,
                width: 400,
                height: 150,
                modal: true,
                title: '<fmt:message bundle="${lctx}">pdf.pdfError.label</fmt:message>',
                buttons: {
                     "Ok": function() {
                         window.location.href = '${redirectURL}';
                      }
                }
            });
        } else {
            errorDialog("open");
        }
    }
    
                    
    $(document).ready(function(){
        showErrorDialog();
     });
    </script>
    <div id="error_dialog" style="display: none;">
        <div style="text-align:center">
        <h3></h3>    
            <span><strong><fmt:message bundle="${lctx}">pdf.pdfError.text</fmt:message></strong></span>
        </div>
    </div>
</body>
</html>