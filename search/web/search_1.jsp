<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<c:set var="fedoraSolr" value="http://194.108.215.227:8080/solr/select/select" />
<c:set var="fedoraHost" value="http://194.108.215.227:8080/fedora" />
<%//c:url var="url" value="http://localhost:8983/solr/select/select" 
//http://194.108.215.227:8080/solr/select?indent=on&version=2.2&q=fedora.model%3A%22info%3Afedora%2Fmodel%3Apage%22&start=0&rows=10&fl=*%2Cscore&qt=standard&wt=xslt&explainOther=&hl.fl=&facet=true&facet.field=fedora.model&tr=example.xsl
%> 
<c:set var="pageType" value="search" />
<jsp:useBean id="pageType" type="java.lang.String" />
<c:url var="url" value="${fedoraSolr}" >
    <c:choose>
        <c:when test="${empty param.q}" >
            <c:param name="q" value="*:*" />
            <c:if test="${empty param.fq}">
                <c:param name="rows" value="0" />
            </c:if>
        </c:when>
        <c:when test="${param.q != null}" >
            <c:if test="${fn:containsIgnoreCase(param.q, '*')}" >
                <c:param name="qt" value="czparser" />
            </c:if>
            <c:param name="q" value="${param.q}" />
        </c:when>
        
    </c:choose>
            <c:param name="rows" value="${param.rows}" />
    <c:param name="facet.field" value="fedora.model" />
    <c:param name="f.fedora.model.facet.sort" value="false" />
    <c:param name="facet.field" value="abeceda" />
    <c:param name="facet.field" value="rok" />
    <c:param name="f.abeceda.facet.sort" value="false" />
    <c:param name="facet" value="true" />
    <c:param name="facet.mincount" value="1" />
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:param name="fq" value="${fqs}" />
    </c:forEach>
    <c:param name="start" value="${param.offset}" />
    <c:param name="sort" value="${param.sort}" />
</c:url>

<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
</c:catch>
<c:if test="${exceptions != null}" >
    <c:import url="empty.xml" var="xml" />
</c:if>


<x:parse var="doc" xml="${xml}"  />
<c:set var="numDocs" scope="request" >
    <x:out select="$doc/response/result/@numFound" />
