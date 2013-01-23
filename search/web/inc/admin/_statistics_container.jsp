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

    
</script>

<fieldset>

    <table>
        <thead>
            <tr>
                <td width="20%"></td><td width="80%"></td><td></td>
            </tr>
        </thead>
        <tbody>
        <tr><td colspan="3"> <strong><view:msg>statistics.report.model</view:msg> </strong></td></tr>    
        <tr>

        <td>
        Model <br/>
        <select id="report_type">
            <c:forEach items="${statistics.models}" var="val">
                <option value="${val}"><view:msg>fedora.model.${val}</view:msg></option>
            </c:forEach>
        </select>

        </td>
        <td>
        </td>
        <td style="text-align: right;"> 
            <script type="text/javascript">function _showModelReport() {
                var model = $('#report_type option:selected').val();
                statistics.showModelReport(model);
             }</script>
            <a href="javascript:_showModelReport();" class="buttons"><view:msg>common.display</view:msg></a></td>
        </tr>

        <tr><td colspan="3"> <hr/></td></tr>    

        <tr><td colspan="3"> <strong><view:msg>statistics.report.dates</view:msg></strong></td></tr>    
        <tr>
            <td>
            <script type="text/javascript">function _showDatesReport() {
                var dateFrom = $('#report_date_from').val();
                var dateTo = $('#report_date_to').val();
                statistics.showDatesRangeReport(dateFrom, dateTo);
             }</script>
            
            Datum od <input id="report_date_from" type="text"></input>  Do <input id="report_date_to" type="text"></input></td>
            <td><span></span></td>
            <td style="text-align: right;"><a href="javascript:_showDatesReport();" class="buttons"> <view:msg>common.display</view:msg> </a></td>
        </tr>

        <tr><td colspan="3"> <hr/></td></tr>    
        <tr><td colspan="3"> <strong><view:msg>statistics.report.authors</view:msg></strong></td></tr>    

        <tr>
            <td colspan="2"></td>
            <td style="text-align: right;">
            <script type="text/javascript">function _showAuthorReport() {
                statistics.showAuthorReport();
             }</script>
            <a href="javascript:_showAuthorReport();" class="buttons"><view:msg>common.display</view:msg></a></td>
        </tr>

        <tr><td colspan="3"> <hr/></td></tr>    
        <tr><td colspan="3"> <strong><view:msg>statistics.report.lang</view:msg></strong></td></tr>    

        <tr>
            <td colspan="2"></td>
            <td style="text-align: right;">
            <script type="text/javascript">function _showLangReport() {
                statistics.showLangReport();
             }</script>
            <a href="javascript:_showLangReport();" class="buttons"><view:msg>common.display</view:msg></a></td>
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
            
            <table><tr><td> <a href="javascript:_CSV();" class="buttons">CSV</a></td> <td> <a href="javascript:_XML();" class="buttons">XML</a></td> </tr></table></td>
        </tr>

        </tbody>
    </table>
        
</fieldset>

</scrd:securedContent>
