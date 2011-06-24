<%@page import="java.io.Writer"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.OutputStreamWriter"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@page import="java.io.BufferedWriter"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="cz.incad.kramerius.utils.UnicodeUtil.UnicodeInputStream"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            pageContext.setAttribute("kconfig", kconfig);
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
            pageContext.setAttribute("lctx", lctx);
            java.io.InputStream is = fedoraAccess.getDataStream("uuid:" + request.getParameter("uuid"), "ALTO");
            //UnicodeInputStream uis = new UnicodeInputStream(is, "UTF-32BE");
            //String enc =  uis.getEncoding();




            //System.out.println("encoding: " + enc);
            //String alto = cz.incad.kramerius.utils.IOUtils.readAsString(is, java.nio.charset.Charset.forName("UTF-8"), true);



            int len;
    int size = 1024;
    byte[] buf;

    if (is instanceof ByteArrayInputStream) {
      size = is.available();
      buf = new byte[size];
      len = is.read(buf, 0, size);
    } else {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      buf = new byte[size];
      while ((len = is.read(buf, 0, size)) != -1)
        bos.write(buf, 0, len);
      buf = bos.toByteArray();
    }

            for (int i = 0; i < buf.length; i++) {
                byte ch = buf[i];
// remove any characters outside the valid UTF-8 range as well as all control characters
// except tabs and new lines
                if (!((ch > 31 && ch < 253) || ch == '\t' || ch == '\n' || ch == '\r')) {
                    buf[i] = ' ';
                }
                
            }
            String alto2 = (new String(buf, java.nio.charset.Charset.forName("UTF-8"))).trim();

            pageContext.setAttribute("xml", alto2);
            //System.out.println(alto2);
%>
<c:catch var="exceptions">
    <c:url var="xslPage" value="xsl/alto.xsl" />
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
        <c:catch var="exceptions2">
            <% out.clear();%>
            <x:transform doc="${xml}"  xslt="${xsltPage}"  >
                <x:param name="q" value="${param.q}"/>
                <x:param name="w" value="${param.w}"/>
                <x:param name="h" value="${param.h}"/>
            </x:transform>
        </c:catch>
        <c:if test="${exceptions2 != null}"><c:out value="${exceptions2}" />
        </c:if>
    </c:otherwise>
</c:choose>