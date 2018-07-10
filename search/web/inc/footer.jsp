<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<view:object name="footerViewObject" clz="cz.incad.Kramerius.views.inc.FooterViewObject"></view:object>

<%@ page isELIgnored="false"%>


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
    ©2008-2012.
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
