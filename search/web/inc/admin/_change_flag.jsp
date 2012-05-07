
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<div id="changeFlag">


<scrd:securedContent action="display_admin_menu" sendForbidden="true">

    <h3><view:msg>administrator.dialogs.changevisibility.combo</view:msg></h3>
    <input type="radio" value="setpublic" name="flag" checked="checked"><view:msg>administrator.dialogs.changevisibility.public</view:msg></input>
    <input type="radio" value="setprivate" name="flag"><view:msg>administrator.dialogs.changevisibility.private</view:msg></input>

</scrd:securedContent>

</div>