/** Deep zoom initialization */
function DeepZoomViewerInitObject() {
    this.dz = null;
    this.zooming = false; // whether we should be continuously zooming
    this.zoomFactor = null; // how much we should be continuously zooming by
    this.lastZoomTime = null;
}

DeepZoomViewerInitObject.prototype.isInitialized = function() {
    return this.dz != null;
}

DeepZoomViewerInitObject.prototype.open = function(pid) {
    this.dz.openDzi("deepZoom/"+pid+"/");
}

DeepZoomViewerInitObject.prototype.beginZoomingIn = function() {
    this.lastZoomTime = new Date().getTime();
    this.zoomFactor = Seadragon.Config.zoomPerSecond;
    this.zooming = true;
    this.scheduleZoom();
}

DeepZoomViewerInitObject.prototype.prevButton = function() {
    var control = document.createElement("a");
    $(control).html("<span class='ui-icon ui-icon-arrowthick-1-w' >full</span>")

    control.onclick = bind(function(event) {
        this.previousImage();
    },this);
    
    $(control).attr('id', 'seadragonButtonPrev');
    $(control).button();
    return control;
}


DeepZoomViewerInitObject.prototype.goHomeButton = function() {
    var control = document.createElement("a");
    $(control).html("<span class='ui-icon ui-icon-home' >full</span>")
    control.setAttribute('id', 'goHome');
    control.onclick = bind(function(event) {
        if (this.dz.viewport) {
            this.dz.viewport.goHome();
        }
    },this);
    $(control).button();
    return control;
}

DeepZoomViewerInitObject.prototype.nextButton = function() {
    var control = document.createElement("a");
    var span = document.createElement("span");
    $(control).html(
            "<span class='ui-icon ui-icon-arrowthick-1-e' >full</span>")
    control.setAttribute('id', 'nextButton');

    control.className = "control";
    control.onclick = bind(function(event) {
        nextImage();
    },this);

    $(control).attr('id', 'seadragonButtonNext');
    $(control).button();
    return control;
}

DeepZoomViewerInitObject.prototype.fullPageButton = function() {
    var control = document.createElement("a");
    $(control)
            .html(
                    "<span class='ui-icon ui-icon-arrowthick-2-ne-sw' >full</span>")
    control.setAttribute('id', 'fullPageButton');

    control.className = "control";
    control.onclick = bind(function(event) {
        this.dz.setFullPage(!this.dz.isFullPage());

        if (this.dz.viewport) {
            this.dz.viewport.ensureVisible();
        }
    },this);

    $(control).button();
    return control;
}


DeepZoomViewerInitObject.prototype.endZooming = function() {
    this.zooming = false;
}

DeepZoomViewerInitObject.prototype.scheduleZoom=function() {
    window.setTimeout(doZoom, 10);
}

DeepZoomViewerInitObject.prototype.doZoom=function() {
    if (zooming && this.dz.viewport) {
        var currentTime = new Date().getTime();
        var deltaTime = currentTime - lastZoomTime;
        var adjustedFactor = Math.pow(zoomFactor, deltaTime / 1000);

        this.dz.viewport.zoomBy(adjustedFactor);
        this.dz.viewport.ensureVisible();
        this.lastZoomTime = currentTime;
        this.scheduleZoom();
    }
}

DeepZoomViewerInitObject.prototype.beginZoomingOut = function() {
    this.lastZoomTime = new Date().getTime();
    this.zoomFactor = 1.0 / Seadragon.Config.zoomPerSecond;
    this.zooming = true;
    this.scheduleZoom();
}

