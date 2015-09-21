

/**
 * Returns valid jquery identifier
 * @param {string} id to convert. 
 * @global
 */function jq(myid) { 
    return '#' + myid.replace(/(:|\.|\/)/g,'\\$1');
}

/**
 * Returns true (or false) if given keypath in given objects exists
 * @param {string} keys combined keys into one string.  '/' is used as delimiter. 
 * @global
 */
function lookUpKey(keypath, object) {
    var stack = [];
    var paths = keypath.split("/");
    stack.push(object);
    while (stack.length > 0) {
        if (paths.length == 0)
            break;
        var struct = stack.pop();
        var topkey = paths[0];
        paths.shift();
        var defined = (typeof struct[topkey] != 'undefined');
        if (defined) {
            stack.push(struct[topkey]);
        } else {
            return false;
        }
    }
    return paths.length == 0;

}

/**
 * Goto method
 * @param addr target address 
 * @global
 */
function link(addr, target) {
    if(target){
         window.open(addr, target);
    }else{
        window.location.assign(addr);
    }
}

/**
 * Returns true if argument is array
 * @param {obj} tested object 
 * @global
 */
function isArray(obj) {
    if (typeof obj !== 'undefined') {
        return obj.constructor === Array;
    } else{
        return false;
    }
}

/**
 * Returns true if argument is empty string
 * @param {string} tested string 
 * @global
 */
function isEmpty(str) {
    return (!str || 0 === str.length);
}

/**
 * Mix two objects into one
 * @function
 * @global
 */
function mixInto(target, source, methodNames) {
    var args = Array.prototype.slice.apply(arguments);
    target = args.shift();
    source = args.shift();

    methodNames = args;

    if (methodNames.length == 0) {
        for (var k in source) {
            var type = typeof source[k];
            if (type === "function") {
                methodNames.push(k);
            }
        }
    }

    var method;
    var length = methodNames.length;

    for (var i = 0; i < length; i++) {
        method = methodNames[i];

        // bind the function from the source and assign the
        // bound function to the target
        target[method] = _.bind(source[method], source);
    }
    return target;
}

/** stacktrace utility */
function stacktrace() {
    function st2(f) {
        return !f ? [] :
                st2(f.caller).concat([f.toString().split('(')[0].substring(9) + '(' + f.arguments.join(',') + ')']);
    }
    return st2(arguments.callee.caller);
}

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};

function colorToHex(c) {
    return ($.rgbHex(c)).toString().toUpperCase();
}


var Point = function(x, y) {
    this.x = x;
    this.y = y;
};

Point.prototype = {
    x: 0,
    y: 0,
    getX: function() {
        return this.x;
    },
    getY: function() {
        return this.y;
    },
    setX: function(x) {
        this.x = x;
    },
    setY: function(y) {
        this.y = y;
    },
    move: function(dx, dy) {
        this.x = this.x + dx;
        this.y = this.y + dy;
    }
}

var Size = function(w, h) {
    this.width = w;
    this.height = h;
};

Size.prototype = {
    width: 0,
    height: 0,
    getWidth: function() {
        return this.width;
    },
    getHeight: function() {
        return this.height;
    }
}

var Rectangle = function(x, y, width, height) {
    this.location = new Point(x, y);
    this.size = new Size(width, height);

    this.center = new Point(x + width / 2, y + height / 2);
};
Rectangle.prototype = {
    location: new Point(0, 0),
    size: new Size(0, 0),
    center: new Point(0, 0),
    getX: function() {
        return this.location.getX();
    },
    getY: function() {
        return this.location.getY();
    },
    getWidth: function() {
        return this.size.getWidth();
    },
    getHeight: function() {
        return this.size.getHeight();
    },
    getLocation: function() {
        return this.location;
    },
    getCenter: function() {
        return this.center;
    },
    getSize: function() {
        return this.size;
    },
    clone: function() {
        return new Rectangle(this.location, this.size);
    }
}


function isScrolledIntoView(elem, view) {
    var docViewTop = $(view).offset().top;
    var docViewBottom = docViewTop + $(view).height();

    var elemTop = $(elem).offset().top;
    var elemBottom = elemTop + $(elem).height();
    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
}

function isTouchDevice() {
    return (('ontouchstart' in window)
            || (navigator.MaxTouchPoints > 0)
            || (navigator.msMaxTouchPoints > 0));
}


/**
 * Close all opened panel
 * @function cleanWindow
 * @static
 */
function cleanWindow() {
        $(".showing").each(function(index,value) {
                close(value);                                               
        });
}

/**
 * Open window defined by element selector
 * @function divopen
 * @static
 */
function divopen(elm) {
        $(elm).addClass("showing");        
        $(elm).show();        
}



/**
 * Open window defined by element selector
 * @function divopen
 * @static
 */
function divclose() {
    $(".showing").hide();     
}

/**
 * Close window defined by element selector
 * @function close
 * @static
 */
function close(elm) {
        $(elm).removeClass("showing");        
        $(elm).hide();        
}


function toggle(elm) {
        if ($(elm).is(':visible')) close(elm);                
        else divopen(elm);
}

function visible(elm) {
        return ($(elm).is(':visible'));                
}

function escapeSolrChars(value){
    var specials = ['+', '-', '&', '!', '(', ')', '{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\'];
    var regexp = new RegExp("(\\" + specials.join("|\\") + ")", "g");
    return value.replace(regexp, "\\$1");
}


function removeHistoryPostfix(url) {
    var arr = url.split(";");
    return arr[0];
}

/** prototypes */
if (typeof String.prototype.startsWith !== 'function') {
        String.prototype.startsWith = function (str){
                return this.slice(0, str.length) === str;
        };
}

if (typeof String.prototype.endsWith !== 'function') {
        String.prototype.endsWith = function (str){
                return this.slice(-str.length) === str;
        };
}

function getHistoryDeep(){
    
    var hash = hashParser();
    if (hash.hasOwnProperty("hist")) {
        return parseInt(hash.hist);
    }else{
        return 0;
    }
}

function backToResults(){
    var histDeep = getHistoryDeep();
    window.history.go(0 - histDeep - 1);
}

function hashParser(hash){
    if(!hash){
        hash = window.location.hash;
    }else if(!hash.startsWith("#")){
        hash = "#" + hash;
    }
    if(hash.length > 1){
        hash = hash.startsWith("#!") ? hash.substring(2) : hash.substring(1);
        var parts = hash.split(";");
        var ret = {};
        //ret.pid = parts[0];
        for(var i = 0; i<parts.length; i++){
            var part = parts[i].split("=");
            if(part.length>1){
                ret[part[0]] = part[1];
            }else if(i===0){
                // je to prvni a bez =
                ret.pid = parts[0];
            }else{
                ret["key"+i] = part[0];
            }
        }
        return ret;
    }else{
        return {};
    }
}

function jsonToHash(json){
    var hash = "";
    
    $.each(json, function(item, val){
        hash += ";"+item+"=" + val;
    });
//    
//    if(json.hasOwnProperty("pid")){
//        hash = json.pid;
//    }
//    
//    if(json.hasOwnProperty("hist")){
//        hash += ";hist=" + json.hist;
//    }
//    
//    if(json.hasOwnProperty("pmodel")){
//        hash += ";pmodel=" + json.pmodel;
//    }
    if(hash.length>1){
        hash = hash.substring(1);
    }
    return hash;
}