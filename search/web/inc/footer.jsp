<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<view:object name="footerViewObject" clz="cz.incad.Kramerius.views.inc.FooterViewObject"></view:object>

<%@ page isELIgnored="false"%>

<div class="nkp" style="display:none;">
    <div>
        <h1><view:msg>common.links</view:msg></h1>
        
    <div>      
        <c:forEach items="${buttons.languageItems}" var="langitm">
        <c:set var="escapedLink" >${fn:replace(langitm.link, quote, escapedquote)}</c:set>
        <a href="${escapedLink}">${langitm.name}</a>
    </c:forEach>
        <!-- Registrace pouze pro neprihlasene -->
        <scrd:notloggedusers>
            <view:kconfig var="showthisbutton" key="search.mainbuttons.showregistrationbutton"></view:kconfig>
            <c:if test="${showthisbutton == 'true'}">
                <a id="registerHref" href="javascript:registerUser.register();"><view:msg>registeruser.menu.title</view:msg></a>
            </c:if>
        </scrd:notloggedusers>

        <!--  show admin menu - only for logged users -->
        <scrd:loggedusers>
            <a id="adminHref" href="javascript:showAdminMenu();"><view:msg>administrator.menu</view:msg></a>
        </scrd:loggedusers>
        
        <!-- login - only for notlogged -->
        <scrd:notloggedusers>
            <a href="redirect.jsp?redirectURL=${searchFormViewObject.requestedAddress}"><view:msg>application.login</view:msg></a>
        </scrd:notloggedusers>
        
        <!-- logout - only for logged -->
        <scrd:loggedusers>
            <c:choose>
                <c:when test="${empty buttons.shibbLogout}">
                            <a href="logout.jsp?redirectURL=${searchFormViewObject.requestedAddress}"><fmt:message bundle="${lctx}">application.logout</fmt:message></a>
                </c:when>
                <c:otherwise>
                            <a href="${buttons.shibbLogout}"><view:msg>application.logout</view:msg></a>
                </c:otherwise>
            </c:choose>
        </scrd:loggedusers>

<a href="javascript:showHelp('<c:out value="${param.language}" />');"><view:msg>application.help</view:msg>
</a>
<c:if test="${rows != 0}" ><a href="."><view:msg>application.home</view:msg></a></c:if>
</div>
        
    </div>
</div>
<div style="width:100%;margin-top:15px;">
<div class="loga"></div>
<div style="float:right;">
    <span style="font-size:1.2em;font-weight: bold;"><view:msg>common.contact</view:msg></span><br/>
    Národní knihovna ČR<br/>Klementinum 190<br/>110 00 Praha 1
</div>
<div class="finance">
Projekt je spolufinancován ze Strukturálních fondů EU (Evropského fondu pro regionální rozvoj) prostřednictvím IOP
</div>
<div id="socialbuttons_div" class="viewer socialbuttons" style="visibility: hidden; height: 0px;">
     <c:if test="${gplus.buttonEnabled || fb.buttonEnabled || tweet.buttonEnabled}">
      <script type="text/javascript">
        // changing og metadata
        $('#socialbuttons_div.viewer').bind('viewReady', function(event, viewerOptions){
        	if(!viewerOptions) return;
            var rbs = new RebuildSocialButtons();
            // rebuild all buttons
            rbs.rebuild(rbs.buildItemsURLS())
        });


        function RebuildSocialButtons() {}

        RebuildSocialButtons.prototype.isItemPage = function() {
            if(viewerOptions) return true;
            return false;
        }

        RebuildSocialButtons.prototype.buildSearchURLS = function() {
            return {
                "url":window.location.href,
                "imgUrl":window.location.href+'/img/logo.png'
            };            
        }

        // gplus button - explicit initialization
        RebuildSocialButtons.prototype.rebuildExplicit=function(urls) {
            $('meta[property="og:url"]').attr('content',urls.url);
            $('meta[property="og:image"]').attr("content", urls.imgUrl);
            $('link[rel="canonical"]').attr("href", urls.url);

        	//Google plus one
            if(typeof(gapi) !== 'undefined') {
                gapi.plusone.go("gplusbutton");
            }
            
        }
        
        RebuildSocialButtons.prototype.buildItemsURLS = function() {
            var pathname = window.location.pathname;
            if (pathname.startsWith("/")) {
                pathname=pathname.substring(1,pathname.length);
            }
            var url = window.location.protocol+"//"+window.location.host+"/"+pathname.split("/")[0]+"/handle/"+encodeURIComponent(viewerOptions.pid);
            var imgUrl = window.location.protocol+"//"+window.location.host+"/"+pathname.split("/")[0]+"/img?uuid="+encodeURIComponent(viewerOptions.pid)+"&stream=IMG_THUMB&action=GETRAW"
            return {
                "url":url,
                "imgUrl":imgUrl
            };            
        }
        
        RebuildSocialButtons.prototype.rebuild = function(urls) {
        	$('meta[property="og:url"]').attr('content',urls.url);
            $('meta[property="og:image"]').attr("content", urls.imgUrl);
            $('link[rel="canonical"]').attr("href", urls.url);
            
            //Facebook like
            if(typeof(FB) !== 'undefined') {
                $('#fbbutton').html('<fb:like href="' + urls.url + '" id="fbbutton_elm" layout="button_count" show_faces="false" width="16" action="like" />');
                FB.XFBML.parse(document.getElementById('fbbutton'), function()  {
                });
            }

            //Google plus one
            if(typeof(gapi) !== 'undefined') {
                gapi.plusone.go("gplusbutton");
            }

            //Twitter tweet button
            if(typeof(twttr) !== 'undefined' && typeof(twttr.widgets) !== 'undefined') {
                $('.twitter-share-button').attr('data-url',urls.url);
                twttr.widgets.load();
            }
        }
        
      </script>
    </c:if>
