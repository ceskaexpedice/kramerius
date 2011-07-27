<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>
<%
// zaklad od kamila 
            String expandedPaginationStr = "";
            //int numHits = Integer.parseInt((String) request.getAttribute("rows"));
            String rows = (String) request.getAttribute("rows");
            int numHits = 0;
            if (rows != null) {
                numHits = Integer.parseInt(rows);
            }
            int numDocs = Integer.parseInt((String) request.getAttribute("numDocs"));
            String div = (String) request.getParameter("d");
            String filters = (String) request.getAttribute("filters");
            String type = (String) request.getParameter("type");
            String offsetUrl = "";
            boolean includeAbeceda = false;
            Facet abecedaFacet = null;

            if (type == null) {
                offsetUrl = "javascript:gotoPageOffset(%s);";
            } else if (type.equals("uncollapse")) {
                offsetUrl = "javascript:uncollapse('" + request.getParameter("root_pid") + "','" + request.getParameter("pid") + "', %s);";
            } else {
                offsetUrl = "javascript:gotoPageOffsetInTree(%s, '" + div + "', '" + filters + "', '" + request.getParameter("pid") + "');";

            }

            if (numDocs > numHits && numHits > 0) {
                String navigationPageTemplate = "";
                int N_WIDTH = 10;
                int offset = 0;
                if (request.getParameter("offset") != null) {
                    offset = Integer.parseInt((String) request.getParameter("offset"));
                }
                int maxPageIndex = Math.min(((numDocs - 1) / numHits + 1), 4000 / numHits);
                int pageIndex = offset / numHits;
                int nStart;
                int nEnd;
                if (pageIndex <= (N_WIDTH / 2) || maxPageIndex <= N_WIDTH) {
                    nStart = 1;
                } else {
                    nStart = pageIndex - (N_WIDTH / 2);
                }
                nEnd = nStart + N_WIDTH;
                if (nEnd > maxPageIndex) {
                    nEnd = maxPageIndex;
                    nStart = nEnd - N_WIDTH;
                    if (nStart < 1) {
                        nStart = 1;
                    }
                }
                StringBuffer navigationPages = new StringBuffer();
                if (nStart > 1) { // skok na zacatek
                    navigationPages.append("<a href='" + String.format(offsetUrl, "0") + "'>&laquo;</a> ");
                }

                if (offset >= numHits) { // predesla strana
                    navigationPages.append("<a class=\"next\" href=\"" + String.format(offsetUrl, (offset - numHits)) + "\">&lt;&lt;</a> ");
                }

                maxPageIndex = nEnd;
                String pismeno;
                for (pageIndex = nStart; pageIndex <= maxPageIndex; pageIndex++) {
                    navigationPages.append((pageIndex == nStart) ? "" : " ");
                    nStart = (pageIndex - 1) * numHits;
                    nEnd = (pageIndex) * numHits;
                    if (nEnd > numDocs) {
                        nEnd = numDocs;
                    }
                    if (nStart == offset) {
                        navigationPages.append("<b>" + pageIndex + "</b> ");
                    } else {
                        navigationPages.append("<a href=\"" + String.format(offsetUrl, String.valueOf(nStart)) + "\">" + pageIndex);
                        if (includeAbeceda) {
                            pismeno = abecedaFacet.getDisplayNameByAcumulatedCount(nStart, numHits);
                            if (!pismeno.equals("")) {
                                navigationPages.append(" (" + pismeno + ")");
                            }
                        }
                        navigationPages.append("</a> ");
                    }
                }
                if (offset < (numDocs - numHits)) { // dalsi strana
                    navigationPages.append("<a class=\"next\" href=\"" + String.format(offsetUrl, (offset + numHits)) + "\">&gt;&gt;</a> ");
                }
                expandedPaginationStr = navigationPages.toString();
            }
%>
<div class="pagination">
    <%=expandedPaginationStr%>
</div>