</c:set>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ include file="inc/proccessFacets.jsp" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">    
    <%@ include file="header.jsp" %>
    
    <body leftmargin="0" topmargin="0" onload="resizeDocFrame();" marginheight="0" marginwidth="0">
        <c:if test="${param.debug}" >
            <c:out value="${url}" />
        </c:if>
        
        
        <!-- Start of header.jsp -->
        
        
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tbody><tr>
                    <td height="84" background="img/main_bg_grey.gif" width="13"><img src="img/spacer.gif" alt="" height="1" border="0" width="13"></td>
                    <td class="mainHead">
                        <a href="http://kramerius.qbizm.cz/" title="System Kramerius ">
                            <img src="img/logo_nk_portal.jpg" alt="System Kramerius " border="0">
                    </a></td>
                    <td class="mainHead" width="100%"><img src="img/spacer.gif" alt="" height="1" border="0" width="50"></td>
                    <td class="mainHead"><div align="right"><img src="img/main_book_portal.jpg" alt="" border="0"></div></td>
                </tr>
        </tbody></table>
        <!-- End of header.jsp -->
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tbody><tr>
                    <td height="2" background="img/main_bg_grey.gif" width="13"><img src="img/spacer.gif" alt="" height="2" border="0" width="13"></td>
                    <td class="mainHead" width="100%"><img src="img/spacer.gif" alt="" height="1" border="0" width="1"></td>
                </tr>
        </tbody></table>
        <!-- Start of serviceMenu.jsp -->
        
        <script language="JavaScript" type="text/javascript" src="js/menu.js"></script>
        
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tbody><tr>
                    <td height="19" background="img/main_bg_grey.gif" width="13"><img src="img/spacer.gif" alt="" height="1" border="0" width="13"></td>
                    <td class="mainHeadGrey" width="1%"><img src="img/spacer.gif" alt="" height="1" border="0" width="1"></td>
                    <td class="mainHeadGrey" width="99%">
                        <div id="serviceMenu">
                            <ul> 
                                <li>
                                    <a href="javascript:printIt();" class="mainServ">TISK 
                                    </a>&nbsp; - &nbsp; 
                                </li>
                                <li>
                                    <a href="javascript:openhelp('/kramerius/help_cs.jsp',%20'P3');" class="mainServ">NÁPOVĚDA</a>
                                </li>
                                <li>&nbsp; - &nbsp;</li>
                                <li><a href="http://kramerius.nkp.cz/kramerius/ShowLinks.do" class="mainServ">ODKAZY</a></li>
                                <c:choose>
                                    <c:when test="${bundleVar.locale.language=='en'}">
                                        <li>&nbsp; - &nbsp;</li>
                                        <li><a href="javascript:setLanguage('cs');" class="mainServ">ČESKY</a>
                                    </c:when>
                                    <c:otherwise>
                                        <li>&nbsp; - &nbsp;</li>
                                        <li><a href="javascript:setLanguage('en');" class="mainServ">ENGLISH</a>
                                        </c:otherwise>
                                    </c:choose>
                                </li>
                            </ul>
                        </div>
                    </td>
                    <td class="mainHeadGrey" width="7px"><img src="img/spacer.gif" alt="" height="1" border="0" width="7"></td>
                </tr>
                <tr>
                    <td height="2" background="img/main_bg_grey.gif" width="13" >
                    <img src="img/spacer.gif" alt="" height="2" border="0" width="13"></td>
                    <td colspan="3" width="100%"><img src="img/spacer.gif" alt="" height="1" border="0" width="1"></td>
                </tr>
                <tr>
                    <td height="1" background="img/main_bg_grey.gif" width="13"><img src="img/spacer.gif" alt="" height="1" border="0" width="13"></td>
                    <td class="mainHeadGrey" colspan="3" width="100%"><div align="right"><img src="img/spacer.gif" alt="" height="1" border="0" width="740"></div></td>
                </tr>
        </tbody></table>
        <!-- End of serviceMenu.jsp -->
        
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tbody><tr>
                    <td height="8" background="img/main_bg_grey.gif" width="13"><img src="img/spacer.gif" alt="" height="6" border="0" width="13"></td>
                    <td width="100%"><img src="img/spacer.gif" alt="" height="1" border="0" width="1"></td>
                </tr>
        </tbody></table>
        <!-- Start of used filters -->
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tbody><tr>
                    <td height="6" background="img/main_bg_grey.gif" width="13"><img src="img/spacer.gif" alt="" height="6" border="0" width="13"></td>
                    <td width="11"><img src="img/spacer.gif" alt="" height="1" border="0" width="11"></td>
                    <td class="mainNav" nowrap="nowrap" width="100%"><fmt:message>Použité filtry</fmt:message>: 
                        <a href="./" class="mainNav">KRAMERIUS</a>
                        <%@ include file="usedFilters.jsp" %>
                    </td>
                </tr>
        </tbody></table>
        <!-- End used filters -->
        
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tbody><tr>
                    <td height="6" background="img/main_bg_grey.gif" width="13"><img src="img/spacer.gif" alt="" height="6" border="0" width="13"></td>
                    <td width="100%"><img src="img/spacer.gif" alt="" height="1" border="0" width="1"></td>
                </tr>
        </tbody></table>
        <table height="82%" border="0" cellpadding="0" cellspacing="0" width="100%">
            <tbody><tr>
                    <td height="6" background="img/main_bg_grey.gif" valign="top" width="13"><img src="img/spacer.gif" alt="" height="6" border="0" width="13"></td>
                    <td class="leftMenu" height="100%" valign="top" width="151">
                        <!-- Start of mainMenuPeriodical.jsp -->
                        <table height="100%" border="0" cellpadding="0" cellspacing="0" width="100%">
                            <tbody><tr>
                                    <td class="leftMenu" valign="top">
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                            <tbody><tr>
                                                    <td colspan="2" valign="top"><img src="img/main_linka.gif" alt="" height="9" border="0" width="151"></td>
                                                </tr>
                                                <tr>
                                                    <td colspan="2" valign="top"><img src="img/spacer_grey.gif" alt="" height="1" border="0" width="151"></td>
                                                </tr>
                                               
                                                <tr>
                                                    <td colspan="2" valign="top"><img src="img/spacer_grey.gif" alt="" height="1" border="0" width="151"></td>
                                                </tr>
                                                <tr>
                                                    <td class="mainLeftMenu" width="11"><img src="img/spacer.gif" alt="" height="19" border="0" width="11"></td>
                                                    <td class="mainLeftMenu" width="151">
                                                        <%@ include file="resultsNav.jsp" %>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td colspan="2" valign="top"><img src="img/spacer_grey.gif" alt="" height="1" border="0" width="151"></td>
                                                </tr>
                                                
                                                <tr>
                                                    <td class="mainLeftMenu" width="11"><img src="img/spacer.gif" alt="" height="19" border="0" width="11"></td>
                                                    <td class="mainLeftMenu" width="151"><strong>Výběr titulu</strong></td>
                                                </tr>
                                                <tr>
                                                    <td colspan="2" valign="top"><img src="img/spacer_grey.gif" alt="" height="1" border="0" width="151"></td>
                                                </tr>
                                                <tr>
                                                    <td colspan="2" class="leftMenu">&nbsp;</td>
                                                </tr>
                                                <tr>
                                                    <td colspan="2" valign="top"><img src="img/spacer_grey.gif" alt="" height="1" border="0" width="151"></td>
                                                </tr>
                                                <tr>
                                                    <td class="mainLeftMenu" colspan="2" style="padding: 4px 6px 0px;">
                                                        
                                                        
                                                        
                                                        <!-- Start of tile small_search_snippet.jsp -->
                                                        
                                                        <form name="searchForm" method="GET" action="search.jsp">
                                                            <input id="debug" name="debug" type="hidden" value="<c:out value="${param.debug}" />" />
                                                            <input id="language" name="language" type="hidden" value="<c:out value="${param.language}" />" />
                                                            <table class="smallSearch">
                                                                <tbody><tr>
                                                                        <td>
                                                                            <input type="text"  alt="Hledaný výraz" name="q" id="q"
                                                                               value="<c:out value="${param.q}" />" size="18" class="text" type="text"></td>
                                                                    </tr>
                                                                    <tr>
                                                                        <td><input type="submit" class="submit" title="<fmt:message>Vyhledat</fmt:message>" alt="&gt;" value="<fmt:message>Vyhledat</fmt:message>" name="submit" id="ar_submit"/>
                                                                        </td>
                                                                    </tr>
                                                                    <tr>
                                                                        <td class="advancedSearch">
                                                                            <a href="http://kramerius.nkp.cz/kramerius/ShowAdvancedSearch.do?searchType=periodical">Pokročilé vyhledávání</a>
                                                                        </td>
                                                                    </tr>
                                                            </tbody></table>
                                                        </form>
                                                        <!-- End of tile small_search_snippet.jsp -->
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td colspan="2" valign="top"><img src="img/spacer_grey.gif" alt="" height="1" border="0" width="151"></td>
                                                </tr>
                                                
                                                
                                                <!-- Start of adminMenu.jsp -->

