<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
// zaklad od kamila 
            String expandedPaginationStr = "";
            int numHits = 10;
            int numDocs = Integer.parseInt((String) request.getAttribute("numDocs"));
            if (numDocs > numHits) {
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
                    navigationPages.append("<a href='javascript:gotoPageOffset(0)'>&laquo;</a> ");
                }
                /*
                 *             if (offset >= numHits) { // predesla strana
                navigationPages.append("<a href='javascript:gotoPageOffset(" +
                (offset - numHits) + ");'>&lt;</a>");
                }
                 */
                maxPageIndex = nEnd;
                for (pageIndex = nStart; pageIndex <= maxPageIndex; pageIndex++) {
                    navigationPages.append((pageIndex == nStart) ? "" : " ");
                    nStart = (pageIndex - 1) * numHits;
                    nEnd = (pageIndex) * numHits;
                    if (nEnd > numDocs) {
                        nEnd = numDocs;
                    }
                    if (nStart == offset) {
                        //navigationPages.append("<b>" + (nStart + 1) + "-" + nEnd + "</b>");
                        navigationPages.append("<b>" + pageIndex + "</b> ");
                    } else {
                        //navigationPages.append("<a href=\"javascript:gotoPageOffset(" + String.valueOf(nStart) + ");\">" + (nStart + 1) + "-" + nEnd + "</a>");
                        navigationPages.append("<a href='javascript:gotoPageOffset(" +
                                String.valueOf(nStart) + ");'>" + pageIndex + "</a> ");
                    }
                }
                if (offset < (numDocs - numHits)) { // dalsi strana
                    //navigationPages.append(" | <a href=\"").append(navigationPageTemplate.replace("INSERT_OFFSET", String.valueOf(offset + numHits))).append("\">&gt;</a>");
                    navigationPages.append("<a class=\"next\" href=\"javascript:gotoPageOffset(" +
                            (offset + numHits) + ");\">&gt;&gt;</a> ");
                }
                expandedPaginationStr = navigationPages.toString();
            }
%>
<%=expandedPaginationStr%>