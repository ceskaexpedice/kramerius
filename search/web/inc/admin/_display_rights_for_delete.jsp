<%--
    Dialog for  delete rights
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>


<view:object name="selection" clz="cz.incad.Kramerius.views.rights.DisplaySelectionRightsViewObject"></view:object>

<div id="delRights">
<scrd:loggedusers>

    <h3> <view:msg>rights.dialog.blobaldelete.title</view:msg></h3>
    <table style="width: 100%">
            <thead style="border-bottom: 1px dashed; ">
                <tr>
                    <td width="8px"></td>
                    <td width="16px"><strong><view:msg>rights.dialog.table.column.id</view:msg> </strong></td>
                    <td><strong><view:msg>rights.dialog.table.column.object</view:msg> </strong></td>
                    <td><strong><view:msg>rights.dialog.table.column.action</view:msg> </strong></td>
                    <td><strong><view:msg>rights.dialog.table.column.group</view:msg></strong></td>
                    <td title="<view:msg>rights.dialog.table.column.priority</view:msg>"><strong>..</strong></td>
                    <td><strong><view:msg>rights.dialog.table.column.criterium</view:msg></strong></td>
                    <td><strong><view:msg>rights.dialog.table.column.criteriumparams</view:msg></strong></td>
                </tr>
            </thead>
            <tbody>


    <c:forEach var="right" items="${selection.rights}">
        <tr>
            <td><input type="checkbox" checked="checked" value="${right.id}"></input> </td>
            <td><div>${right.id}</div></td>
            <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.title}</div></td>
            <td title="<view:msg>rights.action.${right.action}</view:msg>">${right.action}</td>
            <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.user}</div></td>
           <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.fixedPriority}</div></td> 
           <td title="${right.criteriumWrapper!=null ? right.criteriumWrapper : "cz.incad.kramerius.security.impl.criteria.none"}"><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"> <view:msg>${right.criteriumWrapper!=null ? right.criteriumWrapper : 'cz.incad.kramerius.security.impl.criteria.none'}</view:msg></div></td> 
           <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"> ${right.criteriumWrapper!=null ? right.criteriumWrapper.criteriumParams : '' }</div></td> 
        </tr>
    </c:forEach>
    </table>

</scrd:loggedusers>
</div>
