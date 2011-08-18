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

<view:object name="newRight" clz="cz.incad.Kramerius.views.rights.DisplayNewRightView"></view:object>


<scrd:loggedusers>
<div id="rightDialogContent"> 

    <div>
        <p style="">
            Aplikovat pravo <c:out value="${newRight.securedAction}" ></c:out>            
            na vybrane objekty
        </p>
    </div>
    
    
    <script type="text/javascript">

    rightContainer = {
   		affectedObjects: [
            <c:forEach var="pid" items="${newRight.pidsParams}" varStatus="st">${st.index > 0 ? "," :""} '${pid}'  </c:forEach>
        ],    
	    data: {
		    ident:'0',
		    securedAction:'${newRight.securedAction}',
            justcreated:true,
            condition:'',
            param: {
                ident:0,
                shortDesc:'',
                objects:[]
            },
            priority:0, 
            role:'common_users'
        },
        options: {
            roles:[
             <c:forEach var="role" items="${newRight.roles}" varStatus="status">
                ${status.index > 0 ? "," :""} "${role}"   
             </c:forEach>],
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

    </script>
    
    <form method="post" id="modifyRight">
        <fieldset style="border-width: 0px; padding: 0px;">


            <hr>
            <table>
            <tbody><tr><td> 
                <h3><label for="abstractUser">Uživatelská role</label></h3></td></tr>
                <tr><td>
        
                    <span id="roleTypeSpan">
                        <input type="radio"  onchange="_radio_change();" value="role" name="abstractUser" id="roleType"> Role 
                        <img src="img/rights-group.png">
                    </span>
    
                    <span id="allTypeSpan">
                        <input type="radio" onchange="_radio_change();" checked="checked" value="all" name="abstractUser" id="allType"> Všichni 
                        <img src="img/rights-person.png">
                        <img src="img/rights-group.png">
                    </span>
                    
                    <script type="text/javascript">
                                function _radio_change(elm) {
                                	var rb = $("input:radio:checked");
                                    if (rb.val()==="role")  {
                                        $("#userId").show();
                                        $("#userId").val("");
                                    } else {
                                        $("#userId").hide();
                                    }
                                 }        
                    </script>
                </td>
                </tr>
            
    
            <tr><td> 
                <div style="" id="userIdDiv">
                    <span>
                        <input type="text" onkeyup="_roles_keyup(this);" value="common_users" style="width: 100%;display: none; " size="10" name="userName" id="userId">

                        <script type="text/javascript">

                        function _roles_keyup(elm) {
                        	var userVal = $(elm).val();

                            rightContainer.data.role=userVal;

                        	var narr = map(function(item) {
                                        	    	  if (item.startsWith(userVal)) {
                                        	    		 return item;                                           	    		    
                                        	    	  } else return null;                                        	    	   
                                            	    },rightContainer.options.roles);

                                     	    if (narr.length > 0) {
                                                var nhtml = "";
                                                narr.forEach(function(item) {
                                                  nhtml = nhtml + "<div><a href='javascript:_role_select(\""+item+"\");'>"+item+"</a></div>";
                                                });
                                                $("#userautocomplete").html(nhtml);                                              
                                                $("#userautocomplete").show();
                                       	    }
                                         }

                                         
                                         function _role_select(what) {
                                        	    $("#userId").val(what);                                        	    
                                        	    $("#userautocomplete").hide();
                                        	    rightContainer.data.role=what;
                                         } 

                        </script>
                    </span>
                    
                    
                </div>

                <div style="border-top:1px dashed black;margin-top: 5px; display: none;" id="userautocomplete"> </div>

             </td></tr>
    
            </tbody></table>
    
            <hr>
            <table width="100%">
                <tbody><tr><td> <h3><label for="criterium">Dodatečná podmínka:</label></h3></td></tr>
                    <tr><td>
                            <div id="criteriumDiv">
                                <select size="1" name="criterium" id="criterium" onchange="_combo_criterium_change();">
                                <option value=""></option>
                                <c:forEach var="criteriumWrapper"  items="${newRight.criteriums}"> 
                                <option value="${criteriumWrapper.rightCriterium.QName}"><view:msg>${criteriumWrapper.rightCriterium.QName}</view:msg></option>
                                </c:forEach>                                     
                              </select>
                            </div>
                            
                            <script type="text/javascript">
                            function _combo_criterium_change() {
                                var selected = $("select[name='criterium'] option:selected").val();
                                if (selected) {
                                    $("#rightParamsCreation").show();
                                } else {
                                    $("#rightParamsCreation").hide();
                                }   
                                rightContainer.data.condition=selected;
                            }
                            </script>

                </td></tr>
    
                 <tr><td>   
                        <div style="display: none;" id="rightParamsCreation">
                            <label for="paramsVals">Hodnoty:</label>
                            <textarea style="width: 100%;" name="paramsVals" id="paramsVals" onkeyup="_text_area_change();"></textarea>
    
                            <label for="paramsVals">Předdefinované hodnoty:</label>
                            <select style="display: block;" name="params" size="1" id="params" onchange="_combo_params_change();">
                                    <option value="">Předdefinované hodnoty:</option>
                                    <c:forEach var="params" items="${newRight.rightCriteriumParams}"> 
                                        <option value="${params.id}">${params.shortDescription}</option>                                     
                                    </c:forEach>
                            </select>
                            
                            
                            <script type="text/javascript">

                            function _text_area_change() {
                                if (rightContainer.data.param.ident > 0) {
                                	rightContainer.data.param.dirty=true;
                                }
                            }
                            
                            function _combo_params_change() {
                                var selected = $("select[name='params'] option:selected").val();
                                // edit
                                if (selected) {
                                    alert(selected);
                                	var foundItem = reduce(function(base, item){
                                		var str = ""+item.ident;
                                        if (base != null) return base; 
                                        if (str==selected){
                                    	    return item;
                                    	} else return null;
                                    }, null,rightContainer.options.params);                  
                                    if (foundItem) {
                                        $("#paramsVals").val(foundItem.objects);
                                        $("#shortDesc").val(foundItem.shortDesc);
                                        rightContainer.data.param = foundItem;
                                    } else {
                                        $("#paramsVals").val("");
                                        rightContainer.data.param = {
                                                ident:0,
                                                shortDesc:'',
                                                objects:[]
                                        };
                                    }
                                } else {
                                    $("#paramsVals").val("");
                                    rightContainer.data.param = {
                                            ident:0,
                                            shortDesc:'',
                                            objects:[]
                                    };
                                 }  
                            }
                            
                            </script>
    
                            <label for="shortDesc">Popis:</label>
                            <input type="text" style="width: 100%;" name="shortDesc" id="shortDesc">
                            
                        </div>
                        
                </td></tr>
                
            </tbody></table>
            <hr>
    
            <table width="100%">
                <tbody><tr><td> <h3><label for="priority">Priorita:</label></h3>(měnit pouze ve výjimečných případech):</td></tr>
                <tr><td> <input type="text" style="width: 100%;" size="10" name="priority" id="priority"></td></tr>
            </tbody></table>
            <script type="text/javascript">
            function _priorty_change() {
            	rightContainer.data.priority = $("#priority").val();
            }
            </script>
        </fieldset>
        </form>
</div>

</scrd:loggedusers>