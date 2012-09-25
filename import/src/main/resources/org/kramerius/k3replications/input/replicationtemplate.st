/** sablony pro k3 replicate process */
group k3replicate;


/** html polozka stromu */
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


/** html strom */
tree(root) ::=<<
    <ul>
        $root.children:{item|$treeNode(item=item)$}$ 
    </ul>
>>

/** strom - js inicializace */
makeJSTree(treeRoot, bindfunction) ::=<<
        \$(function () {
            \$("#$treeRoot$").jstree({ 
                "plugins" : [ "themes", "html_data", "ui" ]
            })
            .bind("select_node.jstree", function (event, data) { 
                var p = data.rslt.obj.attr("id");
                $(bindfunction)()$;
            });
        });
>>


/** migration folder selection */
selectionJSTreeMigration() ::=<<
\$("#_migrationDirectory").text(p);
>>

/** target folder selection */
selectionJSTreeTarget() ::=<<
\$("#_targetDirectory").text(p);
>>






form(migrationDirectory,targetDirectory,importRootDirectory, bundle) ::=<<

<div style="width:100%">
    <script language="JavaScript" type="text/javascript"> 
    <!--    
        // send parameters to server
        window.onProcessFormSend = function() {
            var charsThatMustBeEscaped = [':',';','}','{'];
            
            var startIndexer = \$('#startIndexer').attr('checked');
            var defaultRights = \$('#defaultRights').attr('checked');
            var ingestSkip = \$('#ingestSkip').attr('checked');
            
            var replTypeId = \$("input[@name=repltype]:checked").attr('id');

            var vals = 'replicatetype='+replTypeId
                +';idlist='+   \$('#idlist').val().escapeChars(charsThatMustBeEscaped)
                +';migrationDirectory='+ \$('#migrationDirectory').val().escapeChars(charsThatMustBeEscaped)
                +';targetDirectory='+\$('#targetDirectory').val().escapeChars(charsThatMustBeEscaped)
                +';defaultRights='+defaultRights
                +';startIndexer='+startIndexer
                +';indexSkip='+ingestSkip;
    
                
            var url = "lr?action=form_post&def=k3_replication&paramsMapping={"+vals+"}&out=text";
            processStarter("k3_replication").start(url);
        }

        

        // show directory dialog
        function _RepliacitonsTree() {
            this.importDialog = null;
            this.targetDialog = null;
        }

        _RepliacitonsTree.prototype.openImportTree=function() {
            if (this.importDialog) {
                this.importDialog.dialog('open');
            } else {
                this.importDialog = \$("#_select_migration_directory").dialog({
                bgiframe: true,
                width:  450,
                height:  400,
                modal: true,
                title: dictionary['k3replication.migration.directory'],
                buttons: 
                    [{
                        text:dictionary['common.ok'],
                        click:bind(function() {
                            \$("#migrationDirectory").val(\$("#_migrationDirectory").text());
                             this.importDialog.dialog("close"); 
                        },this) 
                    },{
                        text:dictionary['common.close'],
                        click:bind(function() {
                             this.importDialog.dialog("close"); 
                        },this) 
                    }]
                });
                
            }
        }
    
        _RepliacitonsTree.prototype.openTargetTree=function() {
            if (this.targetDialog) {
                this.targetDialog.dialog('open');
            } else {
                this.targetDialog = \$("#_select_target_directory").dialog({
                bgiframe: true,
                width:  450,
                height:  400,
                modal: true,
                title: dictionary['k3replication.target.directory'],
                buttons: 
                    [{
                        text:dictionary['common.ok'],
                        click:bind(function() {
                            \$("#targetDirectory").val(\$("#_targetDirectory").text());
                            this.targetDialog.dialog("close"); 
                        },this) 
                    },{
                        text:dictionary['common.close'],
                        click:bind(function() {
                            this.targetDialog.dialog("close"); 
                        },this) 
                    }]
                });
                
            }
        }
    
        $makeJSTree(treeRoot="_targetDirectoryTree", bindfunction="selectionJSTreeTarget")$
        $makeJSTree(treeRoot="_migrationDirectoryTree", bindfunction="selectionJSTreeMigration")$
    
        var trees = new _RepliacitonsTree();
        
    -->        
    </script>


        <table style="width:100%;">
            <thead>
                <tr> <td width="80%"></td><td></td>
            </thead>
            <tbody>

                <tr>
                    <td colspan="2"> <label>$bundle.("k3replication.migration.directory")$</label> </td>
                </tr>
            
                <tr>
                        <td>
                            <input style="width:100%" type="text"  id="migrationDirectory" value="$migrationDirectory$"></input>
                        </td>
                        <td>
                            <input type="button" onclick="trees.openImportTree();" value="$bundle.("k3replication.directory.selection")$"></input>
                        </td>
                </tr>

                <tr>
                    <td colspan="2"> <label>$bundle.("k3replication.target.directory")$</label> </td>
                </tr>

                <tr>
                        <td>
                            <input style="width:100%" type="text"  id="targetDirectory" value="$targetDirectory$"></input>
                        </td>
                        <td>
                            <input type="button" onclick="trees.openTargetTree();" value="$bundle.("k3replication.directory.selection")$"></input>
                        </td>
                </tr>

                <tr>
                    <td colspan="2"> <label>$bundle.("k3replication.type")$</label> </td>
                </tr>

                <tr>
                        <td colspan="2">
                            <input type="radio" name="repltype" id="replTypeMonograph" value="monograph" checked="checked">$bundle.("k3replication.type.monographs")$</input>
                            <input type="radio" name="repltype" id="replTypePeriodical" value="periodical">$bundle.("k3replication.type.periodicals")$</input>
                        </td>
                </tr>

                <tr>
                    <td colspan="2"> <label>$bundle.("k3replication.idlist")$</label> </td>
                </tr>

                <tr>
                    <td> <textarea id="idlist" rows="5" style="width:100%;"></textarea></td>
                </tr>

                <tr>
                    <td colspan="2"> <input type="checkbox" id="startIndexer" name="startIndexer" value="indexer">$bundle.("k3replication.indexerstart")$</input></td>
                </tr>

                <tr>
                    <td colspan="2"> <input type="checkbox" id="defaultRights" name="defaultRights" value="defaultRights">$bundle.("k3replication.defaultrights")$</input></td>
                </tr>
                <tr>
                    <td colspan="2"> <input type="checkbox" id="ingestSkip" name="ingestSkip" value="ingestSkip">$bundle.("k3replication.ingestskip")$</input></td>
                </tr>
    
            </tbody>
        </table>

        
    <div id="_select_migration_directory" style="display:none">

        <h3>  $bundle.("import.selection.dialog")$</h3>

        <div id="_migrationDirectoryTree">
            $tree(root=importRootDirectory)$
        </div>

        <div id="_migrationDirectory" style="display:none"></div>    
    </div>    


    <div id="_select_target_directory" style="display:none">

        <h3>  $bundle.("import.selection.dialog")$ </h3>

        <div id="_targetDirectoryTree">
            $tree(root=importRootDirectory)$
        </div>

        <div id="_targetDirectory" style="display:none"></div>    
    </div>    
    
    
</div>

>>