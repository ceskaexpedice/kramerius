/**
 * PDF view
 * @constructor
 */
function PDFView(appl, selector) {
        this.application = (appl || K5);
        //var jqSel = (selector || '#viewer>div.container');        
        var jqSel = (selector || '#viewer>div.container>div.ol');

        this.container = $(jqSel);

        this.searchHandler = _.bind(function(type, data) {
                if (type =="app/searchInside") {
                        var q = data;
                        $("#pdfIframe")[0].contentWindow.postMessage("query="+q, '*');
                }
        },this);

        this.application.eventsHandler.addHandler(this.searchHandler);

}
/**
 * Open pdf
 * @method
 */
PDFView.prototype.open = function() {

        var href = "/client/api/item/"+K5.api.ctx.item.selected+"/streams/IMG_FULL";
        if (this.page !== undefined) {
                href += "#"+this.page;
        }


        var optionsDiv = _optionspane({
                "maximize":true,
                "fit":false,
                "rotateleft":false,
                "rotateright":false,
                "zoomin":true,
                "zoomout":true,
                "lock":false
        });
        if ($("#options").length > 0) {
                $("#options").remove();
        }
        this.container.append(optionsDiv);

        var q = $("#q").val();
        if (q !== null) {
                href = href +"&query="+q;
        }


        var iFrame = $("<iframe/>",{'src':'pdf/web/viewer.html?file='+href,'id':'pdfIframe'});
        iFrame.css("width","99%");
        iFrame.css("height","100%");
        iFrame.css('border','none');

        this.container.append(iFrame);

        //if ()
        if (!this.containsLeftStructure()) {
                this.olDirtyFlag = true;
                $("div.ol").css("width","100%");
        }

        // copied from Alberto thumbs
        K5.eventsHandler.trigger("application/menu/ctxchanged", null);
}

PDFView.prototype.clearContainer = function() {
        $("#pdfIframe").remove();
        $("#pdfContainer").remove();
        $("#pageleft").remove();
        $("#pageright").remove();
        $("#options").remove();

        if (this.olDirtyFlag) {
                $("div.ol").css("width","width: calc(100% - 350px)");
                this.olDirtyFlag = false;
        }
}

PDFView.prototype.arrowbuttons = function() {
        var selected = K5.api.ctx["item"].selected;
        if (K5.api.ctx["item"] && K5.api.ctx["item"][selected] &&  K5.api.ctx["item"][selected]["siblings"]) {
                var data = K5.api.ctx["item"][selected]["siblings"];
                var arr = data[0]['siblings'];
                var index = _.reduce(arr, function(memo, value, index) {
                        return (value.selected) ? index : memo;
                }, -1);
                if (index>0) { $("#pageleft").show(); } else { $("#pageleft").hide(); }
                if (index<arr.length-1) { $("#pageright").show(); } else { $("#pageright").hide(); }
        } else {
                K5.api.askForItemSiblings(K5.api.ctx["item"]["selected"], function(data) {
                        var arr = data[0]['siblings'];
                        var index = _.reduce(arr, function(memo, value, index) {
                                return (value.selected) ? index : memo;
                        }, -1);

                        if (index>0) { $("#pageleft").show(); } else { $("#pageleft").hide(); }
                        if (index<arr.length-1) { $("#pageright").show(); } else { $("#pageright").hide(); }
                });
        }

}

PDFView.prototype.addContextButtons=  function() {
        _ctxbuttonsrefresh();
}

PDFView.prototype.isEnabled= function(data) {
        var datanode = data["datanode"];
        var zoom = data["zoom"];
        var pdf = data["pdf"];
        return  (datanode  &&  pdf);
}


PDFView.prototype.forbiddenCheck = function(okFunc, failFunc) {
        var v = K5.api.ctx.item.selected;
        K5.api.askForRights(v,["read"], function(data){
                if (data.read) {
                        okFunc.apply(null, []);
                } else {
                        failFunc.apply(null, []);
                }
        });

}

PDFView.prototype.containsLeftStructure = function() {
        var selected = K5.api.ctx["item"].selected;
        if (K5.api.ctx["item"] && K5.api.ctx["item"][selected]) {
                return K5.api.ctx["item"][selected]["model"] === "article";
        }
        return false;
}

PDFView.prototype.prevPageEnabled = function () {
        return (this.containsLeftStructure());
}
PDFView.prototype.nextPageEnabled = function () {
        return (this.containsLeftStructure());
}

PDFView.prototype.zoomIn = function() {
        $("#pdfIframe")[0].contentWindow.postMessage("zoomIn", '*');
}
PDFView.prototype.zoomOut = function() {
        $("#pdfIframe")[0].contentWindow.postMessage("zoomOut", '*');
}

PDFView.prototype.leftStructureSettings = function() {
        if (this.containsLeftStructure()) {
                return {
                        "selector":function(thumb) {
                                if (thumb && thumb.model && (thumb.model === "article" || thumb.model === "page")) {
                                        return true;

                                }
                        }
                }
        } else return null;
}

