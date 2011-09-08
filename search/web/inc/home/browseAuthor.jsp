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
                        <div style="position:relative;"><fmt:message bundle="${lctx}" key="common.author" />: 
                        <input type="text" id="br_browse_autor" onkeyup="doSuggest(this)" size="70"/>
                        <div id="br_browse_autor_res" class="suggest border shadow">
                            <div class="header"><a href="javascript:hideSuggest('#br_autor_res');"><span class="ui-icon ui-icon-close" style="float:right;">close</span></a></div>
                            <div class="content"></div>
                        </div></div>
                    </td>
                </tr>
                <tr>
                    <td width="90%" valign="top">
                        <div id="browse_autor" class="autocomplete"></div>
                    </td>
                    <td align="center" valign="top" id="letters_browse_autor" class="letters letters_l">
<%
String[] pismena = {"0","A","B","C","Č","D","Ď","E","F","G","H","CH","I","J","K","L","M","N","Ň","O","P","Q","R","Ř","S","Š","T","Ť","U","V","W","X","Y","Z","Ž"};
pageContext.setAttribute("pismena", pismena);
%>
<c:forEach var="p" items="${pismena}"><div class="${p}"><a href="#">${p}</a></div></c:forEach>
                    </td>
                </tr></table>
<script type="text/javascript">
    $(document).ready(function(){
        doBrowse('', 'browse_autor');
        
        $("#browse_autor").bind('scroll', function(event){
            var id = $(this).attr('id');
            if($('#'+id+">div.more_terms").length>0 && isTermVisible(id)){
                getMoreTerms(id);
            }
            selectLetter(id);
        });

    });
</script>
        </div>