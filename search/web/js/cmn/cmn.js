/**
 * @fileoverview <h3>Common functions using everywhere</h3>
 */

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.rm = function(from, to) {
	var rest = this.slice((to || from) + 1 || this.length);
	this.length = from < 0 ? this.length + from : from;
	return this.push.apply(this, rest);
};

/** forEach in array */
Array.prototype.forEach =  function (action) {
	for (var i = 0; i < this.length; i++) {
		action(this[i]);
	}
}

/** forEach in String */
String.prototype.forEach =  function (action) {
	for (var i = 0; i < this.length; i++) {
		action(this.charAt(i));
	}
}

/** escape all bad chars in string */
String.prototype.escapeChars = function(allEscapedChars) {
    var retVal = "";

	for (var i = 0; i < this.length; i++) {
		var ch = this.charAt(i);
		
        var found = reduce(function(base, element,status) {
            if (base) return base;
            if (element === ch) {
                base = true;
            }
            return base;
        }, false, allEscapedChars);
        
        if (!found) { retVal = retVal + ch; } else { retVal = retVal +  "\\"+ ch; }
	}	
    return retVal;
}


// IndexOf doesnt work in IE 
if (!Array.prototype.indexOf) {
  Array.prototype.indexOf = function(elt /*, from*/) {
    var len = this.length;
    var from = Number(arguments[1]) || 0;
    from = (from < 0)
         ? Math.ceil(from)
         : Math.floor(from);
    if (from < 0)
      from += len;

    for (; from < len; from++) {
		if (from in this && this[from] === elt)
    	  return from;
    }
    return -1;
  };
}


/** starts with in string */
String.prototype.startsWith = function(str){
    return (this.indexOf(str) === 0);
}

/** replace all in string */
String.prototype.replaceAll=function(replace,with_t) {
    return this.replace(new RegExp(replace, 'g'),with_t);
}

/** ends with in string */
String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

/** trim in prototype - IE doesnt support this function*/
if(typeof String.prototype.trim !== 'function') {
  String.prototype.trim = function() {
    return this.replace(/^\s\s*/, '').replace(/\s\s*$/, ''); 
  }
}




/** compose function */
Function.prototype.compose  = function(argFunction) {
    var invokingFunction = this;
    return function() {
        return  invokingFunction.call(this,argFunction.apply(this,arguments));
    }
}

/** 
 * Reduce function can reduce given array to one result. 
 * 
 * @param {Function} combinator function
 * @param {Object} base starting value
 * @param {array} array
 */
function reduce(combine, base, array) {
	$.each(array,function(index, element) {
		var status = {index:index, first:index==0, last:index==array.length-1};
		base = combine(base, element,status);
	});
	return base;
}

/**
 * Maps function apply given function to every item of given array
 * 
 * @param {Function} function applying to every item of array
 * @param {array} array
 */
function map(func, array) {
	var result = [];
	$.each(array,function(index, element) {
		var value = func(element);
		if (value) {
			result.push(value);
		}			
	});
	return result;
}

function mapJQuerySelector(func, jqueryobj) {
	var result = [];
	jqueryobj.each(function() {
		result.push(func(arguments[1]));		
	});
	return result;
}

function identity(param) {
	return param;
}

/** 
 * Returns rest of array from given index
 * 
 * @param {array} array
 * @param {int} intfrom index
 */
function rest(array, intfrom) {
	var result = [];
	$.each(array,function(index, element) {
		if (index >= intfrom) {
			result.push(element);
		}
	});
	return result;
}


/** 
 * Returns head of array till given index 
 * 
 * @param {array} array
 * @param {int} intto index
 */
function head(array, intto) {
	var result = [];
	$.each(array,function(index, element) {
		if (index <= intto) {
			result.push(element);
		}
	});
	return result;
}


/** partial applying - fixing arguments
 * @private 
 */
	function asArray(quasiArray, start) {
		var result = [];
		for (var i = (start || 0); i < quasiArray.length; i++) {
			result.push(quasiArray[i]);
		}
		return result;
	}

/**
 * Partial application of  given function 
 * @param {Function} function
 * @returns {Function}
 */	
