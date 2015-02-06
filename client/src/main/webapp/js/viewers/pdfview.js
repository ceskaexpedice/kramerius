/**
 * PDF view
 * @constructor
 */
function PDFView(appl, selector) {
        this.application = (appl || K5);
        var jqSel = (selector || '#viewer>div.container');        
        this.container = $(jqSel);
}
/** 
 * Open pdf  
 * @method       
 */
PDFView.prototype.open = function() {

        var leftArrowContainerDiv = $("<div/>",{"id":"pageleft","class":"leftarrow" });
        leftArrowContainerDiv.append($("<div/>",{"id":"pagelefticon", class:"arrow"}));    
        this.container.append(leftArrowContainerDiv);    

        var rightArrowContainerDiv = $("<div/>",{"id":"pageright","class":"rightarrow"});
        rightArrowContainerDiv.append($("<div/>",{"id":"pagerighticon", class:"arrow"}));    
        this.container.append(rightArrowContainerDiv);    

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

        this.arrowbuttons();


        var href = "api/item/"+K5.api.ctx.item.selected+"/streams/IMG_FULL";
        if (this.page !== undefined) {
                href += "#"+this.page;                        
        }           

        var pdfContainer = $("<div/>",{'id':'pdfContainer'});
        pdfContainer.css("width","100%");
        pdfContainer.css("height","100%");

        var object = $("<object/>");
        object.attr("type","application/pdf");
        object.attr("data",href);
        object.attr("width","100%");
        object.attr("height","100%");

        pdfContainer.append(object);

        this.container.append(pdfContainer);

        K5.eventsHandler.trigger("application/menu/ctxchanged", null);

}

PDFView.prototype.clearContainer = function() {
        $("#pdfContainer").remove();
        $("#pageleft").remove();
        $("#pageright").remove();

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