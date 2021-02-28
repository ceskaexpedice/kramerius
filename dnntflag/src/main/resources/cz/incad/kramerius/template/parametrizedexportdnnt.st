/** default sablona */
group defaultTemplate;


/** formular */
form(bundle,  process) ::=<<

<div>
    <script language="JavaScript" type="text/javascript">
    <!--

        \$('#dnnt_export_label_enabled').change(function() {
           \$( "#dnnt_export_label" ).toggle();
       });

        window.onProcessFormSend = function() {
            var charsThatMustBeEscaped = [':',';','}','{', '\\\\'];
            var dnntLabel = \$('#dnnt-label').val();
            var dnntLabelEnabled = \$( "#dnnt_export_label_enabled:checked" ).length ==1 ? true : false;
            if (dnntLabelEnabled) {
                var vals = 'dnnt-label='+dnntLabel;
                var url = "lr?action=form_post&def=parametrizeddnntexport&paramsMapping={"+vals+"}&out=text";
                processStarter("parametrizeddnntexport").start(url);
            } else {
                var url = "lr?action=start&def=dnntexport&out=text";
                processStarter("dnntexport").start(url);
            }
        }
    -->
    </script>

<form>


<fieldset style="margin-top:10px;border: 1px solid gray;">

    <div>
      <label for="dnnt_export_label_enabled">$bundle.("export.csv.dnnt.labels.enabled")$:</label>
      <input type="checkbox" id="dnnt_export_label_enabled"></input>

    </div>

    <div id="dnnt_export_label" style="display:none">
      <label>$bundle.("export.csv.dnnt.label")$</label><br>
      <input style="width:80%" type="text"  id="dnnt-label" value=""></input>
    </div>

</form>


</div>
>>