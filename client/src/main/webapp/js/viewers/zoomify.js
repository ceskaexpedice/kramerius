/**
 * Zoomify viewer   
 * @constructor
 * @param {Application} application - The application instance  {@link Application}
 * @param {string} jquery selector for the container
 * @param {string} zoomify url 
 */
function Zoomify(appl, selector) {
        this.application = (appl || K5);
        var v = K5.api.ctx.item.selected;
        this.url = K5.api.ctx.item[v].zoom.url;

        var jqSel = (selector || '#viewer>div.container>div.ol');        
        this.elem = $(jqSel);

        
        this.projection = null;
        this.src = null;
        this.map = null;        
        this.tile = null;
        this.view2D = null;

        this.disposed = false;

        this.alto = new Alto();
        
        this.resizeHandler = _.bind(function(type, configuration) {
                if (type =="window/resized") {
                        if (this.projection && this.map && this.view2D) {
                                var ext = this.projection.getExtent();
                                var size = this.map.getSize();
                                var resolution =  this.view2D.getResolution();

                                var curWidth = ext[2]/resolution ;
                                var curHeight = ext[3]/resolution ;

                                if (curWidth < size[0] && curHeight < size[1]) {
                                        //NOTE: timeout because of tablet delay                                
                                        setTimeout(function() { K5.gui.selected.fit();  _checkArrows(); }, 500);
                                } 
                        }
                }
        },this);        

        this.application.eventsHandler.addHandler(this.resizeHandler);
}






Zoomify.prototype.repairurl = function(url) {
        var changedurl = url.replace("deepZoom","zoomify");
        var index = changedurl.indexOf("zoomify");
        return changedurl.substring(index,changedurl.length);
}


/** 
 * Open image 
 * @method
 */
Zoomify.prototype.open = function() {
    this.chnagedurl = this.repairurl(this.url);   

    this.elem.append(_leftNavigationArrow());    
    this.elem.append(_rightNavigationArrow());    

    var optionsDiv = _optionspane();
    this.elem.append(_optionspane());    
    
    
    /*
    if(isTouchDevice()){
        this.elem.swipe({
            swipeLeft: function(event, direction, distance, duration, fingerCount) {
                K5.gui.selected.next();
            },
            swipeRight: function(event, direction, distance, duration, fingerCount) {
                K5.gui.selected.prev();
            },
            maxTimeThreshold:200,
            threshold:5,
            triggerOnTouchEnd:true
        });
    }*/

    
    var mapDiv = $("<div/>",{"id":"map","width":"100%","height":"100%"});

    this.elem.append(mapDiv);    

    $.ajax(this.chnagedurl+"/ImageProperties.xml")
        .fail(_.bind(function(jqXHR, textStatus) {
                var rect = [$("#map").width(), $("#map").height()];                
                $("#map").remove();
                K5.gui.selected.forbidden(rect);
        },this)).success(_.bind(function(data,textStatus,jqXHR) {
                var url = this.repairurl(this.url)+"/";
                console.log("open url "+url);    
                function resolutions(numberOfTiles) {
                    var resolutions = [];
                    for(var i = numberOfTiles-1; i>=0; i--) {resolutions.push(Math.pow(2, i));}       
                    return resolutions;
                }

                var width = $(data).find("IMAGE_PROPERTIES").attr("WIDTH");
                var height = $(data).find("IMAGE_PROPERTIES").attr("HEIGHT");
                var imgCenter = [width / 2, - height / 2];
                
                var source = new ol.source.Vector();

                this.projection = new ol.proj.Projection({
                        code: 'ZOOMIFY',
                        units: 'pixels',
                        extent: [0, 0, width, height]
                });

                this.src = new ol.source.Zoomify({
                        url: url,
                        size: [width, height],
                        crossOrigin: 'anonymous'
                });
        
                this.tile = new ol.layer.Tile({ source: this.src });

                this.view2D =  new ol.View2D({
                        projection: this.projection,
                        center: imgCenter,
                        zoom: 1
                });              

                var rend = _olrenderer();
                
                this.map = new ol.Map({
                        layers: [this.tile],
                        renderer:rend,
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
                        if (!this.alto.altoInitialized) {
                                this.alto.init(this);
                        }
                        this.alto.markers(this);                       
                },this));



                // animation only first page on the title
                var anim = false;                
                var callhistory = this.application.api.ctx.callhistory;
                if (callhistory.length >= 2) {

                        var previous = callhistory[callhistory.length-2];
                        var current = callhistory[callhistory.length-1];

                        var previousParents = _.reduce(this.application.api.ctx.item[previous].context, function(memo, value, index) {
                                if (value.length >= 2) {
                                        memo.push(value[value.length-2]);
                                }
                                return memo;
                        }, []);

                        var currentParents = _.reduce(this.application.api.ctx.item[previous].context, function(memo, value, index) {
                                if (value.length >= 2) {
                                        memo.push(value[value.length-2]);
                                }
                                return memo;
                        }, []);
                                                
                        var more = previousParents.length >  currentParents.length ? previousParents : currentParents;
                        var less = previousParents.length <=  currentParents.length ? previousParents : currentParents;
                        var found = false;
                        for(var i = 0; i<more.length; i++) {
                                if (_.contains(less, more[i])) {
                                     found=true;
                                     break;                                                   
                                }
                        }       
                        anim = !found;
                        
                } else {
                        anim = true;
                }
                
                if (anim) {
                        var anim = ol.animation.zoom({
                                resolution:this.map.getView().getResolution()
                        });
                        this.map.beforeRender(anim);    
                }

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
        },this));

}


