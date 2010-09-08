<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.views.item.ItemViewObject"%>
<%@page import="cz.incad.Kramerius.views.item.menu.ItemMenuViewObject"%>


<%
	Injector inj = (Injector)application.getAttribute(Injector.class.getName());
	
	// view objekt pro stranku = veskera logika 
	ItemViewObject itemViewObject = new ItemViewObject();
	inj.injectMembers(itemViewObject);

	// lokalizacni kontext
	LocalizationContext lctx= inj.getProvider(LocalizationContext.class).get();
	pageContext.setAttribute("lctx", lctx);
	
	// ukladani nejoblibenejsich 
	itemViewObject.saveMostDesirable();
	pageContext.setAttribute("itemViewObject", itemViewObject);
%>

<%@ include file="inc/initVars.jsp" %>



<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="cz.incad.kramerius.utils.RESTHelper"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager"%>
<%@page import="cz.incad.kramerius.processes.DefinitionManager"%>
<%@page import="cz.incad.kramerius.MostDesirable"%>
<%@page import="cz.incad.kramerius.utils.pid.PIDParser"%><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
    <%@ include file="inc/html_header.jsp" %>
    <body >
		<!--  procesy - dialogy -->
	    <%@ include file="dialogs/_processes_dialogs.jsp" %>
        <table style="width:100%" id="mainItemTable"><tr><td align="center">
		
        <%@ include file="inc/searchForm.jsp" %>
        <table>
            <tr valign='top'>
                <td><%//@ include file="usedFilters.jsp" %></td>
            </tr>
        </table>
        
        <table class="main">
            <tr valign='top'>
                <td colspan="2" valign="middle" align="center">
                    <table style="width: 100%"><tr>
                        <td width="20px" align="center"><a class="prevArrow"  href="javascript:selectPrevious();"><img src="img/la.png" border="0" /></a></td>
                        <td align="center"><%--<%@ include file="gwtView.jsp" %>--%>
                        <%@ include file="thumbsViewer.jsp" %></td>
                        <td width="20px" align="center"><a class="nextArrow"  href="javascript:selectNext();"><img src="img/ra.png" border="0" /></a></td>
                    </tr></table>
                    </td>
            </tr>
            <tr valign='top'>
                <td>
                    
                    <table cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td valign="top" align="center" width="20px"><a class="prevArrow"  href="javascript:selectPrevious();"><img src="img/la.png" border="0" /></a></td>
                        <td valign="top" align="center" id="mainContent">
							<!-- 
                            <div style="position:relative;top:0px;">

                            <div style="position:absolute;top:3px;right:3px;">
                                <a href="javascript:showFullImage('${itemViewObject.imagePid}')"><img border="0" src="img/lupa_shadow.png" align="right" alt="" />
                                </a>
                            </div>
                            </div>
							 -->
							 
		        			<script type="text/javascript" 
        		      			src="http://seadragon.com/ajax/0.8/seadragon-min.js">
					        </script>
					        
					        <script type="text/javascript">
					            var viewer = null;
								
					            function init() {
					            	viewer = new Seadragon.Viewer("container");
									viewer.clearControls();
									viewer.addControl(viewer.getNavControl(),  Seadragon.ControlAnchor.TOP_RIGHT);
									showDeepZoomFile(currentSelectedPage);
					            }

					            
					            Seadragon.Utils.addEvent(window, "load", init);
								// lokalizace
								//dictionary['rightMsg']
					            Seadragon.Strings.setString("Tooltips.FullPage",dictionary["deep.zoom.Tooltips.FullPage"]);
					            Seadragon.Strings.setString("Tooltips.Home",dictionary["deep.zoom.Tooltips.Home"]);
					            Seadragon.Strings.setString("Tooltips.ZoomIn",dictionary["deep.zoom.Tooltips.ZoomIn"]);
					            Seadragon.Strings.setString("Tooltips.ZoomOut",dictionary["deep.zoom.Tooltips.ZoomOut"]);

					            Seadragon.Strings.setString("Errors.Failure",dictionary["deep.zoom.Errors.Failure"]);
					            Seadragon.Strings.setString("Errors.Xml",dictionary["deep.zoom.Errors.Xml"]);
					            Seadragon.Strings.setString("Errors.Empty",dictionary["deep.zoom.Errors.Empty"]);
					            Seadragon.Strings.setString("Errors.ImageFormat",dictionary["deep.zoom.Errors.ImageFormat"]);
					            
		            		</script>

							<div id="container" style="padding-top:10px; width: 500px;height: 400px; color: black;"></div>
							<div id="securityError" style="padding-top:10px; width: 500px;height: 400px; color: black; display:none;">
								<fmt:message bundle="${lctx}" key="rightMsg"></fmt:message>
							</div>
							<div id="loadingDeepZoomImage" style="padding-top:10px; width: 500px;height: 400px; color: black; display:none;">
								<fmt:message bundle="${lctx}" key="deep.zoom.loadingImage"></fmt:message>
							</div>
                            <!-- 
                            <a href="javascript:showFullImage('${itemViewObject.imagePid}')" class="lighbox">
                                <img border="0" width="650px" onerror="showError();" src="${itemViewObject.firstPageImageUrl}" id="imgBig" alt="">
                            </a>
                             -->
                            
                            </td>
                        <td valign="top" align="center" width="20px"><a class="nextArrow"  href="javascript:selectNext();"><img src="img/ra.png" border="0" /></a></td>
                    </tr></table>
                </td>
                <td class="itemMenu">
                    <div id="itemTree">
                        <script>
                            var firstCalled = false;
                        </script>
	                    <%@ include file="inc/details/itemMenu.jsp" %>
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="3" align="right" id="donatorContainer"></td>
            </tr>
        </table>
        <table>
            <tr valign='top'>
                <td><%@ include file="templates/footer.jsp" %></td>
            </tr>
        </table>
        </td></tr></table>

