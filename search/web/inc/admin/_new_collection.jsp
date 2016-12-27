<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>
<view:object name="ga" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>

<style>
</style>


<div class="newrole">

<div>

     <c:choose>
       <c:when test="${ga.parameterCollection != null}">
	     <input id="vc_pid" name="vc_pid" type="text" value="${ga.parameterCollection.pid}" style="display:none"/>
       </c:when>    
      <c:otherwise>
	     <input id="vc_pid" name="vc_pid" type="text" value="" style="display:none"/>
       </c:otherwise>
     </c:choose>
	

     <table style="width:100%">
     <tr><td style="width:100%"><label for="czech_text"><view:msg>collection.administration.edit.czech_lang</view:msg></label></td></td>
     <c:choose>
       <c:when test="${ga.parameterCollection != null}">
        <tr><td style="width:100%"><input id="czech_text" name="czech" type="text" value="${ga.parameterCollection.descriptionsMap['cs']}" /></td></td>
       </c:when>    
      <c:otherwise>
	     <tr><td style="width:100%"><input id="czech_text" name="czech" type="text" /></td></td>
       </c:otherwise>
     </c:choose>
     

     <tr><td style="width:100%"><label for="english_text"><view:msg>collection.administration.edit.eng_lang</view:msg></label></td></td>
     <c:choose>
       <c:when test="${ga.parameterCollection != null}">
        <tr><td style="width:100%"><input id="english_text" name="english" type="text" value="${ga.parameterCollection.descriptionsMap['en']}" /></td></td>
       </c:when>    
      <c:otherwise>
	     <tr><td style="width:100%"><input id="english_text" name="english" type="text" /></td></td>
       </c:otherwise>
     </c:choose>
     


     <tr><td><label for="canLeave"><view:msg>collection.administration.content.canleave</view:msg></label></td></td>
     <c:choose>
       <c:when test="${ga.parameterCollection != null && ga.parameterCollection.canLeave}">
         <tr><td><input id="canLeave" name="canLeave" type="checkbox" checked/></td></td>
       </c:when>    
      <c:otherwise>
          <tr><td><input id="canLeave" name="canLeave" type="checkbox" /></td></td>
       </c:otherwise>
     </c:choose>
	</table>
</div>

</div>