
/**
 * Viewer for plain images (JPEG, PNG, GIF)    
 * @constructor
 * @param {Application} application - The application instance  {@link Application}.
 * @param {string} jquery selector for the container.
 * @lends ItemSupport# 
 */
function ZoomifyStaticImage(appl, selector) {
        this.application = (appl || K5);
        
        this.url = this.makeStaticURL(this.application.api.ctx["item"]["selected"]);
        
        var jqSel = (selector || '#viewer>div.container>div.ol');        
        this.elem = $(jqSel);
        
        this._tmpimage = new Image();
        
        this._tmpimageWidth =0;
        this._tmpimageHeight =0;
        
        this.projection = null;
        this.imagelayer = null;
        this.map = null;        
        this.tile = null;
        this.view2D = null;
        
        /** @member */
        this.alto = new Alto();

        this.disposed = false;
        
        
        this.application.eventsHandler.addHandler(_.bind(function(type, configuration) {
                if (type =="window/resized") {
                        if (this.projection && this.map && this.view2D) {
                                var ext = this.projection.getExtent();
                                var size = this.map.getSize();
                                var resolution =  this.view2D.getResolution();

                                var curWidth = ext[2]/resolution ;
                                var curHeight = ext[3]/resolution ;

                                if (curWidth < size[0] && curHeight < size[1]) {
                                        setTimeout(function() { K5.gui.selected.fit();  _checkArrows(); }, 500);
                                } 
                        }
                }
        },this));

}

ZoomifyStaticImage.prototype.makeStaticURL=function(pid) {
        function  _djvu(mtype) {if (mtype) { return mtype.indexOf('djvu')> 0; } else { return false; } }
        var item = K5.api.ctx.item[pid];
        if ((item.streams) && (item.streams.IMG_FULL) && (_djvu(item.streams.IMG_FULL.mimeType))) {
                var staticurl = 'img?pid=' + pid + '&stream=IMG_FULL&action=TRANSCODE';
                return staticurl;
        } else {
                var staticurl = 'api/item/' + pid + '/full';
                return staticurl;
        }       
} 

ZoomifyStaticImage.prototype.prefetchNextAndPrev = function () {
    if (K5.api.isKeyReady("item/selected") && (K5.api.isKeyReady("item/"+K5.api.ctx.item.selected+"/siblings"))) {
            var arr = K5.api.ctx.item[K5.api.ctx.item.selected].siblings[0]['siblings'];
            var index = _.reduce(arr, function(memo, value, index) {
                return (value.selected) ? index : memo;
            }, -1);
            if (index >= 0) { 
                var img = new Image();
                img.src=this.makeStaticURL(arr[index-1].pid);
            } 
            if (index < arr.length-1) {
                var img = new Image();
                img.src=this.makeStaticURL(arr[index+1].pid);
            }
        } else {
                K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], _.bind(function(data) {
                    var arr = data[0]['siblings'];
                    var index = _.reduce(arr, function(memo, value, index) {
                        return (value.selected) ? index : memo;
                    }, -1);
                    if (index >= 0) { 
                        var img = new Image();
                        img.src=this.makeStaticURL(arr[index-1].pid);
                    } 
                    if (index < arr.length-1) {
                        var img = new Image();
                        img.src=this.makeStaticURL(arr[index+1].pid);
                    }
                },this));
        }
}


/** 
 * Open image 
 * @method
 */
