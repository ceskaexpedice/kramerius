/**
 * @fileoverview <h3>Common functions using everywhere</h3>
 */



/** Logger interface */
function Logger() {
	this.messages = {
			"DEBUG": new Array(),
			"INFO": new Array(),
			"ERROR": new Array()
	}
}
/** Log message with level */
Logger.prototype.log = function(level, mesg) {
	if (!(level in this.messages)) {
		throw new Error("no level '"+level+"'");
	}
	this.messages[level].push(mesg);
}
/** display messages into div */
Logger.prototype.display = function(level, divid) {
//	var texts = this.messages[level];
//	$.each(this.messages[level], function(index, value) {
//		
//	});
}


Logger.prototype.getMessages = function(level) {
	return this.messages[level];
}


/** Default logger instance */
var LOGGER = new Logger();

/** forEach in array */
Array.prototype.forEach =  function (action) {
	for (var i = 0; i < this.length; i++) {
		action(this[i]);
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
		base = combine(base, element);
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
		result.push(func(element));
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
 * Bind property or method from object to this object
 * @param func
 * @param object
 * @returns {Function}
 */
function bind(func, object) {
  return function(){
    return func.apply(object, arguments);
  };
}


/** 
 * Ajax call. 
 * @param {string} urlAddress REquesting address
 * @param {Function} successFunction Callback executed after browser receive response
 * @param {Function} failedFunction Callback when the error occured
 * @requires JQuery
 */
function ajax(urlAddress, successFunction, failedFunction) {
    $.ajax({
  	  url: urlAddress,
        success: successFunction,
        error:failedFunction	  
    });
}



/** 
 * Ajax helper. 
 */
function AjaxHelper(defaultErrorFunctions) {
	this.defaultErrorFunctions = defaultErrorFunctions || {};			
}

AjaxHelper.prototype.fail=function(xhr) {
	var func = this.defaultErrorFunctions[xhr.status] || this.defaultErrorFunctions["default"];
	func.apply(this,arguments);
}

AjaxHelper.prototype.get = function(urlAddress, successFunction, failedFunction) {
    $.ajax({
	url: urlAddress,
	success: successFunction,
	error:bind(function(xhr) { 
			if (failedFunction) {  
				failedFunction.apply(this,arguments); 
			} else {
				this.fail(xhr);			
			}
		}, this)
    });
}

var defaultJQueryXHR = new AjaxHelper({
	"403": function(xhr) {
		var cur = window.location;
		window.location=cur;	
	},
	"default": function(xhr) {
		alert("unhandled error");	
	}
});


