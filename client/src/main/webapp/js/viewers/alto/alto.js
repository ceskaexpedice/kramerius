/**
 * Represents alto support
 * @constructor
 */
function Alto() {
        this.altoData = null;
        this.altoMarkers=[];
        this.altoInitialized = false;
        
        this.oContainers = [];        
}

/**
 * Creates identifier
 * @param {string} term  Term which sould be highlighted .
 * @param {integer} index Index of identifier.
 * @method
 */
Alto.prototype.altoident = function(term, index) {
    var encoded = encodeURIComponent(term);
    encoded = encoded.replace(/%/g, "");
    return 'marker-'+encoded+'-'+index;
}


/**
 * Data received
 * @method
 */
Alto.prototype.receiveddata = function(data) {
        console.log("received data");
        this.altoData = data;
}

/**
 * Alto initialization 
 * @method
 */
Alto.prototype.init = function(zoomifyInstance) {
        if (this.altoInitialized) {
                console.log("already initialized");
                return;
        }
        if ((typeof K5.api.getParameterByName('q') != 'undefined') && (K5.api.getParameterByName('q') != null) && (!isEmpty(K5.api.getParameterByName('q')))){
               $.get("alto?pid="+K5.api.ctx.item.selected+"&q="+K5.api.getParameterByName('q'),_.bind(function(data) {
                        var ext = zoomifyInstance.projection.getExtent();
                        var size = zoomifyInstance.map.getSize();
                        var nresolution = ext[3]/(size[1]-20);

                        this.receiveddata(data);

                        for(var i in data) {
                                var resolution = zoomifyInstance.view2D.getResolution();

                                var boxes = data[i]["boxes"];
                                for(var j=0;j<boxes.length;j++) {
                                        var marker = $('<div/>', {"id": this.altoident(i,j)});
                                
                                        $(marker).css("border","3px solid rgba(0, 30, 60, 0.7)");
                                        $(marker).css("height",(boxes[j]["height"]/nresolution)+"px");
                                        $(marker).css("width",(boxes[j]["width"]/nresolution)+"px");
                                        

                                        $("#viewer").append(marker);
                                        //console.log($("#viewer").html());
                                        //this.oContainers.push(marker);
                                
                                        var marker = zoomifyInstance.createMarker(this.altoident(i,j),boxes[j]["xpos"], boxes[j]["ypos"]);

                                        this.altoMarkers.push(marker);
                                        zoomifyInstance.map.addOverlay(marker);
                                }
                        }
                                                                                                        
                },this));
               this.altoInitialized = true;

        }
}

/**
 * Changing width of the markers 
 * @method
 */
Alto.prototype.markers = function(zoomifyInstance) {
        if (zoomifyInstance.disposed) return;
        var resolution = zoomifyInstance.view2D.getResolution();
        if (this.altoData!=null) {
                console.log("changing width");
                for(var i in this.altoData) {
                        var boxes = this.altoData[i]["boxes"];
                        for (var j=0;j<boxes.length;j++) {
                                var height = boxes[j]["height"]/resolution;
                                var width = boxes[j]["width"]/resolution;
                                 
                                $('#'+this.altoident(i,j)).css("height",height+"px");
                                $('#'+this.altoident(i,j)).css("width",width+"px");
                        }                                        
                }
        }
}

/**
 * Clear markers 
 * @method
 */
Alto.prototype.clear = function(zoomifyInstance) {
        if (this.altoData!=null) {
                zoomifyInstance.map.getOverlays().clear();
                for(var i in this.altoData) {
                        var boxes = this.altoData[i]["boxes"];
                        for (var j=0;j<boxes.length;j++) {
                               $('#'+this.altoident(i,j)).remove();
                        } 
               }
        }
        this.altoMarkers = null;
        this.altoData = null;
}


