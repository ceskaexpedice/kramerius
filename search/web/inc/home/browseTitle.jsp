<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<%
    Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
    LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
    pageContext.setAttribute("lctx", lctx);
%>

        <div class="ui-tabs-panel ui-corner-bottom">
            <table width="100%">
                <tr>
                    <td valign="top" colspan="2">
                        <div style="position:relative;"><fmt:message bundle="${lctx}" key="filter.maintitle" />: 
                        <input type="text" id="br_browse_title" onkeyup="doSuggest(this)" size="70" />
                        <div id="br_browse_title_res" class="suggest border shadow">
                            <div class="header"><a href="javascript:hideSuggest('#br_browse_title_res');"><span class="ui-icon ui-icon-close" style="float:right;">close</span></a></div>
                            <div class="content"></div>
                        </div>    
                        </div>
                    </td>
                </tr>
                <tr>
                    <td width="90%" valign="top">
                        <div id="browse_title" class="autocomplete" onscroll="checkScroll('browse_title');"></div>
                    </td>
                    <td align="center" valign="top" id="letters_browse_title" class="letters letters_l">
<%
String[] pismena = {"0","A","B","C","Č","D","E","F","G","H","CH","I","J","K","L","M","N","O","P","Q","R","Ř","S","Š","T","U","V","W","X","Y","Z","Ž"};
pageContext.setAttribute("pismena", pismena);
%>
<div class="all"><a class="browse_title" href="#"><span class="ui-icon ui-icon-arrowthickstop-1-n" title="<fmt:message bundle="${lctx}" key="search.terms.start" />">all</span></a></div>
<c:forEach var="p" items="${pismena}"><div class="${p}"><a class="browse_title" href="#">${p}</a></div></c:forEach>
                    </td>
                </tr></table>
<script type="text/javascript">
    $(document).ready(function(){
        doBrowse('', 'browse_title');
    });
</script>
        </div>