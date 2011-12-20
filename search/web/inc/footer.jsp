<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>


<table class="socialbuttons" align="center">
    <tr>
    <td class="gplus">
     <!-- Umístěte tuto značku na místo, kde chcete zobrazovat tlačítko +1. -->
     <c:if test="${gplus.buttonEnabled}">
        <g:plusone size="small" href="${gplus.shareURL}" annotation="bubble"></g:plusone>
     </c:if>
    </td>

    <td class="fb">
     <c:if test="${fb.buttonEnabled}">
      <!-- like button -->
      <fb:like href="${fb.shareURL}" send="false" width="16"  layout="button_count" show_faces="false"></fb:like>
     </c:if>
    </td>

    <td class="tweet">
     <c:if test="${tweet.buttonEnabled}">
      <!-- twitter -->
      <a href="https://twitter.com/share" class="twitter-share-button" data-url="${tweet.shareURL}" data-lang="${tweet.locale}">Tweet</a>
     </c:if>
    </td>
    </tr>
</table>

<div align="center" >
    ©2008-2011. 
    Developed under GNU GPL by <a href="http://www.incad.cz/">Incad</a>, <a href="http://www.nkp.cz/">NKČR</a>, <a href="http://www.lib.cas.cz/">KNAV</a> and <a href="http://www.mzk.cz/">MZK</a> 
</div>


<c:if test="${param.debug==true}">${url}</c:if>
<div id="test"></div>


<c:if test="${ga.ready}">
<!-- google analytics support -->
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', '${ga.webPropertyId}']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>
</c:if>

<c:if test="${fb.buttonEnabled}">
<!-- facebook support  -->
<div id="fb-root"></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = "//connect.facebook.net/${fb.locale}/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>
</c:if>



<c:if test="${gplus.buttonEnabled}">
<!-- Umístěte tuto žádost o vykreslení na příslušné místo. -->
<script type="text/javascript">
  window.___gcfg = {lang: '${gplus.locale}'};

  (function() {
    var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
    po.src = 'https://apis.google.com/js/plusone.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
  })();
</script>
</c:if>

<c:if test="${tweet.buttonEnabled}">
<!--  twitter -->
<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>
</c:if>
