<%@ page pageEncoding="UTF-8" %>
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>


<view:object name="regNewUser" clz="cz.incad.Kramerius.views.inc.RegisterNewUserView"></view:object>


<style>
<!--
.reguserform {
    width: 100%;
    font-size: large;
    color: black;
}

.reguserformerror {
    color: red;
}
-->
</style>

<script type="text/javascript">
<!--
var reguserDefaults= {
     loginNames:
[<c:forEach var="loginName" varStatus="loginNameStatus" items="${regNewUser.userLoginNames}" > <c:if test="${!loginNameStatus.first}">,</c:if>'${loginName}' </c:forEach>]
};


function RegUserValidate() {
    this.validatedInput = false;
}

RegUserValidate.prototype.validateLoginNames = function() {
    var lname = $('#regUserLoginName').val();
    var found = reduce(function(base,element,status) {
        if (base) return true;
    	if (lname == element) {
            return true;
        } else return false;
    }, false, reguserDefaults.loginNames);
    if (found) {
    	$("#regUserLoginName_error").html(dictionary['registeruser.errormessages.loginnamexist']);
    }
    this.validatedInput = this.validatedInput && (!found);
}


RegUserValidate.prototype.emptyPasswords = function() {
    var pswd = $('#regUserPswd').val();
    var emptyPswd =  pswd != "";
    if (!emptyPswd) {
        $("#regUserPswd_error").html(dictionary['registeruser.errormessages.emptypswd']);
    }
    this.validatedInput = this.validatedInput && emptyPswd;
}

RegUserValidate.prototype.emptyLoginname = function() {
    var loginName = $('#regUserLoginName').val();
    var emtptyLoginName =  loginName != "";
    if (!emtptyLoginName) {
        $("#regUserLoginName_error").html(dictionary['registeruser.errormessages.emptyloginame']);
    }
    this.validatedInput = this.validatedInput && emtptyLoginName;
}


RegUserValidate.prototype.validatePasswords = function() {
    var pswd = $('#regUserPswd').val();
	var reppswd = $('#regUserRepeatePswd').val();
    var samePswds =  pswd == reppswd;
    if (!samePswds) {
        $("#regUserPswd_error").html(dictionary['registeruser.errormessages.differentpswds']);
    }
    this.validatedInput = this.validatedInput && samePswds;
}

RegUserValidate.prototype.validateEmail = function() {
	var isEmail_re       = /^\s*[\w\-\+_]+(\.[\w\-\+_]+)*\@[\w\-\+_]+\.[\w\-\+_]+(\.[\w\-\+_]+)*\s*$/;
	function isEmail (s) {
	   return String(s).search (isEmail_re) != -1;
	}
    var email = $('#regUserEmail').val();
    var goodEmailAddress = email && isEmail(email);
    if (!goodEmailAddress) {
        $("#regUserEmail_error").html(dictionary['registeruser.errormessages.bademail']);
    }
    this.validatedInput = this.validatedInput && goodEmailAddress;
}

RegUserValidate.prototype.grabData = function() {
    var data = {
    	    'loginName':$('#regUserLoginName').val(),
            'email':$('#regUserEmail').val(),
            'pswd':$('#regUserPswd').val(),
            'name':$('#regUserName').val()
    };
    return data;
}

RegUserValidate.prototype.validate=function() {
	["#regUserLoginName_error","#regUserPswd_error","#regUserEmail_error"].forEach(function(item) {
        $(item).html('');    
    });
 
	this.validatedInput = true;

    this.emptyPasswords();
    this.emptyLoginname();
     
	this.validateLoginNames();
    this.validateEmail();
    this.validatePasswords();

    return this.validatedInput;
}


var regUserValidate = new RegUserValidate();

//-->
</script>


<h3><view:msg>registeruser.title</view:msg></h3>

<table style="width:100%">

<tbody>


<tr><td><label><view:msg>registeruser.loginname</view:msg> </label><span id="regUserLoginName_error" class="reguserformerror"></span></td></tr>
<tr><td><input class="reguserform" id='regUserLoginName' ></input></td></tr>

<tr><td><label><view:msg>registeruser.namesurname</view:msg> </label><span id="regUserName_error" class="reguserformerror"></span></td></tr>
<tr><td><input class="reguserform" id='regUserName' ></input></td></tr>

<tr><td><label><view:msg>registeruser.email</view:msg></label><span id="regUserEmail_error" class="reguserformerror"></span></td></tr>
<tr><td><input class="reguserform" id='regUserEmail' ></input></td></tr>

<tr><td><label><view:msg>registeruser.password</view:msg></label><span id="regUserPswd_error" class="reguserformerror"></span></td></tr>
<tr><td><input type="password" class="reguserform" id='regUserPswd'></input></td></tr>

<tr><td><label><view:msg>registeruser.retypepassword</view:msg></label></td></tr>
<tr><td><input type="password"  class="reguserform" id='regUserRepeatePswd'></input></td></tr>
                
</tbody></table>

<div>

</div>