<!-- dialogs -->
<div id="pdf_options" style="display:none;">
		<h3 id="pdf_desc_head"></h3>
		<div id="pdf_desc_content"></div>
		<div id="pdf_page_range" style="margin-top:10px;">
	        <strong>Rozsah stran:&nbsp;(max.&nbsp;<%=kconfig.getProperty("generatePdfMaxRange")%>)</strong><br>&nbsp;&nbsp;                           
    	    <input type="text" id="genPdfStart" value="1" name="genPdfStart" size="3" > -
    	    <input type="text" id="genPdfEnd" value="1" name="genPdfEnd" size="3">
		</div>
</div>


<div id="fullImageContainer" style="display:none;">
    <div id="djvuContainer" style="display:none;">
        <iframe src="" frameborder="0" width="100%" height="100%"></iframe>
        <%--
        <object width="100%" border="0" height="100%" style="border: 0px none ;" codebase="http://www.lizardtech.com/download/files/win/djvuplugin/en_US/DjVuControl_en_US.cab" classid="clsid:0e8d0700-75df-11d3-8b4a-0008c7450c4a" id="docframe" name="docframe">
            <param name="src" value="" />
            <embed width="100%" height="100%" src="" type="image/vnd.djvu" id="docframe2" name="docframe2"/>
            If you don't see picture, your browser has no plugin to view DjVu picture files. You can install plugin from <a target="_blank" href="http://www.celartem.com/en/download/djvu.asp"><b>LizardTech</b></a>.<br/>
            <a href="http://www.celartem.com/en/download/djvu.asp">File download</a><br/> <br/> <br/> 
        </object>
        --%>
    </div>
    
    <c:if test="${param.format == 'application/pdf'}">
    <div id="pdfContainer" style="display:none;">  
        <input type="hidden" id="pdfPage" name="pdfPage" value="${itemViewObject.page}" />
        <iframe src="" width="100%" height="100%"></iframe>
        <%--
        <object data="djvu?uuid=${itemViewObject.firstUUID}&outputFormat=RAW&page=${itemViewObject.page-1}" type="application/pdf" width="100%" height="100%" page="${itemViewObject.page-1}"></object>
        --%>
    </div>
    </c:if>
    <div id="imgContainer" style="display:none;" align="center">
        <img id="imgFullImage" src="img/empty.gif" />
    </div>
    <div id="divFullImageZoom" style="display:none;">
        <span class="ui-dialog-titlebar-zoom"><fmt:message bundle="${lctx}">velikost</fmt:message>: <select onchange="changeFullImageZoom()" id="fullImageZoom">
            <option value="width"><fmt:message bundle="${lctx}">šířka okna</fmt:message></option>
            <option value="height"><fmt:message bundle="${lctx}">výška okna</fmt:message></option>
            <option value="0.1">10%</option>
    <option value="0.2" >20%</option>
    <option value="0.3" >30%</option>
    <option value="0.4" >40%</option>
    <option value="0.5" >50%</option>
    <option value="0.6" >60%</option>
    <option value="0.7" >70%</option>
    <option value="0.8" >80%</option>
    <option value="0.9" >90%</option>
    <option value="1" selected="selected" >100%</option></select></span>
    </div>
</div>
</body></html>