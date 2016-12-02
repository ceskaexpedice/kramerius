<%@ page import="cz.incad.Kramerius.views.HeaderViewObject"%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ page isELIgnored="false"%>
<%@ page trimDirectiveWhitespaces="true"%>

<%
            String fromField = "f1"; 
            String toField = "f2";
            String fromValue = "";
            String toValue = "";
%>

<!--  google analytics view -->
<view:object name="ga" clz="cz.incad.Kramerius.views.GoogleAnalyticsViewObject"></view:object>

<!-- facebook button view -->
<view:object name="fb" clz="cz.incad.Kramerius.views.social.FacebookLikeItButton"></view:object>

<!-- google plus button view -->
<view:object name="gplus" clz="cz.incad.Kramerius.views.social.GooglePlusButton"></view:object>

<!-- tweet button -->
<view:object name="tweet" clz="cz.incad.Kramerius.views.social.TweetButton"></view:object>

<!-- virtual collections -->
<view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>


<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.service.ResourceBundleService"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.Enumeration"%><head>

    <!-- header view object -->
    <view:object name="headerViewObject" clz="cz.incad.Kramerius.views.HeaderViewObject"></view:object>


    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Cache-Control" content="no-cache" />
    <meta name="description" content="Digitized documents access aplication." />
    <meta name="keywords" content="periodical, monograph, library,  book, publication, kramerius, fedora" />
    <meta name="author" content="INCAD, www.incad.cz" />
    <meta http-equiv="X-UA-Compatible" content="IE=Edge"  />


     <c:if test="${fb.buttonEnabled || gplus.buttonEnabled}">
        <meta property="og:title" content="${fb.title}"/>

        <meta property="og:type" content="${fb.metadataType}"/>
        <meta property="og:url" content="${fb.shareURL}"/>
        <meta property="og:image" content="${fb.metadataImage}"/>
        <meta property="og:site_name" content="${fb.applicationTitle}"/>

        <meta property="og:description"
              content="${fb.description}"/>
              
        <link rel="canonical" href="${fb.shareURL}" />
    </c:if>


    <link rel="icon" href="img/favicon.ico"/>
    <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />
    <%
    String theme;
    String parameter = request.getParameter("theme");
    if (parameter != null) {
        theme = parameter;
        session.setAttribute("theme",parameter);
    } else if (session.getAttribute("theme") != null) {
        theme = (String)session.getAttribute("theme");
    } else {
        theme = "smoothness";
    }
    pageContext.setAttribute("theme", theme);
    %>
    <link type="text/css" href="css/${theme}/jquery-ui.custom.css" rel="stylesheet" />

    <link rel="stylesheet" href="css/dateAxisV.css" type="text/css"/>
    <link rel="StyleSheet" href="css/styles.css" type="text/css" />
    <link rel="StyleSheet" href="css/autocomplete.css" type="text/css" />
    <link rel="StyleSheet" href="css/layout-default-latest.css" type="text/css" />
    <link rel="StyleSheet" href="css/app-bar-cookie-info.css" type="text/css" />
    
    <!--[if IE ]>
    <link rel="StyleSheet" href="css/ie.css" type="text/css" />
    <![endif]-->
    
    <%-- rss --%>
    ${headerViewObject.searchingRSSChannels}

    <script src="js/jquery-1.5.1.min.js" type="text/javascript" ></script>
    <script src="js/jquery-ui-1.8.11.custom.min.js" language="javascript" type="text/javascript"></script>

    <script  src="js/settings.js" language="javascript" type="text/javascript"></script>
    <script src="js/jquery.mousewheel.js" type="text/javascript" ></script>
    <script src="js/jquery.layout-latest.js" type="text/javascript" ></script>

    <script src="js/jquery-ui-timepicker-addon.js" language="JavaScript" type="text/javascript"></script>
    <script src="js/jquery.cookie.js"  type="text/javascript"></script>


    <script src="js/pageQuery.js" language="JavaScript" type="text/javascript"></script>
    <script src="js/main.js" language="JavaScript" type="text/javascript"></script>
    <script src="js/dateAxis_formatV.js" language="javascript" type="text/javascript"></script>
    <script src="js/dateAxisV.js" language="javascript" type="text/javascript"></script>
    <script  src="js/autocomplete.js" language="javascript" type="text/javascript"></script>

    <script  src="js/jquery.jstree.js" language="javascript" type="text/javascript"></script>

    <script type="text/javascript"  src="js/seadragon-min.js"></script>
    <script type="text/javascript"  src="js/app-bar-cookie-info.js"></script>

    <script  src="js/cmn/cmn.js" language="javascript" type="text/javascript"></script>
    <script  src="js/json2.js" language="javascript" type="text/javascript"></script>


     <c:if test="${gplus.buttonEnabled}">
        <script type="text/javascript" src="https://apis.google.com/js/plusone.js">
        {
            "lang" : '${gplus.locale}',
            "parsetags": "explicit"
        }
        </script>
    </c:if>

    <scrd:loggedusers>    
    	<!--script  src="js/admin/admin.js" language="javascript" type="text/javascript"></script-->
       <script  src="js/admin/roles/roles.js" language="javascript" type="text/javascript"></script>
       <script  src="js/admin/processes/processes.js" language="javascript" type="text/javascript"></script>
       <script  src="js/admin/processes/starter.js" language="javascript" type="text/javascript"></script>
        
       <script  src="js/admin/rights/rights.js" language="javascript" type="text/javascript"></script>
       <script  src="js/admin/rights/right.js" language="javascript" type="text/javascript"></script>

       <script  src="js/admin/statistics/statistics.js" language="javascript" type="text/javascript"></script>

    </scrd:loggedusers>
    
    ${headerViewObject.localizationScripts}
    <title>${title}</title>
    <script language="JavaScript" type="text/javascript">
        
        var searchPage = "./";
        // time axis 
        var fromField = "<%=fromField%>";
        var toField = "<%=toField%>";
        var dateAxisAdditionalParams = "";
        var fromStr = "od";
        var toStr = "do";
        var selectStart = "";
        var selectEnd = "";

        
        // localization
        ${headerViewObject.dictionary}
        // selekce
        ${headerViewObject.levelsModelSelectionArray}
        
        ${headerViewObject.injectSettings}

            // upravuje polozky menu tak aby byly resp. nebyly videt
            // presunout jinam, ale kam?
            function postProcessContextMenu() {
		    
                // polozky, ktere jsou viditelne (neviditelne) jenom kvuli roli
                $(".adminMenuItems").each(function(menuindex, menuelm) {
                    $(menuelm).children("div").each(function(itemidex,itemelm){
				    
                        var roleDiv = $(itemelm).children("div._data_x_role");
                        var uuidDiv = $(itemelm).children("div._data_x_uuid")
                        var levelDiv = $(itemelm).children("div._data_x_level")

                        // role element
                        if ((roleDiv.length == 1) && (roleDiv.text() != '')) {
                            var actionToPerform = roleDiv.text();
                            //var uuid = uuidDiv.text();
                            var level = levelDiv.text();
                            var uuid = $("#tabs_"+level).attr('pid');
					    
                            if (viewerOptions.rights[actionToPerform]) {
                                if (viewerOptions.rights[actionToPerform][uuid]) {
                                    $(itemelm).show();
                                } else if (viewerOptions.rights[actionToPerform]["1"]){
                                    $(itemelm).show();
                                } else {
                                    $(itemelm).hide();
                                }
                            } else {
                                $(itemelm).hide();
                            }
                        }
                    });
                });
            }
            
            var collectionsDict = {
            <c:forEach var="col" items="${cols.virtualCollectionsLocale}">
                    <c:forEach items="${col.descriptions}" var="desc" >
                        "${col.pid}": "${desc.text}",
                    </c:forEach>
            </c:forEach>
                "dummy":""};
    </script>
</head>
