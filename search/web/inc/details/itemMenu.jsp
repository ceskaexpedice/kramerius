<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page isELIgnored="false"%>
<%@ page import="java.util.*"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager,cz.incad.kramerius.processes.DefinitionManager" %>

<c:set var="href" value="#{href}" />
<c:set var="label" value="#{label}" />
<c:set var="level" value="0" />
<c:if test="${!empty param.level}">
    <c:set var="level" value="${param.level}" />
</c:if>
<%-- fill path up to the end --%>
<c:forEach var="menu" varStatus="status" items="${itemViewObject.menus}">
<c:choose>
<c:when test="${level==0 || status.count>1}">
    <c:set var="cur_level" value="${status.count + level}" />
    <c:set var="obj" value="#tabs_${cur_level}" />
    <c:if test="${!fn:contains(menu.uuid, '@')}">
    <div id="tabs_<c:out value="${cur_level}" />" style="padding: 2px;"
         pid="<c:out value="${menu.uuid}" />" >
        <ul>
            <li class="${itemViewObject.models[status.count -1]}"><a
                    href="#tab<c:out value="${status.count + level}" />-<c:out value="${itemViewObject.models[status.count -1]}" />"><fmt:message
                        bundle="${lctx}">fedora.model.<c:out value="${itemViewObject.models[status.count -1]}" /></fmt:message> </a><img width="12px" src="img/empty.gif" class="op_list"
                                    onclick="toggleRelsList(this, '<c:out value="${itemViewObject.models[status.count -1]}" />')" /><img width="12px" src="img/lupa.png" class="searchInsideButton" alt="search"
                                    onclick="showSearchInside(<c:out value="${status.count + level}" />, '<c:out value="${itemViewObject.models[status.count -1]}" />')" /></li>
        </ul>
        <div id="tab<c:out value="${cur_level}" />-<c:out value="${itemViewObject.models[status.count -1]}" />"
            class="<c:out value="${itemViewObject.models[status.count -1]}  ui-tabs-panel ui-widget-content ui-corner-bottom" />"
            pid="<c:out value="${menu.uuid}" />">
          <%  
          pageContext.setAttribute("language", lctx.getLocale().getLanguage() );
          %>
           <c:url var="url" value="${kconfig.applicationURL}/inc/details/getItemMenuInfo.jsp"  >
               <c:param name="level" value="${status.count}" />
               <c:param name="pid" value="${menu.uuid}" />
               <c:param name="language" value="${language}" />
           </c:url>
           <c:import url="${url}" var="infoa" charEncoding="UTF-8" scope="request"  />
                    <div class="relList" style="display: none;"
                         id="list-<c:out value="${itemViewObject.models[status.count -1]}" />"></div>
                    <div id="info-<c:out value="${itemViewObject.models[status.count -1]}" />"
                         style="min-height: 16px;"><c:out value="${infoa}" escapeXml="false" />
                    </div>
    </c:if>

    <script language="javascript">
    	//$(document).ready(function(){

            var obj = "<c:out value="${obj}" />";
            var tabTemp = '<li class="${href}"><a href="<c:out value="${href}" />"><c:out value="${label}" /></a><img width="12px" src="img/empty.gif" class="op_list" onclick="toggleRelsList(this, \'<c:out value="${href}" />\')" /><img width="12" src="img/lupa.png" class="searchInsideButton" alt="search" ' +
                           'onclick="showSearchInside(<c:out value="${status.count + level}" />, \'<c:out value="${href}" />\')"  /></li>';
            $(obj).tabs({
                tabTemplate: tabTemp,
                show: function(event, ui){
                    updateThumbs();
                },
                select: function(event, ui){
                    initPage = "";
                    changingTab=true;
                }
            });
            ${menu.updateSelection}

        //});

    </script>
    </c:when>
        </c:choose>
        </c:forEach>
        <c:forEach var="model" varStatus="status" items="${itemViewObject.models}">
            <c:choose>
                <c:when test="${level==0 || status.count>1 && !fn:contains(menu.uuid, '@')}">
                </div>
            </div>
        </c:when>
    </c:choose>
</c:forEach>


<script language="javascript">
     setTvContainerWidth();
     
    $(document).ready(function(){
        $('#tabs_1>ul>li>img.op_list').hide();
        currentSelectedPage = '<c:out value="${itemViewObject.lastUUID}" />';
        getRels(false);
        getFirstLevelMenu('<c:out value="${itemViewObject.firstUUID}" />',
                            '<c:out value="${itemViewObject.models[0]}" />');
        selectPage('<c:out value="${itemViewObject.lastUUID}" />');
        checkDonator('<c:out value="${itemViewObject.firstUUID}" />');
    });
    
    initParent = '<c:out value="${itemViewObject.parentUUID}" />';
    initPage = '<c:out value="${itemViewObject.lastUUID}" />';
    
    
    function startPage(){
        currentSelectedPage = initPage;
        selectPage(initPage);
    }
    
    function startItemMenu(){
        $('.item_options').show();
        $('.menu_activation').unbind('mouseenter');
        $('.item_options').unbind('mouseleave');
        
        $('.item_options').each(function(){
            var l = $(this).parent().width() + $($(this).parent()).offset().left - 9;
            $(this).css('left',l);
        });
        $('.menu_activation').bind('click', function(){
            $('.item_options').stop();
            var il = $(this).parent().parent().width() + $(this).parent().parent().offset().left - 9;
            if($(this).parent().offset().left == il){
                $(this).parent().css({'width': 129, 'left': '-=120'});
            }
        });
        
        $('.item_options').bind('mouseleave', function(){
            $('.menu_activation').stop();
            var il = $(this).parent().width() + $(this).parent().offset().left -9;
            var fl = il - 120;
            if($(this).offset().left == fl){
                $(this).animate({
                    width: 9,
                    left: '+=120'
                }, 50);
            }else{
                $(this).css('left',il);
                $(this).css('width',9);
            }
        });
    }
    
</script>
