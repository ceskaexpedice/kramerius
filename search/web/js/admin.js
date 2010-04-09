/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
var processDialog;

//
//var lastStartedProcess=function() {
//	return {
//		state:""
//	}
//}();
	


function generateStatic(level){
	var pid = $("#tabs_"+level).attr('pid');
	var url = "lr?action=start&def=static_export&out=text&params="+pid;
    $.get(url, function(data) {
    	$("#processState").html("Stav procesu "+data);
    	var dialog = $("#process_started").dialog({
            bgiframe: true,
            width: 400,
            height: 100,
            modal: true,
            title: "Spusteni procesu 'Statický export'",
            
            buttons: {
                "Close": function() {
                    $(this).dialog("close"); 
                } 
            } 
              
        });
    	//alert(dialog);
	});

}

