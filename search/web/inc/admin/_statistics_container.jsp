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

        $( "#report_date_from" ).datepicker({
            dateFormat: 'yy.mm.dd'
        });
        $( "#report_date_to" ).datepicker({
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

        <tr><td colspan="3"> <strong><view:msg>statistics.report.model</view:msg> </strong></td></tr>    

        <tr>
        <td>
         <br/>
          <select id="report_type">
              <c:forEach items="${statistics.models}" var="val">
                  <option value="${val}"> <view:msg>fedora.model.${val}</view:msg>
                  </option>
              </c:forEach>
          </select>
        </td>

        <td></td>

        <td style="text-align: right;"> 
            <a href="javascript:statistics.showModelReport(_action(),$('#report_type option:selected').val());" class="buttons"><view:msg>statistics.main_dialog.displaygraph</view:msg></a>
            <a href="javascript:statistics.modelCSV(_action(),$('#report_type option:selected').val());" class="buttons"><view:msg>common.format.CSV</view:msg></a>
            <a href="javascript:statistics.modelXML(_action(),$('#report_type option:selected').val());" class="buttons"><view:msg>common.format.XML</view:msg></a>
            </td>
        </tr>

        <tr><td colspan="3"> <hr/></td></tr>    

        <tr><td colspan="3"> <strong><view:msg>statistics.report.dates</view:msg></strong></td></tr>    
        <tr>
            <td>

            <view:msg>statistics.report.dates.datefrom</view:msg>:<input id="report_date_from" type="text"></input>  
            <view:msg>statistics.report.dates.dateto</view:msg> <input id="report_date_to" type="text"></input></td>
            
            <td><span></span></td>
            
            <td style="text-align: right;" width="30%">
            <a href="javascript:statistics.showDatesRangeReport(_action(),$('#report_date_from').val(),$('#report_date_to').val());" class="buttons"> <view:msg>statistics.main_dialog.displaygraph</view:msg> </a>
            <a href="javascript:statistics.dateDurationCSV(_action(),$('#report_date_from').val(),$('#report_date_to').val());" class="buttons"><view:msg>common.format.CSV</view:msg></a>
            <a href="javascript:statistics.dateDurationXML(_action(),$('#report_date_from').val(),$('#report_date_to').val());" class="buttons"><view:msg>common.format.XML</view:msg></a>
            </td>
        </tr>

        <tr><td colspan="3"> <hr/></td></tr>    
        <tr><td colspan="3"> <strong><view:msg>statistics.report.authors</view:msg></strong></td></tr>    

        <tr>
            <td colspan="2"></td>
            <td style="text-align: right;">
                <a href="javascript:statistics.showAuthorReport(_action());" class="buttons"><view:msg>statistics.main_dialog.displaygraph</view:msg></a>
                <a href="javascript:statistics.authorCSV(_action());" class="buttons"><view:msg>common.format.CSV</view:msg></a>
                <a href="javascript:statistics.authorXML(_action());" class="buttons"><view:msg>common.format.XML</view:msg></a>
            </td>
        </tr>

        <tr><td colspan="3"> <hr/></td></tr>    
        <tr><td colspan="3"> <strong><view:msg>statistics.report.lang</view:msg></strong></td></tr>    

        <tr>
            <td colspan="2"></td>
            <td style="text-align: right;">
                <a href="javascript:statistics.showLangReport(_action());" class="buttons"><view:msg>statistics.main_dialog.displaygraph</view:msg></a>
                <a href="javascript:statistics.langCSV(_action());" class="buttons"><view:msg>common.format.CSV</view:msg></a>
                <a href="javascript:statistics.langXML(_action());" class="buttons"><view:msg>common.format.XML</view:msg></a>
            </td>
        </tr>

        <tr><td colspan="3"> <hr/></td></tr>    
        <tr><td colspan="3"> <strong> <view:msg>statistics.report.exports</view:msg></strong></td></tr>    
        <tr>
            <td colspan="2"></td>
            <td style="text-align: right;">
            <script type="text/javascript">
              function _CSV() {
                 window.open('stats?format=CSV', '_blank');
              }
              function _XML() {
                 window.open('stats?format=XML', '_blank');
              }
             </script>
            
            <!--
            <a href="javascript:_CSV();" class="buttons">CSV</a>
            -->
            
            <a href="javascript:_XML();" class="buttons">XML</a>
            
            </td> 
        </tr>

        </tbody>
    </table>
        
</fieldset>

</scrd:securedContent>
