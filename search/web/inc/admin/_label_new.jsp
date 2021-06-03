<%--
    Dialog for managing labels
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
    .labels-table {
        width:100%;
    }
    .labels-table thead tr td:last-child {
        width: 150px;
    }
    .labels-buttons {
        float: right;
    }
    .labels-buttons-clear {
        clear: right;
    }
    -->
</style>

<view:object name="label" clz="cz.incad.Kramerius.views.rights.LabelEditView"></view:object>

<table width="100%" class="labels-table">

    <input type="hidden" name="label-id" id="label-id" value="${label.id}" />

    <tr><td width="100%"><label for="label-name"><view:msg>administrator.menu.dialogs.only_newer</view:msg></label></td></tr>
    <tr><td width="100%"><input id="label-name" type="text" value="${label.name}" onkeyup="" style="width:100%" id="label-name"></input></td></tr>
    <tr><td width="100%"><label for="label-description"><view:msg>administrator.menu.dialogs.only_newer</view:msg></label></td></tr>
    <tr><td width="100%"><textarea id="label-description" type="text" style="width:100%" id="label-description">${label.description}</textarea></td></tr>

</table>