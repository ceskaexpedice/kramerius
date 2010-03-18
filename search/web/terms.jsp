<%@ page contentType="text/html;charset=utf-8" %>
<%@ page session="false"%>
<%@ page import="java.net.*,java.io.*" %>
<%@ include file="inc/initVars.jsp" %>
<%
            try {
                String term = request.getParameter("t");
                if (term.length() > 0) {
                    if (term.length() > 1) {
                        term = term.substring(0,1).toLowerCase() + term.substring(1) + "|" +
                            term.substring(0,1).toUpperCase() + term.substring(1);
                    }else{
                        term = term.toLowerCase() + "|" + term.toUpperCase();
                    }
                    term = java.net.URLEncoder.encode(term, "UTF-8");
                    String reqUrl = kconfig.getSolrHost() + "/terms?terms.fl=" +
                            request.getParameter("field") +
                            "&wt=json&omitHeader=true&terms.regex.flag=case_insensitive&terms.limit=40&terms.regex=" +
                            term + ".*";
                    //String reqUrl = "http://194.108.215.227:8080/solr/terms?terms.fl="+request.getParameter("field")+
                    //        "&wt=json&omitHeader=true&terms.regex.flag=case_insensitive&terms.prefix=" + 
                    //        java.net.URLEncoder.encode(request.getParameter("t"), "UTF-8") ;
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

                    response.setContentType(con.getContentType());
                    BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), java.nio.charset.Charset.forName("UTF-8")));
                    String line;
                    out.clear();
                    while ((line = rd.readLine()) != null) {
                        out.print(line);
                    }
                    rd.close();
                } else {
                    out.clear();
                    out.print("{\"terms\":[\"empty\",[]]}");
                }
            } catch (Exception e) {
                response.setStatus(500);
            }
%>