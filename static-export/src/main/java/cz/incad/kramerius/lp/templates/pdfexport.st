/** sablony pro proces pdfexport */
group pdfexport;


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




form(rootNode, lang, country ) ::=<<

<div>
    <script language="JavaScript" type="text/javascript"> 
    <!--    

        window.onProcessFormSend = function() {
            //var vals = 'outputFolder='+\$('#outputFolder').val().replace()
            
             var escapedFolder = \$('#outputFolder').val().replaceAll("\\\\\\\\","/").replaceAll(":","\\\\:");
                       if (escapedFolder.startsWith("/")) {
                               escapedFolder = "/"+escapedFolder;
                       }

            var vals = 'outputFolder='+escapedFolder

                        +';lang='+\$('#lang').val()
                        +';country='+\$('#country').val()
                        +';pid='+\$('#pid').val()
                        +';imgUrl='+\$('#imgUrl').val()
                        +';i18nUrl='+\$('#i18nUrl').val();

                                        

            if (\$('#DVD').is(":checked")) { vals = vals + ';medium=DVD'; } else { vals = vals + ';medium=CD'; }
            var url = "lr?action=form_post&def=parametrized_static_export&paramsMapping={"+vals+"}&out=text";
            processStarter("parametrized_static_export").start(url);
        }
        
        
        window._pidsDisect = function() {
            
            var pids = map(function(pid) {
                  var divided = pid.split("_");
                  var structure = {
                          models:divided[0],
                          pid:divided[1]
                  };
                  return structure.pid.replaceAll(":","\\\\:");
            },getAffectedPids());
            
            var _v = reduce(function(base,element, status) {
                var retval = status.first ? "" : ";";
                retval = retval + element;  
                return retval;
            }, "",pids);
              
            \$("#pid").val(_v);
        }
        
        

        \$(function () {
         
            \$("#foldertree").jstree({ 
                "plugins" : [ "themes", "html_data", "ui" ]
            })
            .bind("select_node.jstree", function (event, data) { 
                var p = data.rslt.obj.attr("id");
                \$("#outputFolder").val(p);
            });
        });
        
        
        _pidsDisect();
    -->        
    </script>


        <table style="width:100%;">
            <tr>
                <td>
                    <input type="hidden" name="pid" id="pid" value=""></input>

                    <input type="hidden" name="imgUrl" id="imgUrl" value="img"></input>
                    <input type="hidden" name="i18nUrl" id="i18nUrl" value="i18"></input>

                    <input type="hidden" name="lang" id="lang" value="$lang$"></input>
                    <input type="hidden" name="country" id="country" value="$country$"></input>
                </td>
            </tr>
            <tr>
                <td>
                        <label>Medium:</label><br>
                        <input type="radio" id="DVD" value="DVD" name="medium">DVD</input>
                        <input type="radio" id="CD" value="CD" name="medium"  checked="checked">CD</input>
                </td>
            </tr>

            <tr><td><div style="border-bottom:1px solid black;"></div></td></tr>
            
            <tr>
                <td>
                    <label>Vystupni adresar:</label><br>
                    <input type="text" id="outputFolder" value="" style="width:100%"></input><br>
                </td>
            </tr>


            <tr>
                <td>
                    <div id="foldertree">                
                        <ul>
                            $treeNode(item=rootNode)$
                        </ul>
                    </div>        
                </td>
            </tr>
            
        </table>

    
</div>

>>