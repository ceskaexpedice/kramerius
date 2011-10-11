<%--
    Dialog for rights - display objects that can be managed
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>


<view:object name="objectsView" clz="cz.incad.Kramerius.views.rights.DisplayObjectsView"></view:object>

<div id="rightsAffectedObjects">

<scrd:loggedusers>


<div id="rightsAffectedObject_tabs">

    
    <%-- zalozky --%>
    <ul>
        <li><a title="<view:msg>rights.action.read</view:msg>" href="#rightsAffectedObject_selected"><view:msg>rights.dialog.selectedobjects</view:msg></a></li>
        <c:forEach var="act" items="${objectsView.actions}">
            <li><a title="<view:msg>rights.action.${act.formalName}</view:msg>" href="#rightsAffectedObject_${act.formalName}" data-action="${act.formalName}">${act.formalName}</a></li>
        </c:forEach>
    </ul>
    
    <div id="rightsAffectedObject_selected">
	    <table style="width: 100%;" class="ui-dialog-content ui-widget-content">
	        <thead style="border-bottom: 1px dashed; background-image: url('img/bg_processheader.png'); background-repeat: repeat-x;">
	            <tr>
	                <td width="8px"></td>
	                <td><strong><view:msg>rights.dialog.table.column.name</view:msg></strong></td>
                    <td><strong><view:msg>rights.dialog.table.column.model</view:msg></strong></td>
                    <td><strong><view:msg>rights.dialog.table.column.pid</view:msg></strong></td>
	            </tr>
	        </thead>
	        <tbody>
	            <c:forEach items="${objectsView.affectedObjects}" var="object" varStatus="status">
	                <tr id="${object.pid}" class="${(status.index mod 2 == 0) ? 'result r0': 'result r1'}">
	                   <td>
	                        <c:choose>
	                            <c:when test="${object.accessed}">
	                              <input id="_check_${object.pid}" type="checkbox" checked="checked"  onchange="affectedObjectsRights.onChange('${object.pid}');" value="${object.pid}"></input>
	                            </c:when>
	                            <c:otherwise>
	                               <input id="_check_${object.pid}" type="checkbox" onchange="affectedObjectsRights.onChange('${object.pid}');" value="${object.pid}"></input>
	                            </c:otherwise>
	                        </c:choose>
	                        
	                   </td>
	                   <td id="_title_${object.pid}">${object.title}</td>
	                   <td id="_comment_${object.pid}">${object.modelName}</td>
                       <td id="_pid_${object.pid}">${object.pid}</td>
	               </tr>
	            </c:forEach>
	        </tbody>
	    </table>
    </div>

    <%-- Obsahy zalozek --%>
    <c:forEach var="act" items="${objectsView.actions}">
    <div id="rightsAffectedObject_${act.formalName}" data-action="${act.formalName}">
        <span><view:msg>administrator.dialogs.waiting</view:msg></span>
        
    </div>
    </c:forEach>

</div>
</scrd:loggedusers>
</div>