<!-- End of adminMenu.jsp -->
                                                
                                        </tbody></table>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftMenu" valign="bottom" width="99%"><!-- Start of tiraz.jsp -->
                                        <br>
                                        <div id="tiraz" align="center">
                                            ©2003-2009<br>
                                            <a class="tiraz" href="javascript:openhelp('/kramerius/ShowVersion.do');">Developed</a>
                                            under GNU GPL by 
                                            <a href="http://www.incad.cz/">Incad</a>, <a href="http://www.nkp.cz/">NKČR</a> and <a href="http://www.lib.cas.cz/">KNAV</a><br><br>
                                        </div>
                                    <!-- End of tiraz.jsp --></td>
                                </tr>
                        </tbody></table>
                        <!-- End of mainMenuPeriodical.jsp -->
                    </td>    
                    <td height="1" valign="top" width="10"><img src="img/spacer.gif" alt="" height="1" border="0" width="10"></td>
                    <td class="textpole" height="100%" valign="top" width="100%">
                        <!-- Start of body_P3.jsp -->
                        <table border="0" cellpadding="0" cellspacing="0" width="100%">
                            <tbody><tr>
                                    <td colspan="3" height="9" background="img/main_linka.gif" valign="top" width="100%"><img src="img/spacer.gif" alt="" height="9" border="0" width="564"></td>
                                </tr>
                                <tr>
                                    <td colspan="3" height="1" background="img/main_linka.gif" valign="top" width="100%"><img src="img/spacer_grey.gif" alt="" height="1" border="0" width="564"></td>
                                </tr>
                                <tr>
                                    <td colspan="3" width="100%"><img src="img/spacer.gif" alt="" height="2" border="0" width="1"></td>
                                </tr>
                                <tr>
                                    <td class="textpole" colspan="3">
                                        <table border="0" cellpadding="0" cellspacing="0">
                                            <tbody><tr>
                                                    <td class="textpole" colspan="3"><img src="img/spacer.gif" alt="" height="10" border="0" width="31"></td>
                                                </tr>
                                                <tr>
                                                    <td class="textpole" width="31"><img src="img/spacer.gif" alt="" height="1" border="0" width="31"></td>
                                                    <td class="textpole">
                                                        <div id="tisk">
                                                                <c:out value="${numDocs}" />
                                                                <table border="0">
                                                                    <tbody>
                                                                        <%@ include file="paginationPageNum.jsp" %>
                                                                        <%@ include file="resultsMain.jsp" %>
                                                                </tbody></table>
                                                        </div>
                                                    </td>
                                                    <td class="textpole" width="31"><img src="img/spacer.gif" alt="" height="1" border="0" width="31"></td>
                                                </tr>
                                        </tbody></table>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="textpole"><img src="img/spacer.gif" alt="" height="10" border="0" width="1"></td>
                                </tr>
                        </tbody></table>

                    </td>
                </tr>
        </tbody></table>
</body></html>