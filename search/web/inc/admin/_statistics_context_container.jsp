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

        $( "#report_date_from_pid" ).datepicker({
            dateFormat: 'yy.mm.dd'
        });
        $( "#report_date_to_pid" ).datepicker({
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
    
    function _visibility() {
        return $("#report_visibility_pid").val();
    }
    
    function _ip_address() {
        return $("#report_ip_addresses_pid").val();
    }
    function _ip_address_unique() {
        return $("#report_unique_ip_addresses_pid").attr('checked');
    }

    
</script>

<fieldset style="border: 0px; margin: 0px; padding: 0px;">
    <table style="width:100%">
        
        <thead>
            <tr>
               <td style="width:40%"></td> 
               <td style="width:40%"></td> 
               <td></td>
            </tr>
        </thead>
        
        <tbody>

            <tr>
                <td><strong><view:msg>statistics.main_dialog.filter</view:msg></strong></td>
                <td></td>
                <td></td>
                <td></td>
            </tr>
        	
            <tr>
                <td><view:msg>statistics.report.dates.datefrom</view:msg>:</td>
                <td><view:msg>statistics.report.dates.dateto</view:msg>:</td>
                <td><view:msg>common.action</view:msg>:</td>
                <td><view:msg>k3replication.defaultrights.fields</view:msg>:</td>
                <td><view:msg>statistics.report.ip.forbidden</view:msg>:</td>
            </tr>
            
            <tr>
                <td><input id="report_date_from_pid" type="text"></td>
                <td><input id="report_date_to_pid" type="text"></td>
                 <td>
                   <select id="report_action">
                       <option value="${statistics.allFilterOption}"><view:msg>statistics.main_dialog.actions.${statistics.allFilterOption}</view:msg> </option>
                       <c:forEach items="${statistics.actionFilterOptions}" var="fltr">
                           <option value="${fltr}"><view:msg>statistics.main_dialog.actions.${fltr}</view:msg></option>
                       </c:forEach>
                   </select>
                </td>
                
                 <td>
                   <select id="report_visibility_pid">
                       <option value="all"><view:msg>common.all</view:msg></option>
                       <option value="public"><view:msg>k3replication.defaultrights.public</view:msg></option>
                       <option value="private"><view:msg>k3replication.defaultrights.private</view:msg></option>
                   </select>
                </td>
                
                <td><input id="report_ip_addresses_pid" type="text"></td>
            </tr>
            
            <tr>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td><input type="checkbox" id="report_unique_ip_addresses_pid"><view:msg>statistics.report.ip.unique</view:msg></input></td>
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
            <a href="javascript:statistics.showPidsReport(_action(), _pids(), _visibility(), $('#report_date_from_pid').val(),$('#report_date_to_pid').val(), _ip_address(), _ip_address_unique());" class="buttons"><view:msg>statistics.main_dialog.displaygraph</view:msg> </a>
            <a href="javascript:statistics.pidsCSV(_action(), _pids(), _visibility(),$('#report_date_from_pid').val(),$('#report_date_to_pid').val(), _ip_address(), _ip_address_unique());" class="buttons"><view:msg>common.format.CSV</view:msg></a>
            <a href="javascript:statistics.pidsXML(_action(), _pids(), _visibility(),$('#report_date_from_pid').val(),$('#report_date_to_pid').val(), _ip_address(), _ip_address_unique());" class="buttons"><view:msg>common.format.XML</view:msg></a>
            </td>
        </tr>

        </tbody>
    </table>
        
</fieldset>

</scrd:securedContent>
