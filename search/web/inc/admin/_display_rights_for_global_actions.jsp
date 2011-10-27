<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<style>
.buttons span.ui-button-text {
    padding: 3px;
}
</style>


<script type="text/javascript">
   $(document).ready(function() {
       $(".buttons .button").button();
       $(".buttons .disabled").each(function(i,val) {
           $(val).attr('disabled', 'disabled' ).addClass('ui-state-disabled');
       });            
    });
</script>

<view:object name="rights"
 clz="cz.incad.Kramerius.views.rights.DisplayRightsForObjectsView"></view:object>

<table style="width: 100%">
 <c:forEach var="rightsPath" items="${rights.rightsPath}"
  varStatus="rstatus">

  <strong> <view:msg>rights.action.${rights.securedAction}.formalName</view:msg>
  - <c:out
   value="${rightsPath.titleForPath} (${rightsPath.models[rightsPath.path.leaf]})"></c:out></strong>

  <tr>
   <td colspan="2">

   <div
    id="${rightsPath.rowId}_${rstatus.index}_${rights.securedAction}"
    style="border: 1px solid gray;">

   <table width="100%">
    <tr>
     <td width="100%"></td>
     <td>
     <div class="buttons"><span class="button"> <a
      href="javascript:findObjectsDialog().securedActionTabs['${rights.securedAction}'].newRightForPath('${rightsPath.path}');"
      class="ui-icon ui-icon-plus"> </a> </span></div>
     </td>
    </tr>
   </table>


   <table width="100%">
    <tr>
     <td valign="top">
     <div></div>
     </td>
     <td>
     <div>
     <table width="100%" style="table-layout: fixed;">
      <thead
       style="border-bottom: 1px dashed; background-image: url('img/bg_rights_table.png'); background-repeat: repeat-x; height: 28px;">
       <tr>
        <td width="20px" style="border-top: 1px solid black;"></td>
        <td width="20%" align="center"
         style="border-top: 1px solid black;"><strong><view:msg>rights.dialog.table.column.object</view:msg></strong></td>
        <td width="60px" align="center"
         style="border-top: 1px solid black;"><strong><view:msg>rights.dialog.table.column.action</view:msg></strong></td>
        <td width="130px" align="center"
         style="border-top: 1px solid black;"><strong><view:msg>rights.dialog.table.column.user</view:msg></strong></td>
        <td title="Priorita" width="15px" align="center"
         style="border-top: 1px solid black;"><strong>..</strong></td>
        <td width="140px" align="center"
         style="border-top: 1px solid black;"><strong><view:msg>rights.dialog.table.column.criterium</view:msg></strong></td>
        <td align="center" style="border-top: 1px solid black;"><strong><view:msg>rights.dialog.table.column.criteriumparams</view:msg></strong></td>
        <td width="60px" align="center"
         style="border-top: 1px solid black;"><strong><view:msg>rights.dialog.table.column.change</view:msg></strong></td>
       </tr>
      </thead>
      <tbody>
       <c:forEach items="${rightsPath.rights}" var="right"
        varStatus="status">
        <tr>
         <td title="${status.index}">
         <div
          style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${status.index}</div>
         </td>
         <td title="${right.title}">
         <div
          style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.title}</div>
         </td>
         <td title="<view:msg>rights.action.${right.action}</view:msg>">
         <div
          style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"><view:msg>rights.action.${right.action}.formalName</view:msg></div>
         </td>
         <td>
         <div
          style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.user}</div>
         </td>
         <td title="${right.fixedPriority}">
         <div
          style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.fixedPriority}</div>
         </td>
         <td
          title="${right.criteriumWrapper!=null ? right.criteriumWrapper : "cz.incad.kramerius.security.impl.criteria.none"}">
         <div
          style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
         <view:msg>${right.criteriumWrapper!=null ? right.criteriumWrapper : 'cz.incad.kramerius.security.impl.criteria.none'}</view:msg></div>
         </td>
         <td
          title="${right.criteriumWrapper!=null ? right.criteriumWrapper.criteriumParams : '' }">
         <div
          style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
         ${right.criteriumWrapper!=null ?
         right.criteriumWrapper.criteriumParams : '' }</div>
         </td>
         <td>
         <table>

          <tr>
           <td><c:choose>
            <c:when test="${right.editable}">

             <div class="buttons"><span class="button"> <a
              title="Remove"
              href="javascript:findObjectsDialog().securedActionTabs['${rights.securedAction}'].deleteRightForPath(${right.id},'${rightsPath.path}');"
              class="ui-icon ui-icon-minus"> </a> </span></div>

            </c:when>
            <c:otherwise>
             <div class="buttons"><span class="button disabled">
             <span class="ui-icon ui-icon-minus "></span> </span></div>
            </c:otherwise>
           </c:choose></td>

           <td><c:choose>
            <c:when test="${right.editable}">

             <div class="buttons"><span class="button"> <a
              title="Edit"
              href="javascript:findObjectsDialog().securedActionTabs['${rights.securedAction}'].editRightForPath(${right.id},'${rightsPath.path}');"
              class="ui-icon ui-icon-wrench"> </a> </span></div>
            </c:when>

            <c:otherwise>
             <div class="buttons"><span class="button disabled">
             <span class="ui-icon ui-icon-wrench"></span> </span></div>

            </c:otherwise>
           </c:choose></td>
          </tr>


         </table>
         </td>
        </tr>
       </c:forEach>
      </tbody>
     </table>
     </div>
     </td>
    </tr>

   </table>
   </div>
   </td>
  </tr>


 </c:forEach>
 </tbody>

</table>

