var k4Settings = new _k4Settings();
function _k4Settings(){
    $.ajaxSetup({
        cache: false
    });
    this.currentSelectedPage = "";
    this.activeUuids = [];
    this.activeUuids = null;
    this.selectedPath = [];
    this.appUrl = '';
}