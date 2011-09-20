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

