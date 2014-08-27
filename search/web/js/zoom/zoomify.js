/**
 * Zoomify viewer object
 * @returns {ZoomifyViewerInitObject}
 */
function ZoomifyViewerInitObject() {
    this.flag = false;    
    
    this.zoomify = null;
    this.image = null;
    this.overview = null;
    this.vectorLayer = null;
    
    this.zoomIn = null;
    this.zoomOut = null;
    this.rotate = null;
    
    this.divs = {
    		"ol-image":"ol-image",
    		"ol-wrapper":"ol-wrapper",
    		"ol-overview":"ol-overview"
    		
    };
}



/** returns true, if this objest is initialized */
ZoomifyViewerInitObject.prototype.isInitialized = function() {
    return this.flag;
}

/** initialization */
ZoomifyViewerInitObject.prototype.init = function(d) {
	this.divs = (d || this.divs);
	this.flag = true;
}


/** display alto */
ZoomifyViewerInitObject.prototype.highlightAlto=function(altoObject) {
    //{"image":{"HEIGHT":3232,"WIDTH":2515},"box":{"VPOS":292,"HEIGHT":27,"HPOS":2070,"WIDTH":108}}
    //{"image":{"width":1390,"height":2130},"boxes":[{"term":"prosa,","width":115,"height":28,"xpos":906,"ypos":649}]}
    var boxes = altoObject.boxes;        
    if (boxes.length > 0) {
            var pointList = [];        
            pointList.push(new OpenLayers.Geometry.Point(boxes[0].xpos, altoObject.image.height - boxes[0].ypos));   
            pointList.push(new OpenLayers.Geometry.Point(boxes[0].xpos + boxes[0].width, altoObject.image.height - boxes[0].ypos));
            pointList.push(new OpenLayers.Geometry.Point(boxes[0].xpos + boxes[0].width, altoObject.image.height - boxes[0].height - boxes[0].ypos));     
            pointList.push(new OpenLayers.Geometry.Point(boxes[0].xpos, altoObject.image.height - boxes[0].height - boxes[0].ypos));
            var linearRing = new OpenLayers.Geometry.LinearRing(pointList);
            var polygonFeature = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon([linearRing]));
            this.vectorLayer.addFeatures([polygonFeature]);  
    }    

}

/** clear alto */
ZoomifyViewerInitObject.prototype.clearAlto=function() {
    if (this.vectorLayer) this.vectorLayer.removeAllFeatures();
}

/** open image */
ZoomifyViewerInitObject.prototype.open = function(pid,altoObject) {
    this.clearAlto();
    $('#'+this.divs['ol-image']).remove();	
    $('#'+this.divs['ol-overview']).remove();	
    $('#'+this.divs['ol-wrapper']).html('<div id=\"'+this.divs['ol-image']+'\" style=\"width: 100%; height: 100%\"></div>');
    
    $.get('zoomify/'+pid+'/ImageProperties.xml', bind(function(data) {
        
        var url = "zoomify/"+pid+"/";
        
        function resolutions(numberOfTiles) {
            var resolutions = [];
            for(var i = numberOfTiles-1; i>=0; i--) {resolutions.push(Math.pow(2, i));}       
            return resolutions;
        }

        var width = $(data).find("IMAGE_PROPERTIES").attr("WIDTH");
        var height = $(data).find("IMAGE_PROPERTIES").attr("HEIGHT");

        var tileSize = $(data).find("IMAGE_PROPERTIES").attr("TILESIZE");
        
        this.zoomify = new OpenLayers.Layer.Zoomify("Zoomify", url, new OpenLayers.Size(width, height));        
        this.zoomify.transitionEffect = 'resize';    
        this.zoomify.isBaseLayer = false;    
        this.zoomify.data = ""
            
        this.image = new OpenLayers.Layer.Image('Image',
            url + "TileGroup0/0-0-0.jpg",
            new OpenLayers.Bounds(0, 0, width, height),
            this.zoomify.tierImageSize[0],
            { alwaysInRange: true }
        );


        var map_controls = [ /*new OpenLayers.Control.OverviewMap()*/ ];
        this.map = new OpenLayers.Map(this.divs["ol-image"], {
            controls: map_controls,
            resolutions: resolutions(this.zoomify.numberOfTiers),
            maxExtent: new OpenLayers.Bounds(0, 0, width, height),
            numZoomLevels: this.zoomify.numberOfTiers,
            //tileSize:512,
            units: 'pixels'
        });


        
        this.map.addLayer(this.zoomify);
        this.map.addLayer(this.image);    
        this.vectorLayer = new OpenLayers.Layer.Vector("Box");
        this.map.addLayer(this.vectorLayer);
        this.zoomIn = new OpenLayers.Control.ZoomIn();
        this.map.addControl(this.zoomIn);
        this.zoomOut = new OpenLayers.Control.ZoomOut();
        this.map.addControl(this.zoomOut);
        this.rotate = new OpenLayers.Control.Rotate();
        this.map.addControl(this.rotate);

        this.map.addControl(new OpenLayers.Control.KeyboardDefaults());
        this.map.addControl(new OpenLayers.Control.Navigation({
                                        mouseWheelOptions: {
                                            cumulative: false,
                                            interval: 0
                                        },
                                        dragPanOptions: {
                                            enableKinetic: true
                                        },
                                        zoomBoxEnabled: false,
                                        zoomWheelEnabled: true
                             }));
        
        this.map.zoomToMaxExtent();
        
        if (!(typeof altoObject === 'undefined')) {
            if (altoObject.boxes) {
                this.highlightAlto(altoObject);
            }
        }
        
    },this));
    
}

ZoomifyViewerInitObject.prototype.minus = function() {
    this.zoomOut.trigger();
}

ZoomifyViewerInitObject.prototype.plus = function() {
    this.zoomIn.trigger();
}

ZoomifyViewerInitObject.prototype.rotateImg = function() {
    this.rotate.trigger();
}


var zoomInit = new ZoomifyViewerInitObject();