ZoomifyStaticImage.prototype.open = function() {


        this.elem.append(_leftNavigationArrow());    
        this.elem.append(_rightNavigationArrow());    

        var optionsDiv = _optionspane();
        if ($("#options").length > 0) {
            $("#options").remove();
        }
        this.elem.append(optionsDiv);


        var mapDiv = $("<div/>",{"id":"map"});
        mapDiv.css('width','100%');            
        mapDiv.css('height','100%');            
        this.elem.append(mapDiv);    

    
        this._tmpimage.onload = _.bind(function() {
                
                //this.prefetchNextAndPrev();
                
                var width = this._tmpimage.width;                
                var height = this._tmpimage.height;                

                this.projection = new ol.proj.Projection({
                        code: 'pixel',
                        units: 'pixels',
                        extent: [0, 0, width, height]
                });
                this.imagelayer = new ol.layer.Image({
                        source: new ol.source.ImageStatic({
                                url: this.url,
                                imageSize: [width, height],
                                projection: this.projection,
                                imageExtent: this.projection.getExtent()
                        })
                });

                this.view2D =  new ol.View2D({
                        projection: this.projection,
                        center: ol.extent.getCenter(this.projection.getExtent()),
                        zoom: 1
                });


                this.map = new ol.Map({
                        layers: [this.imagelayer],
                        renderer: 'dom',
                        target: 'map',
                        interactions:ol.interaction.defaults({
                                pinchRotate:false,
                                altShiftDragRotate:false,
                        }),
                        
                        controls: ol.control.defaults({logo:false, zoom:false}).extend([
                            new ol.interaction.KeyboardZoom()
                        ]),
                        view: this.view2D
                });
        

                this.map.on('moveend',function() {
                    /*
                    var curpage = K5.gui.selected.currentPage();
                    if (curpage[2] < 0) {
                        K5.gui.selected.next();
                    }
                    
                    var wdth = $("#map").width();
                    if (curpage[0] > wdth) {
                        K5.gui.selected.prev();
                    }*/
                    
                });

                this.map.on('postrender', _.bind(function(evt) {
                    if (!this.disposed) {
                        if (!this.alto.altoInitialized) {
                            this.alto.init(this);
                        }
                        this.alto.markers(this);                       
                    }
                },this));

                var lockedZoom = _checkZoomIsLocked(this.map);
                if (lockedZoom) {
                    _optionspaneLocked("buttons.zoomunlock");
                } else {
                    var ext = this.projection.getExtent();
                    var size = this.map.getSize();
                    var nresolution = ext[3]/(size[1]-20);
                    this.map.getView().setResolution(nresolution);
                }
                _checkArrows();

            },this);

    $(this._tmpimage).error(_.bind(function(eventData) {
        var rect = [$("#map").width(), $("#map").height()];                
        $("#map").remove();
        this.disabledDisplay = true;
    },this));    

    this._tmpimage.src=this.url;
    

}




/**
 * Lock zoom 
 * @method
 */
ZoomifyStaticImage.prototype.lockZoom = function() {
    _lockZoomAndStore(this.map);
}


ZoomifyStaticImage.prototype.unlockZoom = function() {
    _unlockZoomAndStore(this.map);
}


ZoomifyStaticImage.prototype.hideLeftrightbuttons = function() {
        $("#pageright").hide();
        $("#pageleft").hide();
}
ZoomifyStaticImage.prototype.showLeftrightbuttons = function() {
        $("#pageright").show();
        $("#pageleft").show();
}



ZoomifyStaticImage.prototype.addContextButtons=  function() {
    _ctxbuttonsrefresh();
}


ZoomifyStaticImage.prototype.relativePosition = function() {
        // return $("#map").position();        
        return $("#map").offset();

}

ZoomifyStaticImage.prototype.rotateLeft = function() {
    this.alto.clear(this);
    _rotateLeft(this.map);
}

ZoomifyStaticImage.prototype.rotateRight = function() {
    this.alto.clear(this);
    _rotateRight(this.map);
}




/**
 * Make current image to fit to the current space  
 * @method
 */
ZoomifyStaticImage.prototype.fit = function() {
    var ext = this.projection.getExtent();
    var size = this.map.getSize();
    this.view2D.fitExtent(ext,size);
}

ZoomifyStaticImage.prototype.translateCurrent=function(x1,y1,width,height) {
    var curpage = this.currentPage();
    var resolution = this.map.getView().getResolution();
    var x1off = x1/resolution;
    var y1off = y1/resolution;
    var widthoff = width/resolution;
    var heightoff = height/resolution;
    var result = [curpage[0] + x1off, curpage[1] + y1off, curpage[0]+ x1off+widthoff, curpage[1]+ y1off+heightoff];
    return result;
}

ZoomifyStaticImage.prototype.idealCenter = function (ext) {
    return [ext[2]/2, ext[3]/2];
}

ZoomifyStaticImage.prototype.curentSize = function (ext, resolution) {
    return [ext[2]/resolution, ext[3]/resolution];
}

ZoomifyStaticImage.prototype.currentPage = function() {

        var ideal = this.idealCenter(this.projection.getExtent());
        var center = this.view2D.getCenter();   

        var xoffset = center[0] - ideal[0]; //+ smer doprava; - smer doleva
        var yoffset = center[1] - ideal[1]; // - smer nahoru; +smer dolu

        var resolution = this.map.getView().getResolution();
        var pixelxoffset = xoffset / resolution;
        var pixelyoffset = yoffset / resolution;
        
        
        var currentSize = this.curentSize(this.projection.getExtent(), resolution);
        var mapCenter = [$("#map").width()/2, $("#map").height()/2];

        var x1ideal = mapCenter[0]- (currentSize[0]/2);   
        var y1ideal = mapCenter[1]- (currentSize[1]/2);   

        // realny obrazek na obrazovce
        var x1real = x1ideal-pixelxoffset;   
        var y1real = y1ideal+pixelyoffset;   

        var x2real = x1real+currentSize[0];   
        var y2real = y1real+currentSize[1];   

        return [x1real, y1real, x2real, y2real];
}

