function _k4Settings(){
    $.ajaxSetup({
	statusCode: {
		403: function() {
			window.location="./?error=accessdenied";	
		}
	},
	cache:false,
        contentType: "application/x-www-form-urlencoded; charset=UTF-8"
    });
    this.currentSelectedPage = "";
    this.activeUuids = [];
    this.activeUuid = null;
    this.activePidPath = null;
    this.selectedPath = [];
    this.appUrl = '';
}

var k4Settings = new _k4Settings();



