<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<view:kconfig var="output" key="localprint.output" />


<%@ page isELIgnored="false"%>


<style>
#overlay {
    position: absolute;
    z-index: 13;
    width: 100%;
    height: 100%;
    left: 0;
    top: 36px;
}




#okButton {
    position:absolute;
    top:20px;
    left:110px;
    width:30px;
    height:30px;
    background-color:rgba(28, 21, 32, 0.0);
    border:none;
    padding: 0px;
    margin: 0px;
}

#cancelButton {
    position:absolute;
    top:20px;
    left:80px;
    width:30px;
    height:30px;
    background-color:rgba(28, 21, 32, 0.0);
    border:none;
    padding: 0px;
    margin: 0px;
}

#selectbox {
    position:absolute;
    top:30px;
    left:40px;
    width:100px;
    height:100px;
    background-color:rgba(28, 21, 32, 0.0);
    border:1px dashed white;
    padding: 0px;
    margin: 0px;
}

#right-top {
    position:absolute; 
    top:30px;left:136px; 
    width:4px; 
    height:4px; 
    border:2px black solid;
    padding: 0px; 
    margin: 0px;
}


#left-top {
    position:absolute; 
    top:30px;left:40px; 
    width:4px; 
    height:4px; 
    border:2px black solid;
    padding: 0px; 
    margin: 0px;
}

.point {
        background-color: blue;
}
.point:hover {
    border:2px red solid;
}


#left-bottom {
    position:absolute; 
    top:125px;left:40px; 
    width:4px; 
    height:4px; 
    border:2px black solid;
    padding: 0px; 
    margin: 0px;
}

#right-bottom {
    position:absolute; 
    top:125px;left:136px; 
    width:4px; 
    height:4px; 
    border:2px black solid;
    padding: 0px; 
    margin: 0px;
}

</style>


<table style="width:100%; height:100%"> 

<tr>

<td style="width:80%">

    <div id="imagepart" style="width:100%; height:100%; position:relative; background-color:gray;">
        <div id="overlay" style="display:none">
            <div id="selectbox"></div>
            <div id="left-top" class="point"></div>
            <div id="right-top" class="point"></div>
            <div id="left-bottom" class="point"></div>
            <div id="right-bottom" class="point"></div>
        </div>
    </div>

</td>

<td style="width:20%;  height:100%; vertical-align: top;  text-align:center">


<c:if test = "${output == 'pdf'}">
<div id="output">
<h4><view:msg>print.settings.label</view:msg></h4>

<select id="pagepart">
  <option value="A4"><view:msg>print.settings.a4</view:msg></option>
  <option value="A3"><view:msg>print.settings.a3</view:msg></option>
</select>

</div>
</c:if>

 </td>
</tr>
</table>


 
