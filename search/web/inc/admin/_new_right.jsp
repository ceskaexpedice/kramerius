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

<view:object name="newRight" clz="cz.incad.Kramerius.views.rights.DisplayRightView"></view:object>


<scrd:loggedusers>
<div id="rightDialogContent"> 

    <div>
        <p> 
        
            <c:if test="${newRight.action.name=='create'}">
                <view:msg>rights.dialog.newright.title</view:msg> <strong> <view:msg>rights.action.${newRight.securedAction}.formalName</view:msg></strong> - <i><view:msg>rights.action.${newRight.securedAction}</view:msg></i>
            </c:if>
            <c:if test="${newRight.action.name =='edit'}">
                <view:msg>rights.dialog.editright.title</view:msg> <strong> <view:msg>rights.action.${newRight.securedAction}.formalName</view:msg> </strong> - <i><view:msg>rights.action.${newRight.securedAction}</view:msg></i>
             </c:if>
            
            <!--  na vybrane objekty-->
        </p>
    </div>
    
    
    <script type="text/javascript">
    
    	rightContainer = {
                affectedObjects: [
                    <c:forEach var="pid" items="${newRight.pidsParams}" varStatus="st">${st.index > 0 ? "," :""} "${pid}"  </c:forEach>
                ],    
                data: {
                    //action:'${newRight.action}',
                    ident:'${newRight.rightIdParam}',
                    securedAction:'${newRight.securedAction}',
                    justcreated:${newRight.justCreated},
                    condition:'${newRight.criterium}',
                    param: {
                        ident:${newRight.critparamsid},
                        shortDesc:'${newRight.critparamdesc}',
                        objects:[
                              <c:forEach var="p" items="${newRight.critparams}" varStatus="status">
                                 ${status.index > 0 ? "," :""} "${p}"   
                              </c:forEach>
                         ]
                    },
                    priority:${newRight.priority}, 
                    role:'${newRight.appliedRole}'
                },
                options: {
                    roles:[
                     <c:forEach var="role" items="${newRight.roles}" varStatus="status">
                        ${status.index > 0 ? "," :""} "${role}"   
                     </c:forEach>],
                     criteriums: {
                             <c:forEach var="criterium" items="${newRight.criteriums}" varStatus="status">
                             ${status.index > 0 ? "," :""}      "${criterium.rightCriterium.QName}": { paramsNecessary: ${criterium.rightCriterium.paramsNecessary} }
                             </c:forEach>
                     },
                     params:[
                                                  
                         <c:forEach var="criterium" items="${newRight.rightCriteriumParams}" varStatus="status">
                         ${status.index > 0 ? "," :""}    
                         {
                             ident: ${criterium.id},
                             shortDesc: '${criterium.shortDescription}',
                             objects:[ <c:forEach var="obj" items="${criterium.objects}" varStatus="st">${st.index > 0 ? "," :""} '${obj}'  </c:forEach>]
                         }
                          </c:forEach>
                     ]
                }               
            };

    	   
        var right = new Right();
        right.initUI(rightContainer);

        
    </script>
    
    <form method="post" id="modifyRight">
        <fieldset style="border-width: 0px; padding: 0px;">


            <hr>
            <table>
            <tbody><tr><td> 
                <h3><label for="abstractUser"><view:msg>rights.dialog.table.column.user</view:msg></label></h3></td></tr>
                <tr><td>
        
                    <span id="roleTypeSpan">
                        <input type="radio"  onclick="right.onUserRadioChange();" value="role" name="abstractUser" id="roleType"><view:msg>rights.dialog.table.column.group</view:msg> 
                        <img src="img/rights-group.png">
                    </span>
    
    
                    <span id="allTypeSpan">
                        <input type="radio" onclick="right.onUserRadioChange();" checked="checked" value="all" name="abstractUser" id="allType"><view:msg>rights.dialog.table.column.common_users</view:msg> 
                        <img src="img/rights-person.png">
                        <img src="img/rights-group.png">
                    </span>
                    
                </td>
                </tr>
            
    
            <tr><td> 
                <div style="display:none;" id="userIdDiv">
                        <input type="text" onkeyup="right.onRolesKeyUp(this);" value="common_users" style="width: 100%;display: none; " size="10" name="userName" id="userId">
                         <select id="rolecombo" style="width:100%">
                         <c:forEach var="role" items="${newRight.roles}" varStatus="status">
                             <c:if test="${role != 'common_users'}">
                                 <option value="${role}" ${newRight.appliedRole eq role ? 'selected="selected"' : ''}>${role}</option>
                             </c:if>
                             
                         </c:forEach>
                         </select>
                </div>
                <script type="text/javascript">
                    $("#rolecombo").change(bind(function() {
                        var val = $("#rolecombo").val();
                        this.roleSelection(val);
                    },right));
                </script>
                <!-- 
                <div style="border-top:1px dashed black;margin-top: 5px; display: none;" id="userautocomplete"> </div>
                 -->
             </td></tr>
    
            </tbody></table>
    
            <hr>
            <table width="100%">
                <tbody><tr><td> <h3><label for="criterium"><view:msg>rights.dialog.criterium</view:msg></label></h3></td></tr>
                    <tr><td>
                            <div id="criteriumDiv">
                                <select size="1" name="criterium" id="criterium" onchange="right.onCriteriumChange();">
                                <option value=""></option>
                                <c:forEach var="criteriumWrapper"  items="${newRight.criteriums}"> 
                                <option value="${criteriumWrapper.rightCriterium.QName}"><view:msg>${criteriumWrapper.rightCriterium.QName}</view:msg></option>
                                </c:forEach>
                              </select>
                            </div>
                </td></tr>
    
    
                 <tr><td>   
                        <div style="display: none;" id="rightParamsCreation">
                           <div id="tabs">
                               <ul>
                                   <li><a href="#create" data-key="create"><input name="tab" id="create-check" type="checkbox"></input><span>Vytvorit nove parametry</span></a></li>
                                   <li><a href="#edit" data-key="edit"><input name="tab" id="edit-check" type="checkbox"></input><span>Pouzit jiz drive vytvorene parametry</span></a></li>
                               </ul>
                               <div id="create" data-key="create">
                                    <jsp:include page="_criterium_params_create_include.jsp"/>        
                               </div>
                               <div id="edit" data-key="edit">
                                    <jsp:include page="_criterium_params_reuse_include.jsp"/>        
                               </div>
                           </div>
                        </div>
                        
                </td></tr>
                
            </tbody></table>
            <hr>
    
            <table width="100%">
                <tbody><tr><td> <h3><label for="priority"><view:msg>rights.dialog.priority</view:msg></label></h3><view:msg>rights.dialog.prioritynote</view:msg></td></tr>
                <tr><td> <input type="text" style="width: 100%;" size="10" onkeyup="right.onPriorityChange();" name="priority" id="priority" value="${newRight.priority==0 ? '' : newRight.priority}"></input></td></tr>
            </tbody></table>
            
        </fieldset>
        </form>
</div>

</scrd:loggedusers>
