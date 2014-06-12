/* 
 * Contains basic utility methods
 */

/**
 * Returns true (or false) if given keypath in given objects exists
 * @param {string} keys combined keys into one string.  '/' is used as delimiter. 
 */
function lookUpKey(keypath, object) {
	var stack = [];
	var paths = keypath.split("/");
	stack.push(object);
	while(stack.length > 0) {
		if (paths.length == 0) break;
		var struct = stack.pop();
		var topkey = paths[0]; paths.shift();   
		var defined = (typeof struct[topkey] != 'undefined');
		if (defined) {
			stack.push(struct[topkey]);			
		} else {
			return false;
		}		
	}
	return paths.length == 0;
	
}

function mixInto(target, source, methodNames){
 
  // ignore the actual args list and build from arguments so we can
  // be sure to get all of the method names
  var args = Array.prototype.slice.apply(arguments);
  target = args.shift();
  source = args.shift();
  methodNames = args;
 
  var method;
  var length = methodNames.length;
  for(var i = 0; i < length; i++){
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
    
    this.center = new Point(x + width/2, y+height/2);
};
Rectangle.prototype = {
    location: new Point(0, 0),
    size: new Size(0, 0),
    center: new Point(0,0),
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
