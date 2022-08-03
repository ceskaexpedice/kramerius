/** default sablona */
group defaultTemplate;


/** formular */
form(bundle, sdnntendoint, krameriusinstance,  process, allLabels) ::=<<

<div>
    <script language="JavaScript" type="text/javascript">
    <!--

        window.onProcessFormSend = function() {
            var charsThatMustBeEscaped = [':',';','}','{', '\\\\'];

            var vals = 'sdnntchange='+   \$('#sdnntendpoint').val().escapeChars(charsThatMustBeEscaped)
                +';krameriusInstance='+  \$('#krameriusinstance').val().escapeChars(charsThatMustBeEscaped);

            var url = "lr?action=form_post&def=parametrizedsdnntlist&paramsMapping={"+vals+"}&out=text";

            processStarter("parametrizedsdnntlist").start(url);
        }
    -->
    </script>

<form>


<fieldset style="margin-top:10px;border: 1px solid gray;">
	<h3>SDNNT synchronizace</h3>
    <div id="dnnt_export_label" >
	
		<table width="100%">
			<tr>
				<td width="30%"><label>SDNNT instance:</label></td>
				<td><input style="width:80%" type="text"  id="sdnntendpoint" value="$sdnntendoint$"></input></td>
			</tr>
			
			<tr>
				<td width="30%">
					<label>Instance krameria:</label>
				 </td>
				 <td>
					<input style="width:80%" type="text"  id="krameriusinstance" value="$krameriusinstance$"></input>
				</td>
			</tr>
		</table>
	
    </div>
</form>


</div>
>>