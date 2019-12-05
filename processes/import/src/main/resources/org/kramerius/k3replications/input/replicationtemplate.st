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






form(migrationDirectory,targetDirectory,importRootDirectory,visibility,otherSettingsTemplate, bundle) ::=<<

<div style="width:100%">
    <script language="JavaScript" type="text/javascript"> 
    <!--    

        \$('#importToFedora').change( function() {
            var importToFedora = \$('#importToFedora').attr('checked');
            if (importToFedora) {
                \$('#startIndexer').removeAttr("disabled");
            } else {
                \$('#startIndexer').removeAttr("checked");
                \$('#startIndexer').attr("disabled", true);
            }
        });

        // send parameters to server
        window.onProcessFormSend = function() {
            var charsThatMustBeEscaped = [':',';','}','{', '\\\\'];

            var importToFedora = \$('#importToFedora').attr('checked');
            
            var startIndexer = \$('#startIndexer').attr('checked');
            var defaultRights = \$('#defaultRightsPublic').attr('checked');

            var ingestSkip = !importToFedora;
            var replTypeId = \$("input[name=repltype]:checked").val();
            // TODO: idlist encode 
            var encodedData = Base64.encode(\$('#idlist').val()).escapeChars(charsThatMustBeEscaped);
            
            var vals = 'replicatetype='+replTypeId
                +';idlist='+   encodedData
                +';migrationDirectory='+ \$('#migrationDirectory').val().escapeChars(charsThatMustBeEscaped)
                +';targetDirectory='+\$('#targetDirectory').val().escapeChars(charsThatMustBeEscaped)
                +';defaultRights='+defaultRights
                +';startIndexer='+startIndexer
                +';ingestSkip='+ingestSkip;
    
                
            var url = "lr?action=form_post&def=k3_replication&paramsMapping="+encodeURI("{")+vals+encodeURI("}")+"&out=text";
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

<!-- vyber adresaru -->
<fieldset style="margin-top:10px;border: 1px solid gray;">
    <legend style="border:none">$bundle.("parametrizedconvert.directories.fields")$</legend>

    <label>$bundle.("k3replication.migration.directory")$</label><br>
    <input style="width:80%" type="text"  id="migrationDirectory" value="$migrationDirectory$"></input>
    <input type="button" onclick="trees.openImportTree();" value="$bundle.("k3replication.directory.selection")$"></input>
    <br>
    
    <label>$bundle.("k3replication.target.directory")$</label><br>
    <input style="width:80%" type="text"  id="targetDirectory" value="$targetDirectory$"></input>
    <input type="button" onclick="trees.openTargetTree();" value="$bundle.("k3replication.directory.selection")$"></input>
</fieldset>


<!-- periodical/monograph; udelat dle nastaveni -->
<fieldset style="margin-top:10px;border: 1px solid gray;">
    <legend style="border:none">$bundle.("k3replication.type")$</legend>
    <input type="radio" name="repltype" id="replTypeMonograph" value="monograph" checked="checked">$bundle.("k3replication.type.monographs")$</input>
    <input type="radio" name="repltype" id="replTypePeriodical" value="periodical">$bundle.("k3replication.type.periodicals")$</input>
</fieldset>

<!-- public/private; udelat dle nastaveni -->
<fieldset style="margin-top:10px;border: 1px solid gray;">
    <legend style="border:none">$bundle.("k3replication.defaultrights.fields")$</legend>
    $if(visibility)$
    <input type="radio" id="defaultRightsPublic" name="defaultRights" value="$bundle.("k3replication.defaultrights.public")$" checked>$bundle.("k3replication.defaultrights.public")$</input>
    <input type="radio" name="defaultRights" value="$bundle.("k3replication.defaultrights.private")$">$bundle.("k3replication.defaultrights.private")$</input>
    $else$
    <input type="radio" id="defaultRightsPublic" name="defaultRights" value="$bundle.("k3replication.defaultrights.public")$">$bundle.("k3replication.defaultrights.public")$</input>
    <input type="radio" name="defaultRights" value="$bundle.("k3replication.defaultrights.private")$" checked>$bundle.("k3replication.defaultrights.private")$</input>
    $endif$
</fieldset>

<!-- idlist -->
<fieldset style="margin-top:10px;border: 1px solid gray;">
    <legend style="border:none">$bundle.("k3replication.idlist")$</legend>
    <textarea id="idlist" rows="5" style="width:100%;"></textarea>
</fieldset>

<!-- ostatni nastaveni  -->
$(otherSettingsTemplate)()$


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