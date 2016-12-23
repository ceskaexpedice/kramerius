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
                <td><strong><view:msg>rights.dialog.table.column.collectionlabel</view:msg></strong></td>
                <td><strong><view:msg>rights.dialog.table.column.pid</view:msg></strong></td>
                <td><strong>Can leave</strong></td>
                <td><strong>Preview set</strong></td>
                <td><strong>Image full set</strong></td>
                <td><strong><view:msg>rights.dialog.table.column.change</view:msg></strong></td>
                <td><strong>rights</strong></td>
                <td><strong>Images association</strong></td>
                <td><strong>delete</strong></td>
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
	                  
                      <td><button type="button" onclick="colAdm.collection('${itm.pid}');">rights</button></td>
                      <td><button type="button" onclick="collectionRightDialog('${itm.pid}');"><view:msg>common.edit</view:msg></button></td>
                      <td><button type="button" onclick="colAdm.imgassoc('${itm.pid}');">Nahledy</button></td>
                      <td><button type="button" onclick="colAdm.delete('${itm.pid}');"><view:msg>common.delete</view:msg></button></td>
                    </tr>
	            </c:forEach>
        </tbody>
    </table>
    </div>
    
</div>

</scrd:securedContent>