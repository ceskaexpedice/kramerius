<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="cz.incad.kramerius.MostDesirable"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html
 prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# 
                  book: http://ogp.me/ns/book#">

<%@ include file="inc/html_header.jsp"%>
<body>

<script type="text/javascript">
var activationDialog=null;

       function dialogHeight() {
           if (jQuery.browser.msie) {
               return 250;
           } else {
               return 170;             
           }
       }

    
        function openActivationDialog() {
            if (activationDialog) {
            	activationDialog("open");
            } else {
            	activationDialog =  $("#activationinfo").dialog({
            		                       modal: true,
            		                       draggable:false,
                                           height:dialogHeight(),
                                           width:300,

                                         resizable:false,
                                         buttons: {
                                             "Close": function() {
                                                 var url = "search.jsp";    
                                                 $(location).attr('href',url);
                                             } 
                                         },
                                                                      
                                         close: function(event, ui) { 
                                             
                                             var url = "search.jsp";    
                                             $(location).attr('href',url);
                                         },
                                         title:""
                });
            }
        }
    
        $(document).ready(function(){
        	openActivationDialog();
        });
    </script>


<!-- activation info -->
<div id="activationinfo" style="text-align: center; display: none;" >
    <div style="height: 6em; width:22em; display: table-cell; vertical-align: middle;  "><strong><view:msg>registeruser.activated</view:msg></strong></div>
</div>

</body>
</html>