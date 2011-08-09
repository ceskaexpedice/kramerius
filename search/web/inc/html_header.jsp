<%@ page import="cz.incad.Kramerius.views.HeaderViewObject"%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/viewObjects.tld" prefix="view" %>


<%@ page isELIgnored="false"%>
<%@ page trimDirectiveWhitespaces="true"%>

<%
            String fromField = "f1"; 
            String toField = "f2";
            String fromValue = "";
            String toValue = "";
%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.service.ResourceBundleService"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.Enumeration"%><head>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Cache-Control" content="no-cache" />
    <meta name="description" content="Digitized documents access aplication." />
    <meta name="keywords" content="periodical, monograph, library,  book, publication, kramerius, fedora" />
    <meta name="author" content="INCAD, www.incad.cz" />

    <link rel="icon" href="img/favicon.ico"/>
    <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />

    <link type="text/css" href="css/smoothness/jquery-ui-1.8.11.custom.css" rel="stylesheet" />
    <!--link type="text/css" href="css/ui-lightness/jquery-ui-1.8.11.custom.css" rel="stylesheet" /-->

    <link rel="stylesheet" href="css/dateAxisV.css" type="text/css"/>
    <link rel="StyleSheet" href="css/styles.css" type="text/css" />
    <link rel="StyleSheet" href="css/autocomplete.css" type="text/css" />
    <link rel="StyleSheet" href="css/layout-default-latest.css" type="text/css" />
    
    <!--[if IE ]>
    <link rel="StyleSheet" href="css/ie.css" type="text/css" />
    <![endif]-->


    <script src="js/jquery-1.5.1.min.js" type="text/javascript" ></script>
    <script src="js/jquery-ui-1.8.11.custom.min.js" language="javascript" type="text/javascript"></script>

    <script  src="js/settings.js" language="javascript" type="text/javascript"></script>
    <script src="js/jquery.mousewheel.js" type="text/javascript" ></script>
    <script src="js/jquery.splitter.js" type="text/javascript" ></script>
    <script src="js/jquery.cookie.js" type="text/javascript"></script>
    <script src="js/jquery.layout-latest.js" type="text/javascript" ></script>

    <script src="js/pageQuery.js" language="JavaScript" type="text/javascript"></script>
    <script src="js/main.js" language="JavaScript" type="text/javascript"></script>
    <script src="js/dateAxis_formatV.js" language="javascript" type="text/javascript"></script>
    <script src="js/dateAxisV.js" language="javascript" type="text/javascript"></script>
    <script  src="js/autocomplete.js" language="javascript" type="text/javascript"></script>

    <script type="text/javascript"  src="js/seadragon-min.js"></script>
    <script  src="js/pdf/pdf.js" language="javascript" type="text/javascript"></script>

    <scrd:loggedusers>    
    	<!--script  src="js/admin/admin.js" language="javascript" type="text/javascript"></script-->
	   <script  src="js/rights/adminRights.js" language="javascript" type="text/javascript"></script>
    </scrd:loggedusers>
    
    <!-- header view object -->
    <view:object name="headerViewObject" clz="cz.incad.Kramerius.views.HeaderViewObject"></view:object>
    
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
    </script>
</head>
