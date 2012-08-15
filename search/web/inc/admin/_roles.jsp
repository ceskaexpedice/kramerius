<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<style>
<!--
.roles-table {
    width: 100%;
}

.roles-table thead tr td:last-child {
    width: 150px;
}

.roles-buttons {
    float: right;
}

.roles-buttons-clear {
    clear: right;
}
-->
</style>


<view:object name="rolesView"
 clz="cz.incad.Kramerius.views.rights.RolesView"></view:object>


<scrd:securedContent action="display_admin_menu" sendForbidden="true">

<div>
<div class="roles-buttons"><a href="javascript:roles.refresh();"
 class="ui-icon ui-icon-transferthick-e-w"></a></div>
<div class="roles-buttons"><a href="javascript:roles.newRole();"
 class="ui-icon ui-icon-plusthick"></a></div>
<div class="roles-buttons-clear"></div>

<div id="roles-waiting" style="display: none;"><span><view:msg>administrator.dialogs.waiting</view:msg></span>
</div>


<div id="roles-content">
<table class="roles-table">
 <thead>
  <tr>
   <td><strong><view:msg>rights.dialog.table.column.group</view:msg></strong></td>
   <td><strong><view:msg>rights.dialog.table.column.admrole</view:msg></strong></td>
   <td><strong><view:msg>rights.dialog.table.column.change</view:msg></strong></td>
  </tr>
 </thead>
 <tbody>
  <c:forEach items="${rolesView.roles}" var="role" varStatus="status">
   <tr>
    <td>${role.name}</td>
    <td>${role.roleAdministrator != null ?
    role.roleAdministrator.name : '-none-' }</td>
    <td>
    <button onclick="roles.editRole('${role.name}');"><view:msg>rights.dialog.buttons.edit</view:msg></button>
    <button onclick="roles.deleteRole('${role.name}');"
     ${role.canbedeleted ? "":"disabled='disabled'" }><view:msg>rights.dialog.buttons.delete</view:msg></button>
    </td>
   </tr>
  </c:forEach>
 </tbody>
</table>

</div>
</div>
</scrd:securedContent>
