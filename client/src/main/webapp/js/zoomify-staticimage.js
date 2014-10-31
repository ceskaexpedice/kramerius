
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
        
        var jqSel = (selector || '#viewer>div.container');        
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
                                        setTimeout(function() { K5.gui.selected.fit();  K5.gui.selected.arrowbuttons(); }, 500);
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

        var leftArrowContainerDiv = $("<div/>",{"id":"pageleft","class":"leftarrow" });
        leftArrowContainerDiv.append($("<div/>",{"id":"pagelefticon", class:"arrow"}));    
        this.elem.append(leftArrowContainerDiv);    

        var rightArrowContainerDiv = $("<div/>",{"id":"pageright","class":"rightarrow"});
        rightArrowContainerDiv.append($("<div/>",{"id":"pagerighticon", class:"arrow"}));    
        this.elem.append(rightArrowContainerDiv);    

        $.get("svg.vm?svg=arrowleft",_.bind(function(data) {
                $("#pagelefticon").html(data);            
                this.arrowbuttons();
        },this));

        $.get("svg.vm?svg=arrowright",_.bind(function(data) {
                $("#pagerighticon").html(data);            
                this.arrowbuttons();
        },this));
 
        $("#pageleft").click(_.bind(function() {
                K5.gui.selected.prev();
        }, this));

        $("#pageright").click(_.bind(function() {
                K5.gui.selected.next();
        }, this));

        $("#pagelefticon").click(_.bind(function() {
                K5.gui.selected.next();
        }, this));

        $("#pagerighticon").click(_.bind(function() {
                K5.gui.selected.next();
        }, this));

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
        
            }
        */


        this.arrowbuttons();

        var mapDiv = $("<div/>",{"id":"map"});
        mapDiv.css('width','100%');            
        mapDiv.css('height','100%');            
        this.elem.append(mapDiv);    

    
        this._tmpimage.onload = _.bind(function() {
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
                        view: this.view2D,
                });
        

                this.map.on('postrender', _.bind(function(evt) {
                        if (!this.disposed) {
                                console.log("post compose");
                                if (!this.alto.altoInitialized) {
                                        this.alto.init(this);
                                }
                                this.alto.markers(this);                       
                        }
                },this));

        
                var ext = this.projection.getExtent();
                var size = this.map.getSize();

                var nresolution = ext[3]/(size[1]-20);
                this.map.getView().setResolution(nresolution);

                this.arrowbuttons();

            },this);

    $(this._tmpimage).error(_.bind(function(eventData) {
        var rect = [$("#map").width(), $("#map").height()];                
        $("#map").remove();
        this.disabledDisplay = true;
    },this));    

    this._tmpimage.src=this.url;
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
        $("#contextbuttons").html("");
        $("#item_menu>div").each(function() {
            if ($(this).data("ctx")) {
                var a = $(this).data("ctx").split(";");
                if (jQuery.inArray('all', a) > -1 || jQuery.inArray('zoomstatic', a) > -1) {
                    $("#contextbuttons").append($(this).clone());
                }
                
                if (jQuery.inArray('selected', a) > -1) {
                    if (K5.gui.clipboard.isCurrentSelected()) {
                        $("#contextbuttons").append($(this).clone());
                    }
                }

                if (jQuery.inArray('notselected', a) > -1) {
                    if (!K5.gui.clipboard.isCurrentSelected()) {
                        $("#contextbuttons").append($(this).clone());
                    }
                }

                // only clipboard
                if (jQuery.inArray('clipboardnotempty', a) > -1) {
                    if (K5.gui.clipboard.getSelected().length > 0) {
                        $("#contextbuttons").append($(this).clone());
                    }
                }

                // next context
                if (jQuery.inArray('next', a) > -1) {
                        var selected = K5.api.ctx["item"].selected;
                        if (K5.api.ctx["item"][selected]["siblings"]) {
                                var data = K5.api.ctx["item"][selected]["siblings"];
                                var arr = data[0]['siblings'];
                                var index = _.reduce(arr, function(memo, value, index) {
                                        return (value.selected) ? index : memo;
                                }, -1);
                                if (index<arr.length-1) { 
                                        $("#contextbuttons").append($(this).clone());
                                }  
                        }
                }

                // prev context
                if (jQuery.inArray('prev', a) > -1) {
                        var selected = K5.api.ctx["item"].selected;
                        if (K5.api.ctx["item"][selected]["siblings"]) {
                                var data = K5.api.ctx["item"][selected]["siblings"];
                                var arr = data[0]['siblings'];
                                var index = _.reduce(arr, function(memo, value, index) {
                                        return (value.selected) ? index : memo;
                                }, -1);
                                if (index>0) { 
                                        $("#contextbuttons").append($(this).clone());
                                }  
                        }
                }

            }
        });

        if (!K5.gui["selected"].hasParent()) {
            $("#contextbuttons>div.parent").hide();
        }
}


ZoomifyStaticImage.prototype.arrowbuttons = function() {
        var selected = K5.api.ctx["item"].selected;
        if (K5.api.ctx["item"] && K5.api.ctx["item"][selected] &&  K5.api.ctx["item"][selected]["siblings"]) {
                var data = K5.api.ctx["item"][selected]["siblings"];
                var arr = data[0]['siblings'];
                var index = _.reduce(arr, function(memo, value, index) {
                        return (value.selected) ? index : memo;
                }, -1);
                if (index>0) { $("#pageleft").show(); } else { $("#pageleft").hide(); }  
                if (index<arr.length-1) { $("#pageright").show(); } else { $("#pageright").hide(); }  

                K5.eventsHandler.trigger("application/menu/ctxchanged", null);

        } else {
                K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
                        var arr = data[0]['siblings'];
                        var index = _.reduce(arr, function(memo, value, index) {
                                return (value.selected) ? index : memo;
                        }, -1);

                        if (index>0) { $("#pageleft").show(); } else { $("#pageleft").hide(); }  
                        if (index<arr.length-1) { $("#pageright").show(); } else { $("#pageright").hide(); }  

                        K5.eventsHandler.trigger("application/menu/ctxchanged", null);

                });
        }
}

ZoomifyStaticImage.prototype.relativePosition = function() {
        return $("#map").position();        
}


/**
 * Make current image to fit to the current space  
 * @method
 */
ZoomifyStaticImage.prototype.fit = function() {
        var ext = this.projection.getExtent();
        var size = this.map.getSize();

        var nresolution = ext[3]/(size[1]-20);
        this.view2D.setResolution(nresolution);
        
        this.view2D.setCenter([ext[2]/2, ext[3]/2]);
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
        console.log("map center "+mapCenter);

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
     var z = this.map.getView().getZoom()-1;
     //animation
     var anim = ol.animation.zoom({
        resolution:this.map.getView().getResolution()
     });
     this.map.beforeRender(anim);    
     this.map.getView().setZoom(z);
}

/**
 * Zooming in
 * @method
 */
ZoomifyStaticImage.prototype.zoomIn = function() {
    var z = this.map.getView().getZoom()+1;
    //animation
    var anim = ol.animation.zoom({
     resolution:this.map.getView().getResolution()
    });
    this.map.beforeRender(anim);    
    this.map.getView().setZoom(z);
}

/**
 * Dispose current viewer
 * @method       
 */
ZoomifyStaticImage.prototype.clearContainer = function() {
        console.log("clear container");

        this.alto.clear(this);
        if (this.map) this.map.setTarget();
 
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