ZoomifyStaticImage.prototype.crop = function(rect, offset){

        function idealCenter (ext) {
                 return [ext[2]/2, ext[3]/2];         
        }
        function curentSize(ext, resolution) {
                 return [ext[2]/resolution, ext[3]/resolution];         
        }
        var ideal = idealCenter(this.projection.getExtent());

        var center = this.view2D.getCenter();   
        var xoffset = center[0] - ideal[0]; //+ smer doprava; - smer doleva
        var yoffset = center[1] - ideal[1]; // - smer nahoru; +smer dolu

        var resolution = this.map.getView().getResolution();
        var pixelxoffset = xoffset / resolution;
        var pixelyoffset = yoffset / resolution;
        
        
        var currentSize = curentSize(this.projection.getExtent(), resolution);
        var mapCenter = [$("#map").width()/2, $("#map").height()/2];

        var x1ideal = mapCenter[0]- (currentSize[0]/2);   
        var y1ideal = mapCenter[1]- (currentSize[1]/2);   

        // realny obrazek na obrazovce
        var x1real = x1ideal-pixelxoffset;   
        var y1real = y1ideal+pixelyoffset;   

        var x2real = x1real+currentSize[0];   
        var y2real = y1real+currentSize[1];   


        // posunuta uroven vyberu oproti mape
        var layersXOffset = $("#map").offset().left - offset[0];
        var layersYOffset = $("#map").offset().top - offset[1]; 
        

        var refferenceDisect =  [rect[0]-x1real-layersXOffset, rect[1] - y1real-layersYOffset, rect[2]-x1real-layersXOffset, rect[3]-y1real-layersYOffset];

        var percentageDisect = [refferenceDisect[0]/currentSize[0], 
                                refferenceDisect[1]/currentSize[1],

                                (refferenceDisect[2]- refferenceDisect[0])/currentSize[0],
                                (refferenceDisect[3] - refferenceDisect[1])/currentSize[1]];



        
        window.open('part?pid='+K5.api.ctx.item.selected+
                        "&xpos="+percentageDisect[0]+
                        "&ypos="+percentageDisect[1]+
                        "&width="+percentageDisect[2]+
                        "&height="+percentageDisect[3], '_blank');


}


/**
 * Zooming out
 * @method
 */
ZoomifyStaticImage.prototype.zoomOut = function() {
    _zoomOut(this.map);
}

/**
 * Zooming in
 * @method
 */
ZoomifyStaticImage.prototype.zoomIn = function() {
    _zoomIn(this.map);
}

/**
 * Dispose current viewer
 * @method       
 */
ZoomifyStaticImage.prototype.clearContainer = function() {

        this.alto.clear(this);
        if (this.map) this.map.setTarget();
 
        
        $("#options").remove();
        $("#map").remove();
        $("#pageleft").remove();
        $("#pageright").remove();

        this.application.eventsHandler.removeHandler(this.resizeHandler);

        this.projection = null;
        this.src = null;
        this.map = null;        
        this.tile = null;
        this.view2D = null;

        this.disposed = true;

}


/**
 * Creates alto marker
 * @method       
 */
ZoomifyStaticImage.prototype.createMarker=function(ident,xpos,ypos) {

        function curentSize(ext, resolution) {
                 return [ext[2]/resolution, ext[3]/resolution];         
        } 
 
        var resolution = this.view2D.getResolution();
        var currentSize = curentSize(this.projection.getExtent(), resolution);
      
        var marker = new ol.Overlay({
                position: [xpos, this.projection.getExtent()[3] - ypos],
                //position: [xpos,0],
                positioning: 'top-left',
                element: document.getElementById(ident),
                stopEvent: false
        });
        return marker;
}


ZoomifyStaticImage.prototype.isEnabled= function(data) {
        var datanode = data["datanode"];
        var zoom = data["zoom"];
        var pdf = data["pdf"];
        return  (datanode  && ((!zoom) && (!pdf)));
}


ZoomifyStaticImage.prototype.forbiddenCheck = function(okFunc, failFunc) {
        var v = K5.api.ctx.item.selected;
        var imgurl = ZoomifyStaticImage.prototype.makeStaticURL(v);
        $.ajax(imgurl) .fail(_.bind(function(jqXHR, textStatus) {
                failFunc.apply(null, []);
        },this)).success(function() {
                okFunc.apply(null, []);
        });
}





ZoomifyStaticImage.prototype.selectionStartNotif = function() {
    $("#options").hide();
}

ZoomifyStaticImage.prototype.selectionEndNotif = function() {
    $("#options").show();
}

ZoomifyStaticImage.prototype.containsLeftStructure = function() {
    return true;
}