</div>

<table class="socialbuttons" align="center">
    <tr>
     <c:if test="${gplus.buttonEnabled}">
       <td id="gplusbutton"  class="gplus">
           <div class="g-plusone" data-size="small" data-annotation="bubble"></div>
       </td>
     </c:if>

     <c:if test="${fb.buttonEnabled}">
      <!-- like button -->
      <td id="fbbutton" class="fb">
        <!--  place for fb -->
        <fb:like id="fbbutton_elm" href="${fb.shareURL} "  send="false" width="16"  layout="button_count" show_faces="false"></fb:like>
      </td>
     </c:if>

     <c:if test="${tweet.buttonEnabled}">
      <!-- twitter -->
      <td class="tweet">
        <a href="https://twitter.com/share" class="twitter-share-button" data-lang="${tweet.locale}">Tweet</a>
      </td>
     </c:if>
    
    <td>
    
<div align="center" >
    ©2008-2014.
    Developed under GNU GPL by <a href="http://www.incad.cz/">Incad</a>, <a href="http://www.nkp.cz/">NKČR</a>, <a href="http://www.lib.cas.cz/">KNAV</a> and <a href="http://www.mzk.cz/">MZK</a> 
    (version: ${footerViewObject.version}, revision:<a href="https://github.com/ceskaexpedice/kramerius/commit/${footerViewObject.revision}">  ${fn:substring(footerViewObject.revision, 0, 4)}..</a>)
</div>
    </td>
    
    </tr>
    
</table>



<c:if test="${param.debug==true}">${url}</c:if>
<div id="test"></div>

<c:if test="${ga.ready}">
<%-- Google Analytics. Configuration directives - googleanalytics.webpropertyid=   (For measuring code UA-XXXXXXXXX-1)
     For code GTM-XXXXXX /search/web/inc/html_header.jsp
--%>
   <c:if test="${fn:startsWith(ga.webPropertyId,'UA')}">
      <!-- Global site tag (gtag.js) - Google Analytics -->
      <script async src="https://www.googletagmanager.com/gtag/js?id=${ga.webPropertyId}">
      </script>
      <script>
         window.dataLayer = window.dataLayer || [];
         function gtag(){dataLayer.push(arguments);}
         gtag('js', new Date());
         gtag('config', '${ga.webPropertyId}');
      </script>
   </c:if>


<%-- Google Analytics. For measuring code GTM-XXXXXX tag <noscript> 
   https://developers.google.com/tag-manager/quickstart
--%>
   <c:if test="${fn:startsWith(ga.webPropertyId,'GTM')}">
<!-- Google Analytics <noscript> -->
      <noscript><iframe src="https://www.googletagmanager.com/ns.html?${ga.webPropertyId}"
         height="0" width="0" style="display:none;visibility:hidden"></iframe>
      </noscript>
   </c:if>
</c:if>



<c:if test="${fb.buttonEnabled}">
<!-- facebook support  -->
<div id="fb-root"></div>
<script>


(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = "//connect.facebook.net/${fb.locale}/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>
</c:if>




<c:if test="${tweet.buttonEnabled}">
<!--  twitter -->
<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>
</c:if>
</div>
