<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>

<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            pageContext.setAttribute("kconfig", kconfig);
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);

%>
<%@ include file="../searchParams-html.jsp" %>
<%
    cz.incad.kramerius.service.XSLService xs = (cz.incad.kramerius.service.XSLService) ctxInj.getInstance(cz.incad.kramerius.service.XSLService.class);
    try {
        String xsl = "insearch.xsl";
        if (xs.isAvailable(xsl)) {
            String text = xs.transform(xml, xsl, lctx.getLocale());
            out.println(text);
            return;
        }
    } catch (Exception e) {
        out.println(e);
    }
%>
<c:url var="xslPage" value="xsl/insearch.xsl" />
<c:catch var="exceptions">
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <% out.clear();%>
        <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
        <c:catch var="exceptions2">
            <x:transform doc="${xml}"  xslt="${xsltPage}">
                <x:param name="bundle_url" value="${i18nServlet}"/>
            </x:transform>
        </c:catch>
        <c:if test="${exceptions2 != null}"><c:out value="${exceptions2}" />
        </c:if>
    </c:otherwise>
</c:choose>

<script type="text/javascript">
    function changeSelect(pp){
        $.get("inc/modelPath.jsp?pid_path="+pp, function(data){
            pid_path_str = pp;
            model_path_str = trim10(data);
            pid_path = pid_path_str.split('/');
            model_path = model_path_str.split('/');
            loadingInitNodes = true;
            cur = 1;
            loadInitNodes();
        });
    }
    function getCxtInfo(){
        $(".extInfo:hidden").each(function(){
            var info = $(this);
            //$(info).removeClass("extInfo");
            var pid_path = $(info).text();
            if(pid_path.indexOf("/")>0){
                var url =  "inc/results/extendedInfo.jsp?pid_path=" + pid_path;
                $.get(url, function(data) { 
                    var d = $(data);
                    d.children(':last').remove();
                    $(info).html(d.html());
                    $(info).show();
                });
            }
        });
    }
    $(document).ready(function(){
        getCxtInfo();
    });
</script>