/**
 * Clear container - remove all elements created by this viewer
 * @method
 */
Zoomify.prototype.clearContainer = function() {

        this.alto.clear(this);
        if (this.map != null) this.map.setTarget();

        
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

Zoomify.prototype.lockZoom = function() {
    _lockZoomAndStore(this.map);
}
Zoomify.prototype.unlockZoom = function() {
    _unlockZoomAndStore(this.map);
}

Zoomify.prototype.hideLeftrightbuttons = function() {
        $("#pageright").hide();
        $("#pageleft").hide();
}
Zoomify.prototype.showLeftrightbuttons = function() {
        $("#pageright").show();
        $("#pageleft").show();
}

Zoomify.prototype.addContextButtons=  function() {
    _ctxbuttonsrefresh();
}




Zoomify.prototype.translateCurrent=function(x1,y1,width,height) {
    var curpage = this.currentPage();
    var resolution = this.map.getView().getResolution();
    var x1off = x1/resolution;
    var y1off = y1/resolution;
    var widthoff = width/resolution;
    var heightoff = height/resolution;
    var result = [curpage[0] + x1off, curpage[1] + y1off, curpage[0]+ x1off+widthoff, curpage[1]+ y1off+heightoff];
    return result;

}

Zoomify.prototype.currentSize = function(ext, resolution) {
    return [ext[2]/resolution, ext[3]/resolution];         
}

Zoomify.prototype.idealCenter = function(ext) {
    return [ext[2]/2, (-1*ext[3])/2];         
 }


Zoomify.prototype.currentPage=function() {
        var center = this.view2D.getCenter();   
        K5.serverLog("Center is :"+center +" - nezmenseny center");
        
        var ideal = this.idealCenter(this.projection.getExtent());
        K5.serverLog("Ideal center "+ideal +" - nezmenseny ideal");
        
        var xoffset = center[0] - ideal[0]; //- smer doprava; + smer doleva
        var yoffset = center[1] - ideal[1]; // - smer nahoru; +smer dolu
        

        K5.serverLog("Posunuti (realne) "+xoffset +","+yoffset);
        
        
        var resolution = this.map.getView().getResolution();
        K5.serverLog("Resolution is "+resolution);
        var pixelxoffset = xoffset / resolution;
        var pixelyoffset = yoffset / resolution;

        var currentSize = this.currentSize(this.projection.getExtent(), resolution);
        var mapCenter = [$("#map").width()/2, $("#map").height()/2];

        var x1ideal = mapCenter[0]- (currentSize[0]/2);   
        var y1ideal = mapCenter[1]- (currentSize[1]/2);   

        $('#viewer>div.container').append('<div id="idealcenter" style="border:2px solid red;position:absolute;">');
        //return [x1real, y1real, x2real, y2real];

        $("#idealcenter").css('left',(x1ideal+100)+"px");
        $("#idealcenter").css('top',(y1ideal+100)+'px');
        $("#idealcenter").css('width',"200px");
        $("#idealcenter").css('height',"200px");

        
        // realny obrazek na obrazovce
        var x1real = x1ideal-pixelxoffset;   
        var y1real = y1ideal+pixelyoffset;   

        var x2real = x1real+currentSize[0];   
        var y2real = y1real+currentSize[1];   

        return [x1real, y1real, x2real, y2real];
}

Zoomify.prototype.crop = function(rect,offset){

        $("#header").show();
        $("#pageright").show();
        $("#pageleft").show();
        /*
        function idealCenter (ext) {
                 return [ext[2]/2, (-1*ext[3])/2];         
        }
        function curentSize(ext, resolution) {
                 return [ext[2]/resolution, ext[3]/resolution];         
        }*/

        var ideal = this.idealCenter(this.projection.getExtent());

        var center = this.view2D.getCenter();   
        var xoffset = center[0] - ideal[0]; //- smer doprava; + smer doleva
        var yoffset = center[1] - ideal[1]; // - smer nahoru; +smer dolu

        var resolution = this.map.getView().getResolution();
        var pixelxoffset = xoffset / resolution;
        var pixelyoffset = yoffset / resolution;
        
        
        var currentSize = this.currentSize(this.projection.getExtent(), resolution);
        var mapCenter = [$("#map").width()/2, $("#map").height()/2];

        var x1ideal = mapCenter[0]- (currentSize[0]/2);   
        var y1ideal = mapCenter[1]- (currentSize[1]/2);   

        // realny obrazek na obrazovce
        var x1real = x1ideal-pixelxoffset;   
        var y1real = y1ideal+pixelyoffset;   

        var x2real = x1real+currentSize[0];   
        var y2real = y1real+currentSize[1];   


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
 * Make current image to fit to the current space  
 *@method
 */
Zoomify.prototype.fit = function() {
    var ext = this.projection.getExtent();
    var size = this.map.getSize();
    var nresolution = ext[3]/(size[1]-20);
    this.view2D.setResolution(nresolution);
    this.view2D.setCenter([ext[2]/2, -ext[3]/2]);
}


Zoomify.prototype.relativePosition = function() {
        return $("#map").position();        
}


Zoomify.prototype.rotateLeft = function() {
    this.alto.clear(this);
    _rotateLeft(this.map);
}

Zoomify.prototype.rotateRight = function() {
    this.alto.clear(this);
    _rotateRight(this.map);
}


/**
 * Zooming out
 * @method       
 */
Zoomify.prototype.zoomOut = function() {
    _zoomOut(this.map);
}

/**
 * Zooming in
 * @method       
 */
Zoomify.prototype.zoomIn = function() {
    _zoomIn(this.map);
}


/**
 * Creates alto marker
 * @method       
 */
Zoomify.prototype.createMarker=function(ident,xpos,ypos) {
        var marker = new ol.Overlay({
                position: [xpos,-ypos],
                positioning: 'top-left',
                element: document.getElementById(ident),
                stopEvent: false
        });
        return marker;
}


/**
 * Retrurs true if the current viewer can be used
 * @method       
 */
Zoomify.prototype.isEnabled= function(data) {
        var datanode = data["datanode"];
        var zoom = data["zoom"];
        return  (datanode  && zoom);
}


/**
 * Check forbidden message
 * @method       
 */
Zoomify.prototype.forbiddenCheck = function(okFunc, failFunc) {
        var v = K5.api.ctx.item.selected;
        var url = K5.api.ctx.item[v].zoom.url;
        var changedurl = Zoomify.prototype.repairurl(url);
        $.ajax(changedurl+"/ImageProperties.xml").fail(_.bind(function(jqXHR, textStatus) {
                failFunc.apply(null, []);
        },this)).success(function() {
                okFunc.apply(null, []);
        });
}

Zoomify.prototype.selectionStartNotif = function() {
    $("#options").hide();
}

Zoomify.prototype.selectionEndNotif = function() {
    $("#options").show();
}


Zoomify.prototype.containsLeftStructure = function() {
    return true;
}