DeepZoomViewerInitObject.prototype.zoomInButton=function() {
    var control = document.createElement("a");
    var span = document.createElement("span");
    $(control).html("<span class='ui-icon ui-icon-plusthick' >full</span>")
    control.setAttribute('id', 'plusButton');

    control.className = "control";

    var tracker = new Seadragon.MouseTracker(control);
    tracker.clickHandler = bind(function(tracker, position, quick, shift) {
        if (this.dz.viewport) {
            this.zooming = false;
            this.dz.viewport.zoomBy(Seadragon.Config.zoomPerClick / 1.0);
            this.dz.viewport.ensureVisible();
        }
    },this);

    tracker.pressHandler = bind(function(tracker, position) {
        this.beginZoomingIn();
    },this);

    tracker.enterHandler = bind(function(tracker, position, buttonDownElm,
            buttonDownAny) {
        this.beginZoomingIn();
    },this);

    tracker.releaseHandler = bind(function(tracker, position, insideElmtPress,
            insideElmtRelease) {
        this.endZooming();
    },this);

    tracker.exitHandler = bind(function(tracker, position, buttonDownElmt,
            buttonDownAny) {
        this.endZooming();
    },this);

    $(control).button();
    tracker.setTracking(true);
    return control;
}

DeepZoomViewerInitObject.prototype.zoomOutButton=function() {
    var control = document.createElement("a");
    var span = document.createElement("span");
    $(control)
            .html("<span class='ui-icon ui-icon-minusthick' >full</span>")
    control.setAttribute('id', 'plusButton');

    control.className = "control";

    var tracker = new Seadragon.MouseTracker(control);
    tracker.clickHandler = bind(function(tracker, position, quick, shift) {
        if (this.dz.viewport) {
            zooming = false;
            this.dz.viewport.zoomBy(1.0 / Seadragon.Config.zoomPerClick);
            this.dz.viewport.ensureVisible();
        }
    },this);

    tracker.pressHandler = bind(function(tracker, position) {
        this.beginZoomingOut();
    },this);

    tracker.enterHandler = bind(function(tracker, position, buttonDownElm,
            buttonDownAny) {
        this.beginZoomingOut();
    },this);

    tracker.releaseHandler = bind(function(tracker, position, insideElmtPress,
            insideElmtRelease) {
        this.endZooming();
    },this);

    tracker.exitHandler = function(tracker, position, buttonDownElmt,
            buttonDownAny) {
        this.endZooming();
    }

    $(control).button();
    tracker.setTracking(true);
    return control;
}

DeepZoomViewerInitObject.prototype.init = function() {
    this.dz = new Seadragon.Viewer("container");
    this.dz.clearControls();
    this.dz.addControl(this.nextButton(), Seadragon.ControlAnchor.TOP_RIGHT);
    this.dz.addControl(this.prevButton(), Seadragon.ControlAnchor.TOP_RIGHT);
    this.dz.addControl(this.goHomeButton(), Seadragon.ControlAnchor.TOP_RIGHT);
    this.dz.addControl(this.fullPageButton(), Seadragon.ControlAnchor.TOP_RIGHT);
    this.dz.addControl(this.zoomOutButton(), Seadragon.ControlAnchor.TOP_RIGHT);
    this.dz.addControl(this.zoomInButton(), Seadragon.ControlAnchor.TOP_RIGHT);

    // lokalizacenextImage
    Seadragon.Strings.setString("Tooltips.FullPage",
            dictionary["deep.zoom.Tooltips.FullPage"]);
    Seadragon.Strings.setString("Tooltips.Home",
            dictionary["deep.zoom.Tooltips.Home"]);
    Seadragon.Strings.setString("Tooltips.ZoomIn",
            dictionary["deep.zoom.Tooltips.ZoomIn"]);
    Seadragon.Strings.setString("Tooltips.ZoomOut",
            dictionary["deep.zoom.Tooltips.ZoomOut"]);

    Seadragon.Strings.setString("Errors.Failure",
            dictionary["deep.zoom.Errors.Failure"]);
    Seadragon.Strings.setString("Errors.Xml",
            dictionary["deep.zoom.Errors.Xml"]);
    Seadragon.Strings.setString("Errors.Empty",
            dictionary["deep.zoom.Errors.Empty"]);
    Seadragon.Strings.setString("Errors.ImageFormat",
            dictionary["deep.zoom.Errors.ImageFormat"]);
}

var zoomInit = new DeepZoomViewerInitObject();
