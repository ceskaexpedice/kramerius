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

    <div style="width: 100%; height: 100%;">
        <div id="changePswd_form">
        <table style="width: 100%;">
            <tbody>    

                <tr> <td>
                <label for="oldpswd"><view:msg>rights.changepswd.oldpswd</view:msg></label>
                </td></tr>
                <tr> <td>
                <input type="password" style="width: 100%;" id="oldpswd">

                <tr> <td>
                <label for="pswd"><view:msg>rights.changepswd.newpswd</view:msg></label>
                </td></tr>
                <tr> <td>
                <input type="password" style="width: 100%;" id="pswd">
                </td></tr>
                <tr> <td>
                <label for="pswdRepeat"><view:msg>rights.changepswd.repeatnewpswd</view:msg></label>
                </td></tr>
                <tr> <td>
                <input type="password" style="width: 100%;" id="pswdRepeat">
                </td></tr>
            </tbody>
        </table>
        </div>
        <div id="changePswd_wait" style="margin: 16px; font-family: sans-serif; font-size: 10px; display: none;">
            <table style='width:100%'>
                <tbody><tr><td align="center"><img src="img/loading.gif" height="16px" width="16px"></td></tr>
                <tr><td align="center" id="common_started_text"><view:msg>rights.changepswd.changedpswdwait</view:msg> </td></tr>
                </tbody>
            </table>
        </div>

        <div id="checkPswdStatus">
        </div>
    </div>