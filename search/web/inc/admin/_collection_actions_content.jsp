<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<view:object name="ga" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
<scrd:securedContent action="display_admin_menu" sendForbidden="true">

<div id="collections-content">
    
    <table  style="width:100%">
        <thead>
            <tr> 
                <td><strong><view:msg>collection.administration.content.collectionlabel</view:msg></strong></td>
                <td width="25%"><strong><view:msg>common.pid</view:msg></strong></td>
                <td width="2%"><strong><view:msg>collection.administration.content.canleave</view:msg></strong></td>
                <td width="2%"><strong><view:msg>collection.administration.content.thumbset</view:msg></strong></td>
                <td width="2%"><strong><view:msg>collection.administration.content.fullset</view:msg></strong></td>
                <td width="10%"><strong><view:msg>collection.administration.content.change</view:msg></strong></td>
                <td width="10%"><strong><view:msg>collection.administration.content.rights</view:msg></strong></td>
                <td width="20%"><strong><view:msg>collection.administration.content.imgassoc</view:msg></strong></td>
                <td width="10%"><strong><view:msg>collection.administration.content.delete</view:msg></strong></td>
             </tr>
        </thead>
        <tbody>
	            <c:forEach var="itm" items="${ga.virtualCollectionsFromFedora}">
                    <tr>
	                  <td>${itm.descriptionsMap[ga.localeLang]}</td>
	                  <td>${itm.pid}</td>
	                  <td> 
       <c:choose>
       <c:when test="${itm.canLeave}">
		<input disabled  type="checkbox" checked/>
       </c:when>    
      <c:otherwise>
		<input disabled  type="checkbox"/>
       </c:otherwise>
     </c:choose>
          
	                  </td>

      <td> 
       <c:choose>
       <c:when test="${itm.thumbnailAvailable}">
		<input disabled  type="checkbox" checked/>
       </c:when>    
      <c:otherwise>
		<input disabled  type="checkbox"/>
       </c:otherwise>
     </c:choose>
      
      </td>
	                  
      <td> 
       <c:choose>
       <c:when test="${itm.fullAvailable}">
		<input disabled  type="checkbox" checked/>
       </c:when>    
      <c:otherwise>
		<input disabled  type="checkbox"/>
       </c:otherwise>
     </c:choose>
	                  
	                  </td>
	                  
                      <td><button type="button" onclick="colAdm.collection('${itm.pid}');"><view:msg>common.edit</view:msg></button></td>
                      <td><button type="button" onclick="collectionRightDialog('${itm.pid}');"><view:msg>common.edit</view:msg></button></td>
                      <td><button type="button" onclick="colAdm.imgassoc('${itm.pid}');"><view:msg>collection.administration.content.imgassoc.button</view:msg></button></td>
                      <td><button type="button" onclick="colAdm.delete('${itm.pid}');"><view:msg>common.delete</view:msg></button></td>
                    </tr>
	            </c:forEach>
        </tbody>
    </table>
    </div>
    
</div>

</scrd:securedContent>