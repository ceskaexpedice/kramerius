/**
 * Zoomify viewer object
 * @returns {ZoomifyViewerInitObject}
 */
function ZoomifyViewerInitObject() {
    this.identification = "Mark";
    this.flag = false;    
    
    this.zoomify = null;
    this.image = null;
    this.overview = null;
    this.vectorLayer = null;
    
    this.zoomIn = null;
    this.zoomOut = null;
}


ZoomifyViewerInitObject.prototype.isInitialized = function() {
    return this.flag;
}

ZoomifyViewerInitObject.prototype.init = function() {
    this.flag = true;
}

ZoomifyViewerInitObject.prototype.highlightAlto=function(altoObject) {
    //{"image":{"HEIGHT":3232,"WIDTH":2515},"box":{"VPOS":292,"HEIGHT":27,"HPOS":2070,"WIDTH":108}}
    var pointList = [];        
    pointList.push(new OpenLayers.Geometry.Point(altoObject.box.HPOS, altoObject.image.HEIGHT - altoObject.box.VPOS));   
    pointList.push(new OpenLayers.Geometry.Point(altoObject.box.HPOS + altoObject.box.WIDTH, altoObject.image.HEIGHT - altoObject.box.VPOS));
    pointList.push(new OpenLayers.Geometry.Point(altoObject.box.HPOS + altoObject.box.WIDTH, altoObject.image.HEIGHT - altoObject.box.HEIGHT - altoObject.box.VPOS));     
    pointList.push(new OpenLayers.Geometry.Point(altoObject.box.HPOS, altoObject.image.HEIGHT - altoObject.box.HEIGHT - altoObject.box.VPOS));
    var linearRing = new OpenLayers.Geometry.LinearRing(pointList);
    var polygonFeature = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon([linearRing]));
    this.vectorLayer.addFeatures([polygonFeature]);  
}

ZoomifyViewerInitObject.prototype.clearAlto=function() {
    if (this.vectorLayer) this.vectorLayer.removeAllFeatures();
}

ZoomifyViewerInitObject.prototype.open = function(pid,altoObject) {
    this.clearAlto();
    $('#ol-image').remove(); $('#ol-overview').remove();
    $('#ol-wrapper').html('<div id=\"ol-image\" style=\"width: 100%; height: 100%\"></div>');
    
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
        this.map = new OpenLayers.Map("ol-image", {
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
            if (altoObject.box && altoObject.image) {
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

var zoomInit = new ZoomifyViewerInitObject();