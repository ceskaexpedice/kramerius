<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>
<view:object name="statistics"
    clz="cz.incad.Kramerius.views.statistics.ModelStatisticsViewObject"></view:object>

<scrd:securedContent action="show_statictics">


<script>
    $(document).ready(function(){
        $('.buttons').button();

        $( "#report_date_from_pids" ).datepicker({
            dateFormat: 'yy.mm.dd'
        });
        $( "#report_date_to_pids" ).datepicker({
            dateFormat: 'yy.mm.dd'
        });

    });

    function _action() {
        var action = $("#report_action").val();
        if (action === "All") {
            if (console) console.log("all");
            action = null;
        }
        return action;
    }
    
    
	function _pids() {
		var pids = getAffectedPids();
		var pids = map(function(pid) { 
		    var divided = pid.split("_");            
			return divided[1];
		}, pids);     
		return pids;
	}

    
</script>

<fieldset style="border: 0px; margin: 0px; padding: 0px;">
    <table style="width:100%">
        
        <thead>
            <tr>
               <td style="width:80%"></td> 
               <td></td> 
            </tr>
        </thead>
        
        <tbody>
            <tr>
                <td><strong><view:msg>statistics.main_dialog.filter</view:msg></strong></td>               
                <td><view:msg>common.action</view:msg>:</td>
            </tr>
            <tr>
                <td></td>               
                 <td>
                   <select id="report_action">
                       <option value="${statistics.allFilterOption}"><view:msg>statistics.main_dialog.actions.${statistics.allFilterOption}</view:msg> </option>
                       <c:forEach items="${statistics.actionFilterOptions}" var="fltr">
                           <option value="${fltr}"><view:msg>statistics.main_dialog.actions.${fltr}</view:msg></option>
                       </c:forEach>
                   </select>
                </td>
            
            </tr>
        </tbody>
    </table>

</fieldset>

<hr></hr>

<fieldset  style="border: 0px; margin: 0px; padding: 0px;">
    <table style="width:100%">
        <thead>
            <tr>
                <td width="20%"></td><td></td><td width="50%"></td>
            </tr>
        </thead>
        <tbody>


        <tr><td colspan="3"> <strong><view:msg>statistics.report.pids</view:msg></strong></td></tr>    
        <tr>
            <td></td>
            
            <td><span></span></td>
            
            <td style="text-align: right;" width="30%">
            <a href="javascript:statistics.showPidsReport(_action(), _pids());" class="buttons"> <view:msg>statistics.main_dialog.displaygraph</view:msg> </a>
            <a href="javascript:statistics.pidsCSV(_action(), _pids());" class="buttons"><view:msg>common.format.CSV</view:msg></a>
            <a href="javascript:statistics.pidsXML(_action(), _pids());" class="buttons"><view:msg>common.format.XML</view:msg></a>
            </td>
        </tr>

        </tbody>
    </table>
        
</fieldset>

</scrd:securedContent>
