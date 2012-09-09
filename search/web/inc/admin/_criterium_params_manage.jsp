<%--
    Dialog for manage rights criteriums 
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"  %>

<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>

<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<style>
<!--
.criteriums-table {
    width:100%;
}    
.criteriums-table thead tr td:last-child {
    width: 150px;
} 
.criteriums-buttons {
    float: right;
}
.criteriums-buttons-clear {
    clear: right;
}
-->
</style>

<view:object name="criteriums" clz="cz.incad.Kramerius.views.rights.CriteriumsManageView"></view:object>
<div>

<div class="criteriums-buttons">
    <a href="javascript:criteriumsSearcher.refresh();" class="ui-icon ui-icon-transferthick-e-w"></a>
</div>
<div class="criteriums-buttons-clear">
</div>


<div id="criteriums-manage-waiting" style="display: none;">
    <span><view:msg>administrator.dialogs.waiting</view:msg></span>
</div>

<div id="criteriums-manage-content">
    <table style="width: 100%">
      <thead>
        <tr>
            <td><strong><view:msg>rights.dialog.table.column.id</view:msg></strong></td> 
            <td><strong><view:msg>rights.dialog.table.column.description</view:msg></strong></td> 
            <td><strong><view:msg>rights.dialog.table.column.object</view:msg></strong></td> 
            <td align="center"><strong><view:msg>common.change</view:msg></strong></td>
            
      </tr>
      </thead>  
      <tbody>  
      <c:forEach items="${criteriums.criteriumParams}" var="cParam" varStatus="status">
            <tr>

                  <td style="width:20px;">${cParam.id}</td>
                  <td>${cParam.shortDescription}</td>   
                  <td style="width: 40%">
                    <c:out value="${cParam.objectsString}"></c:out>
                  </td>
                  <td style="width:35%"> 

                  <button onclick="criteriumsSearcher.renameCriterium(${cParam.id},'${cParam.shortDescription}');"><view:msg>common.rename</view:msg></button> 

                  <c:if test="${fn:length(cParam.usedPids) gt 0}">
                      <button  onclick="criteriumsSearcher.search(${cParam.usingObjectsAsJSArray})"><view:msg>rights.dialog.criteriumparams.findusage</view:msg></button> 
                  </c:if>  
                  <c:if test="${fn:length(cParam.usedPids) eq 0}">
                      <button  disabled="disabled" onclick="criteriumsSearcher.search(${cParam.usingObjectsAsJSArray})"><view:msg>rights.dialog.criteriumparams.findusage</view:msg></button> 
                  </c:if>
                  
                  <c:if test="${fn:length(cParam.usedPids) eq 0}">
                      <button onclick="criteriumsSearcher.deleteCriterium(${cParam.id});"><view:msg>common.delete</view:msg></button> 
                  </c:if>

                  <c:if test="${fn:length(cParam.usedPids) gt 0}">
                      <button disabled="disabled" onclick="criteriumsSearcher.deleteCriterium(${cParam.id});"><view:msg>common.delete</view:msg></button> 
                  </c:if>
                  
                  </td>
            </tr>
      </c:forEach>
      </tbody>
    </table>

</div>

</div>

