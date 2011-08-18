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

    <script type="text/javascript">
              
    </script>
    
    <%-- zalozky --%>
    <ul>
        <li><a title="<view:msg>rights.action.read</view:msg>" href="#rightsAffectedObject_selected">Vybrane objekty</a></li>
        <c:forEach var="act" items="${objectsView.actions}">
            <li><a title="<view:msg>rights.action.${act.formalName}</view:msg>" href="#rightsAffectedObject_${act.formalName}" data-action="${act.formalName}">${act.formalName}</a></li>
        </c:forEach>
    </ul>
    
    <div id="rightsAffectedObject_selected">
	    <table style="width: 100%;" class="ui-dialog-content ui-widget-content">
	        <thead style="border-bottom: 1px dashed; background-image: url('img/bg_processheader.png'); background-repeat: repeat-x;">
	            <tr>
	                <td width="8px"></td>
	                <td><strong>Jmeno</strong></td>
	                <td><strong>Popis</strong></td>
	            </tr>
	        </thead>
	        <tbody>
	            <c:forEach items="${objectsView.affectedObjects}" var="object" varStatus="status">
	                <tr id="${object.pid}" class="${(status.index mod 2 == 0) ? 'result r0': 'result r1'}">
                       <td><span class="ui-icon ui-icon-triangle-1-e folder">folder</span>
                       </td>
	                   <td>
	                        <c:choose>
	                            <c:when test="${object.accessed}">
	                              <input id="_check_${object.pid}" type="checkbox" checked="checked" ></input>
	                            </c:when>
	                            <c:otherwise>
	                               <input id="_check_${object.pid}" type="checkbox" ></input>
	                            </c:otherwise>
	                        </c:choose>
	                   </td>
	                   <td id="_title_${object.pid}">${object.title}</td>
	                   <td id="_comment_${object.pid}">${object.comment}</td>
	               </tr>
	            </c:forEach>
	        </tbody>
	    </table>
    </div>

    <%-- Obsahy zalozek --%>
    <c:forEach var="act" items="${objectsView.actions}">
    <div id="rightsAffectedObject_${act.formalName}" data-action="${act.formalName}">
        <span>Nahravam obsah.. vyckejte prosim..</span>
    </div>
    </c:forEach>

</div>
</scrd:loggedusers>
</div>