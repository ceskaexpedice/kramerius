<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<view:object name="ga" clz="cz.incad.Kramerius.views.rights.CollectionActionsView"></view:object>

<scrd:securedContent action="display_admin_menu" sendForbidden="true">

<script type="text/javascript">
      function collectionRightDialog(pid){
          var structs = [
             {
               'models': "monograph",
               'pid':pid
             }
          ];
          // open dialog
          var affectedDialog = findObjectsDialog();
          affectedDialog.actions = null;
          affectedDialog.openDialog(structs);
      }
</script>

<div>
    
    <table  style="width:100%">
        <thead>
            <tr> 
                <td><strong><view:msg>rights.dialog.table.column.collectionlabel</view:msg></strong></td>
                <td><strong><view:msg>rights.dialog.table.column.pid</view:msg></strong></td>
                <td><strong><view:msg>rights.dialog.table.column.change</view:msg></strong></td>
            </tr>
        </thead>
        <tbody>
	            <c:forEach var="ga" items="${ga.wrappers}">
                    <tr>
	                  <td>${ga.label}</td>
	                  <td>${ga.pid}</td>
                      <td><button type="button" onclick="collectionRightDialog('${ga.pid}');"><view:msg>common.edit</view:msg></button></td>
                    </tr>
	            </c:forEach>
        </tbody>
    </table>
</div>

</scrd:securedContent>