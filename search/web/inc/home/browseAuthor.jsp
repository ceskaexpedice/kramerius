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
                    <td valign="top" colspan="2"><fmt:message bundle="${lctx}" key="common.author" /></td>
                </tr>
                <tr>
                    <td width="90%" valign="top">
                        <div id="browse_autor" class="autocomplete"></div>
                    </td>
                    <td align="center" valign="top" id="letters_browse_autor" class="letters letters_l">
<%
String[] pismena = {"0","A","Á","B","C","Č","D","Ď","E","É","Ě","F","G","H","CH","I","Í","J","K","L","M","N","Ň","O","Ó","P","Q","R","Ř","S","Š","T","Ť","U","Ú","Ů","V","W","X","Y","Ý","Z","Ž"};
pageContext.setAttribute("pismena", pismena);
%>
<c:forEach var="p" items="${pismena}"><div class="${p}"><a href="#">${p}</a></div></c:forEach>
                    </td>
                </tr></table>
        </div>
<script type="text/javascript">


    $(document).ready(function(){
        titleDivTopBorder = $('#browse_autor').offset().top;
        titleDivBottomBorder = titleDivTopBorder + $('#browse_autor').height() ;
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