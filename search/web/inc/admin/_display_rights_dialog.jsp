<%--
    Dialog for rights - display objects that can be managed
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>

<view:object name="rights" clz="cz.incad.Kramerius.views.rights.DisplayRightsForObjectsView"></view:object>

<style>
<!--

.rightsTableContent-tr-detail {
    display: hidden;
    height: 0px;
}



.buttonTD{
    text-align:right;
    height:30px;
}
.line {
    /*
    height:30px;
    width:1px;
    background-color:black;
    */
}
-->
</style>


<div id="rightsTableContent"><scrd:loggedusers>	
        <div style="border-bottom: 1px solid gray;">
			<table>
			    <tr>
			        <td width="100%"></td>
			        <td>
			          <a href="javascript:affectedObjectsRights.securedActionTabs['${rights.securedAction}'].newRight();" style="background:url('img/add.png') no-repeat scroll; border-width:0px; display:block; height:18px; width:18px;">
			           </a>
			       </td>
			       <td><a href="javascript:affectedObjectsRights.securedActionTabs['${rights.securedAction}'].retrieve();" style="background:url('img/refresh.png') no-repeat scroll; border-width:0px; display:block; height:18px; width:18px;" >
			        </a></td>    
			    </tr>
			</table>    
        </div>
        
        <table style="width: 100%">
        <tbody>    
	        <c:forEach var="rightsPath" items="${rights.rightsPath}" varStatus="rstatus">
                <tr>
                    <td><a href="javascript:affectedObjectsRights.displayDetails('${rightsPath.rowId}_${rstatus.index}_${rights.securedAction}');"><span id="${rightsPath.rowId}_${rstatus.index}_${rights.securedAction}_icon" class="ui-icon ui-icon-triangle-1-e folder">folder</span></a></td>
                    <td>
                         <div>Pravo pro objekt <strong><c:out value="${rightsPath.titleForPath} (${rightsPath.models[rightsPath.path.leaf]})"></c:out></strong></div>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">

                        <div id="${rightsPath.rowId}_${rstatus.index}_${rights.securedAction}" style="display: none; border: 1px solid gray;">

				            <table>
				                <tr>
				                    <td width="100%"></td>
				                    <td>
				                      <a href="javascript:affectedObjectsRights.securedActionTabs['${rights.securedAction}'].newRightForPath('${rightsPath.path}');" style="background:url('img/add.png') no-repeat scroll; border-width:0px; display:block; height:18px; width:18px;">
				                       </a>
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
					                                <table width="100%" style="table-layout:fixed;  ">
					                                   <thead style="border-bottom: 1px dashed; background-image: url('img/bg_rights_table.png'); background-repeat: repeat-x; height: 28px;">
					                                       <tr>
					                                           <td width="6px" style="border-top: 1px solid black;"></td>
                                                               <td width="134px" align="center" style="border-top: 1px solid black;"><strong>Objekt</strong></td>
                                                               <td width="100px" align="center" style="border-top: 1px solid black;"><strong>Akce</strong></td>
                                                               <td width="130px" align="center" style="border-top: 1px solid black;"><strong>Uzivatelska role</strong></td>
                                                               <td width="10px" align="center" style="border-top: 1px solid black;"><strong>Priorita</strong></td>
                                                               <td width="100px" align="center" style="border-top: 1px solid black;"><strong>Kriterium</strong></td>
                                                               <td align="center" style="border-top: 1px solid black;"><strong>Parametry kriteria</strong></td>
                                                               <td width="60px" align="center" style="border-top: 1px solid black;"><strong>Zmena</strong></td>
					                                       </tr>
					                                   </thead>
					                                    <tbody>
					                                          <c:forEach items="${rightsPath.rights}" var="right" varStatus="status">
					                                               <tr> 
					                                                    <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${status.index}</div></td> 
                                                                        <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.title}</div></td> 
					                                                    <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.action}</div></td> 
                                                                        <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.user}</div></td> 
                                                                        <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.fixedPriority}</div></td> 
					                                                    <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${right.criteriumWrapper!=null ? right.criteriumWrapper : ''}</div></td> 
                                                                        <td><div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"> ${right.criteriumWrapper!=null ? right.criteriumWrapper.criteriumParams : '' }</div></td> 
                                                                        <td>
                                                                          <table>

                                                                          <tr><td>
													                      <a title="Remove" href="javascript:affectedObjectsRights.securedActionTabs['${rights.securedAction}'].deleteRightForPath(${right.id},'${rightsPath.path}');" style="background:url('img/minus.png') no-repeat scroll; border-width:0px; display:block; height:18px; width:18px;">
                                                                           </a>
													                      </td>

													                      <td>
													                      <a title="Edit" href="javascript:affectedObjectsRights.securedActionTabs['${rights.securedAction}'].editRightForPath(${right.id},'${rightsPath.path}');" style="background:url('img/edit.png') no-repeat scroll; border-width:0px; display:block; height:18px; width:18px;">
													                       </a>
													                       </td></tr>

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



</scrd:loggedusers></div>
