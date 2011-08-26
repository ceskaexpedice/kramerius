/**
 * @fileoverview <h3>Common functions using everywhere</h3>
 */

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
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






