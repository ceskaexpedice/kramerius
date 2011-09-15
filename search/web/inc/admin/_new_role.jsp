<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>
<view:object name="rolesView"
	clz="cz.incad.Kramerius.views.rights.RolesView"></view:object>

<style>
<!--
.newrole {
    width: 100%;
}

.newrole div {
    padding: 10px;
}
.newrole div label {
    display: block;
}
.newrole div input {
    width: 100%;
}

.newrole div select {
    width: 100%;
}

-->
</style>


<div class="newrole">


<div>
<label for="rolename"><view:msg>role.dialog.rolename</view:msg></label>
<input id="newRoleName" name="rolename" type="text" />
</div>


<div>
<label for="personalAdminRole"><view:msg>role.dialog.roleadm</view:msg></label>
<select id="newRolePersonalAdminId" name="personalAdminRole">
	<c:forEach var="role" items="${rolesView.rolesWithNull}">
		<option value="${role.id}">${role.name}</option>
	</c:forEach>
</select>
</div>

</div>