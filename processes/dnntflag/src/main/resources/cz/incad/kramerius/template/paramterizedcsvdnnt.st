/** sablona pro replikace */
group replicationTemplate;

/** jedna polozka stromu */
treeNode(item) ::=<<
    $if(item.leaf)$

    <li id="$item.id$"><a href="#">$item.itemName$</a></li>

    $else$
    <li id="$item.id$">
    <a href="#">$item.itemName$</a>
    $tree(root=item)$
    </li>
    $endif$

>>


/** strom */
tree(root) ::=<<
    <ul>
        $root.children:{item|$treeNode(item=item)$}$
    </ul>
>>





form(postfixdiv, csvRootDirectory,csvfile, bundle, process,labelProcess, allLabels) ::=<<

<div style="width:100%">
    <script language="JavaScript" type="text/javascript">
    <!--


       \$('#dnnt_label_enabled').change(function() {
           \$( "#dnnt_label_div" ).toggle();
       });

        var legacyprocess = "$process$";
        var labelProcess = "$labelProcess$";


        // send parameters to server
        window.onProcessFormSend = function() {

            var label =  \$('#label').val();

            var charsThatMustBeEscaped = [':',';','}','{','\\\\'];

            var vals = 'csvfile='+encodeURIComponent(\$('#csvfile').val().escapeChars(charsThatMustBeEscaped))
                    +';label='+ \$('#label').val();

            //var url = "lr?action=form_post&def="+labelProcess+"&paramsMapping="+encodeURI("{")+vals+encodeURI("}")+"&out=text";

            var url = "lr?action=form_post&def="+labelProcess+"&paramsMapping="+encodeURI("{")+vals+encodeURI("}")+"&out=text";

            processStarter(labelProcess).start(url);


        }


        // show directory dialog
        function ShowDirectoryTree() {
            this.dialog = null;
        }

        ShowDirectoryTree.prototype.openTree=function() {

            if (this.dialog) {
                this.dialog.dialog('open');
            } else {
                this.dialog = \$("#_selectDNNTFolderDialog").dialog({
                bgiframe: true,
                width:  450,
                height:  400,
                modal: true,
                title: '',
                buttons:
                    [{
                        text:dictionary['common.ok'],
                        click:bind(function() {
                            \$("#csvfile").val(\$("#_selectedDNNTFolder").text());
                            this.dialog.dialog("close");
                        },this)
                    },{
                        text:dictionary['common.close'],
                        click:bind(function() {
                            this.dialog.dialog("close");
                        },this)
                    }]
                });



              \$(function () {
                   \$("#_selectDNNTFolderTree_$postfixdiv$").jstree({
                        "plugins" : [ "themes", "html_data", "ui" ]
                    })
                    .bind("select_node.jstree", function (event, data) {
                    var p = data.rslt.obj.attr("id");
                    \$("#_selectedDNNTFolder").text(p);
                    });
                });


            }


        }


        var directoryTree = new ShowDirectoryTree();

    -->
    </script>

<!-- vyber adresaru -->
<fieldset style="margin-top:10px;border: 1px solid gray;">
    <legend style="border:none">$bundle.("import.selection.dnnt.dialog")$</legend>

    <input style="width:80%" type="text"  id="csvfile" value="$csvfile$"></input>
    <input type="button" onclick="directoryTree.openTree();" value="$bundle.("import.directory.selection")$"></input>
</fieldset>

<!-- priznak -->
<fieldset style="margin-top:10px;border: 1px solid gray;">
    <legend style="border:none">$bundle.("import.selection.dnnt.labels")$</legend>


    <div id="dnnt_label_div" >
    <label for="label">Label:</label>
    <select  id="label">
        $allLabels:{oneLabel | <option value="$oneLabel$">$oneLabel$</option> };separator="\n"$
    </select>
    </div>
</fieldset>


    <div id="_selectDNNTFolderDialog" style="display:none">
        <h3>  $bundle.("import.selection.dnnt.dialog")$</h3>
        <div id="_selectDNNTFolderTree_$postfixdiv$">
            $tree(root=csvRootDirectory)$
        </div>

        <div id="_selectedDNNTFolder" style="display:none"></div>
    </div>
</div>

>>