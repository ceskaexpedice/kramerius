/** default sablona */
group defaultTemplate;


/** formular */
form(bundle,  process, allLabels) ::=<<

<div>
    <script language="JavaScript" type="text/javascript">
    <!--

        window.onProcessFormSend = function() {
            var charsThatMustBeEscaped = [':',';','}','{', '\\\\'];
            var dnntLabel = \$('#dnnt-label').val();
            //var dnntLabelEnabled = \$( "#dnnt_export_label_enabled:checked" ).length ==1 ? true : false;
            var vals = 'dnnt-label='+dnntLabel;
            var url = "lr?action=form_post&def=parametrizeddnntexport&paramsMapping={"+vals+"}&out=text";
            processStarter("parametrizeddnntexport").start(url);
        }
    -->
    </script>

<form>


<fieldset style="margin-top:10px;border: 1px solid gray;">
    <div id="dnnt_export_label" >
      <label>$bundle.("export.csv.dnnt.label")$</label><br>
      <select  id="dnnt-label">
        $allLabels:{oneLabel | <option value="$oneLabel$">$oneLabel$</option> };separator="\n"$
      </select>
    </div>
</form>


</div>
>>