function _k4Settings(){
    $.ajaxSetup({
	statusCode: {
		403: function() {
			window.location="./?error=accessdenied";	
		}
	},
	cache:false
    });
    this.currentSelectedPage = "";
    this.activeUuids = [];
    this.activeUuid = null;
    this.activePidPath = null;
    this.selectedPath = [];
    this.appUrl = '';
}

var k4Settings = new _k4Settings();

//TODO: delete

var openMockDialog = null;
function openMockAction() {
	$.get("lr?action=form_get&def=wmock", function(data){

		if (this.openMockDialog) {
            this.openMockDialog.dialog('open');
    	} else {
            var pdiv = '<div id="wmock"></div>';
            $(document.body).append(pdiv);

            this.openMockDialog = $("#wmock").dialog({
                bgiframe: true,
                width:  400,
                height:  200,
                modal: true,
                title: '',
                buttons: [
                    {
                        text: 'Process',
                        click: function() {
                        	window.onProcessFormSend();
                            $(this).dialog("close"); 
                        }
                    },
                    {
                        text: dictionary["common.close"],
                        click:function() {
                            $(this).dialog("close"); 
                        } 
                    }
                ]
                    
            });

    	}

		$("#wmock").html(data);
    });	
}


