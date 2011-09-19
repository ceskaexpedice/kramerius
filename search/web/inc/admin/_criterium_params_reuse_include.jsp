<%--
    Only include page - included from _new_right.jsp
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<view:object name="r" clz="cz.incad.Kramerius.views.rights.DisplayRightView"></view:object>


<div>

<div style="border-top: 1px solid gray;">
    <table><tr>        
    
    <td><label for="shortDesc"><strong><view:msg>rights.dialog.criteriumparams.label</view:msg>:</strong></label></td> 
        <td><select size="1" name="usedParams" id="usedParams" onchange="right.paramTabs['edit'].onSelectParams();" >
             <option value=""></option>
             <c:forEach var="p"  items="${r.rightCriteriumParams}"> 
             <option value="${p.id}">${p.shortDescription}</option>
            </c:forEach>                         
         </select></td>

    </tr></table>
</div>


<div style="width: 100%;">
    <table width="100%"><tr>
        <td style="width: 100%;"></td>    
        <td style="border-bottom: 1px solid gray;"><a href="javascript:right.paramTabs['edit'].add();" class="ui-icon ui-icon-plus"></a></td>
    </tr></table>
</div>

<table width="100%" >
<thead><tr>
    <td style="width: 100%;"><strong><view:msg>rights.dialog.criteriumparams.value</view:msg> </strong></td>
    <td style="width: 100px;" colspan="2"><strong><view:msg>rights.dialog.criteriumparams.action</view:msg> </strong></td>
</tr></thead>


<tbody style="border-bottom: 1px solid gray;" id="editParams">
</tbody>

</table>

</div>