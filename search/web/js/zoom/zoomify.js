function ZoomifyViewerInitObject() {
    this.identification = "Mark";
    this.flag = false;    
    
    this.zoomify = null;
    this.image = null;
    this.overview = null;
    this.vectorLayer = null;
}


ZoomifyViewerInitObject.prototype.isInitialized = function() {
    return this.flag;
}

ZoomifyViewerInitObject.prototype.init = function() {
    this.flag = true;
}


ZoomifyViewerInitObject.prototype.open = function(pid) {
    $('#ol-image').remove(); $('#ol-overview').remove();
    $('#ol-wrapper').html('<div id=\"ol-image\" style=\"width: 100%; height: 100%\"></div><div id=\"ol-overview\"></div>');
    
    $.get('zoomify/'+pid+'/ImageProperties.xml', bind(function(data) {
        
        var url = "zoomify/"+pid+"/";
        
        function resolutions(numberOfTiles) {
            var resolutions = [];
            for(var i = numberOfTiles-1; i>=0; i--) {resolutions.push(Math.pow(2, i));}       
            return resolutions;
        }

        var width = $(data).find("IMAGE_PROPERTIES").attr("WIDTH");
        var height = $(data).find("IMAGE_PROPERTIES").attr("HEIGHT");

        
        
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

        this.overview = new OpenLayers.Control.OverviewMap({
                div:document.getElementById('ol-overview'),
                size: this.zoomify.tierImageSize[0], 
                autoPan:false,
                mapOptions: { numZoomLevels: 1 },
                isSuitableOverview : function() { return true; }
        }) ;

        this.map = new OpenLayers.Map("ol-image", {
            controls: [],
            resolutions: resolutions(this.zoomify.numberOfTiers),
            maxExtent: new OpenLayers.Bounds(0, 0, width, height),
            numZoomLevels: this.zoomify.numberOfTiers,
            units: 'pixels'
        });

        this.map.addLayer(this.zoomify);
        this.map.addLayer(this.image);    
        this.vectorLayer = new OpenLayers.Layer.Vector("Box");
        this.map.addLayer(this.vectorLayer);

        
        this.map.addControl(new OpenLayers.Control.Zoom());
        
        
        this.map.addControl(new OpenLayers.Control.KeyboardDefaults());
        this.map.addControl(this.overview);
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
        
    },this));
}

var zoomInit = new ZoomifyViewerInitObject();