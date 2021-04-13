/** sablony pro mock process */
group mockwizard;


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




form(rootNode) ::=<<

<div>
    <script language="JavaScript" type="text/javascript"> 
    <!--    

        window.onProcessFormSend = function() {
            var vals = 'inputFolder='+\$('#inputFolder').val()
                        +';processName='+\$('#processName').val();
            
            var url = "lr?action=form_post&def=wmock&paramsMapping="+encodeURI("{")+vals+encodeURI("}")+"&out=text";
            processStarter("wmock").start(url);
                                    
        }

        \$(function () {
         
            \$("#foldertree").jstree({ 
                "plugins" : [ "themes", "html_data", "ui" ]
            })
            .bind("select_node.jstree", function (event, data) { 
                var p = data.rslt.obj.attr("id");
                \$("#inputFolder").val(p);
            });
        });
    -->        
    </script>


        <table style="width:100%;">
            <tr>
                <td>
                    <div id="form">
                        <form>
                            <label>Vybrany adresar:</label><br>
                            <input type="text" size="23" id="inputFolder" value=""></input><br>
                            <label>Nazev:</label><br>
                            <input type="text" size="23" id="processName"></input>
                        </form>
                    </div>
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