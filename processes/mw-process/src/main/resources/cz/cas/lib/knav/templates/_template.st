/** sablony pro mock process */
group applymw;



contextScript(defaultValue) ::=<<
    <script language="JavaScript" type="text/javascript"> 
    <!--    
        window.onProcessFormSend = function() {
            var vals = 'userValue='+\$('#userValue').val()
                        +';mode='+\$('input[name=mode]:checked').val()
                        +';pids='+reduce(function(base, item, status) {
                             base = base+encodeURI(item.pid.replaceAll(":","\\\\:"))+ (status.last ? "": ",");
                            return base;
            }, "",pidstructs());;

            var url = "lr?action=form_post&def=parametrizedapplymw&paramsMapping="+encodeURI("{")+vals+encodeURI("}")+"&out=text";
            processStarter("parametrizedapplymw").start(url);
        }
    -->        
    </script>
>>

iterateRepoScript(defaultValue) ::=<<
    <script language="JavaScript" type="text/javascript"> 
    <!--    
        window.onProcessFormSend = function() {
            var vals = 'userValue='+\$('#userValue').val()
                       + ';mode='+\$('input[name=mode]:checked').val();
            var url = "lr?action=form_post&def=parametrized_iterated_appliedmw&paramsMapping="+encodeURI("{")+vals+encodeURI("}")+"&out=text";
            processStarter("parametrized_iterated_appliedmw").start(url);
        }
    -->        
    </script>
>>


form(bundle,defaultValue, iterateRepo) ::=<<
    $if(iterateRepo)$
        $iterateRepoScript(defaultValue=defaultValue)$
    $else$
        $contextScript(defaultValue=defaultValue)$
    $endif$
<div>

        <table style="width:100%;">
            <tr style="width:100%;">
                <td>
                    <div id="form style="margin:5px;padding:3px">
                        <form>
                            <label>$bundle.("administrator.menu.dialogs.mw.value")$</label><br>
                            <input type="radio" id="year" name="mode" value="year" checked="checked">$bundle.("administrator.menu.dialogs.mw.year")$</label><br>
                            <input type="radio" id="month" name="mode" value="month">$bundle.("administrator.menu.dialogs.mw.month")$</label><br>
                            <input type="text"  id="userValue" value="$defaultValue$" style="width:30%;margin-top:5px"></input>
                        </form>
                    </div>
                </td>
            </tr>
        </table>

    
</div>

>>