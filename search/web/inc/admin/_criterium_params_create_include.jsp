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


<div>

<div style="border-top: 1px solid gray;">
    <table><tr>        
    
    <td><label for="shortDesc"><strong><view:msg>rights.dialog.criteriumparams.label</view:msg>:</strong></label></td> 
    
    <td><span id="createParamsLabel">Nepojmenovano</span>
    <input id="createParamsInput" 
            type="text" style="display: none;" name="shortDesc" id="shortDesc">
            </td>
    <td id="createParamsPen"><a href="javascript:right.paramTabs['create'].editLabel();" class="ui-icon ui-icon-pencil"></a></td>
    <!--  
    <td id="createParamsSave" style="display: none;"><a href="javascript:right.paramTabs['create'].editedLabel();" class="ui-icon ui-icon-disk"></a></td>
    -->
    </tr></table>
</div>


<div style="width: 100%;">
    <table width="100%"><tr>
        <td style="width: 100%;"></td>    
        <td style="border-bottom: 1px solid gray;"><a href="javascript:right.paramTabs['create'].add();" class="ui-icon ui-icon-plus"></a></td>
    </tr></table>
</div>

<table width="100%" >
<thead><tr>
    <td style="width: 100%;"><strong><view:msg>rights.dialog.criteriumparams.value</view:msg> </strong></td>
    <td style="width: 100px;" colspan="2"><strong><view:msg>rights.dialog.criteriumparams.action</view:msg> </strong></td>

</tr></thead>

<tbody style="border-bottom: 1px solid gray;" id="createParams">
</tbody>

</table>

</div>