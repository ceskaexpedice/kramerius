<%@ page contentType="text/html;charset=utf-8" %>
<%@ page session="false"%>
<%@ page import="java.net.*,java.io.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%!
    public String removeDiacritic(String old) {
        char[] o = {'á', 'à', 'č', 'ď', 'ě', 'é', 'í', 'ľ', 'ň', 'ó', 'ř', 'r', 'š', 'ť', 'ů', 'ú', 'u', 'u', 'ý', 'ž', 'Á', 'À', 'Č', 'Ď', 'É', 'Ě', 'Í', 'Ĺ', 'Ň', 'Ó', 'Ř', 'Š', 'Ť', 'Ú', 'Ů', 'Ý', 'Ž'};
        char[] n = {'a', 'a', 'c', 'd', 'e', 'e', 'i', 'l', 'n', 'o', 'r', 'r', 's', 't', 'u', 'u', 'u', 'u', 'y', 'z', 'A', 'A', 'C', 'D', 'E', 'E', 'I', 'L', 'N', 'O', 'R', 'S', 'T', 'U', 'U', 'Y', 'Z'};

        String newStr = old;
        for (int i = 0; i < o.length; i++) {
            newStr = newStr.replace(o[i], n[i]);
        }
        newStr = newStr.replace(" ", "");
        return newStr;
    }
%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            try {
                String term = removeDiacritic(request.getParameter("t").toUpperCase()) +
                        "##" + request.getParameter("t");
                String including = request.getParameter("i");
                if (including==null){
                    including = "true";
                }
                //if (term.length() > 0) {

                    term = java.net.URLEncoder.encode(term, "UTF-8");
                    String reqUrl = kconfig.getSolrHost() + "/terms?terms.fl="
                            + request.getParameter("field")
                            + "&terms.lower.incl=" + including
                            + "&terms.sort=index&terms.limit=50&terms.lower="
                            + term;
                    URL url = new URL(reqUrl);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoOutput(true);
                    con.setRequestMethod(request.getMethod());
                    int clength = request.getContentLength();
                    if (clength > 0) {
                        con.setDoInput(true);
                        byte[] idata = new byte[clength];
                        request.getInputStream().read(idata, 0, clength);
                        con.getOutputStream().write(idata, 0, clength);
                    }

                    //response.setContentType(con.getContentType()); 
                    BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), java.nio.charset.Charset.forName("UTF-8")));
                    String line;
                    String res = "";
                    out.clear();
                    while ((line = rd.readLine()) != null) {
                        res += line;
                    }
                    rd.close();
                    pageContext.setAttribute("res", res);
 %>
<c:url var="xslPage" value="inc/home/xsl/autocomplete.xsl" />
<c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
<x:transform doc="${res}"  xslt="${xsltPage}"  />
<%

            } catch (Exception e) {
                response.setStatus(500);
            }
%>