function partial(func) {
	var fixedArgs = asArray(arguments, 1);
	return function(){
		return func.apply(null, fixedArgs.concat(asArray(arguments)));
	};
}


/** 
 * Function composition 
 * @param {Function} func1 
 * @param {Function} func2
 */
function compose(func1, func2) {
	return function() {
		return func1(func2.apply(null, arguments));
	};
}

/** 
 * Negate function 
 * @param {Function} funtion to negate
 */
function negate(func) {
  return function() {
    return !func.apply(null, arguments);
  };
}

/**
 * Bind property or method to given object
 * @param func
 * @param object
 * @returns {Function}
 */
function bind(func, object) {
  return function(){
    return func.apply(object, arguments);
  };
}



var Base64 = {
		 
		// private property
		_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
	 
		// public method for encoding
		encode : function (input) {
			var output = "";
			var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
			var i = 0;
	 
			input = Base64._utf8_encode(input);
	 
			while (i < input.length) {
	 
				chr1 = input.charCodeAt(i++);
				chr2 = input.charCodeAt(i++);
				chr3 = input.charCodeAt(i++);
	 
				enc1 = chr1 >> 2;
				enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
				enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
				enc4 = chr3 & 63;
	 
				if (isNaN(chr2)) {
					enc3 = enc4 = 64;
				} else if (isNaN(chr3)) {
					enc4 = 64;
				}
	 
				output = output +
				this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
				this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);
	 
			}
	 
			return output;
		},
	 
		// public method for decoding
		decode : function (input) {
			var output = "";
			var chr1, chr2, chr3;
			var enc1, enc2, enc3, enc4;
			var i = 0;
	 
			input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
	 
			while (i < input.length) {
	 
				enc1 = this._keyStr.indexOf(input.charAt(i++));
				enc2 = this._keyStr.indexOf(input.charAt(i++));
				enc3 = this._keyStr.indexOf(input.charAt(i++));
				enc4 = this._keyStr.indexOf(input.charAt(i++));
	 
				chr1 = (enc1 << 2) | (enc2 >> 4);
				chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
				chr3 = ((enc3 & 3) << 6) | enc4;
	 
				output = output + String.fromCharCode(chr1);
	 
				if (enc3 != 64) {
					output = output + String.fromCharCode(chr2);
				}
				if (enc4 != 64) {
					output = output + String.fromCharCode(chr3);
				}
	 
			}
	 
			output = Base64._utf8_decode(output);
	 
			return output;
	 
		},
	 
		// private method for UTF-8 encoding
		_utf8_encode : function (string) {
			string = string.replace(/\r\n/g,"\n");
			var utftext = "";
	 
			for (var n = 0; n < string.length; n++) {
	 
				var c = string.charCodeAt(n);
	 
				if (c < 128) {
					utftext += String.fromCharCode(c);
				}
				else if((c > 127) && (c < 2048)) {
					utftext += String.fromCharCode((c >> 6) | 192);
					utftext += String.fromCharCode((c & 63) | 128);
				}
				else {
					utftext += String.fromCharCode((c >> 12) | 224);
					utftext += String.fromCharCode(((c >> 6) & 63) | 128);
					utftext += String.fromCharCode((c & 63) | 128);
				}
	 
			}
	 
			return utftext;
		},
	 
		// private method for UTF-8 decoding
		_utf8_decode : function (utftext) {
			var string = "";
			var i = 0;
			var c = c1 = c2 = 0;
	 
			while ( i < utftext.length ) {
	 
				c = utftext.charCodeAt(i);
	 
				if (c < 128) {
					string += String.fromCharCode(c);
					i++;
				}
				else if((c > 191) && (c < 224)) {
					c2 = utftext.charCodeAt(i+1);
					string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
					i += 2;
				}
				else {
					c2 = utftext.charCodeAt(i+1);
					c3 = utftext.charCodeAt(i+2);
					string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
					i += 3;
				}
	 
			}
	 
			return string;
		}
	 
	}

// IE Console 
if ( ! window.console ) console = { log: function(){} };
