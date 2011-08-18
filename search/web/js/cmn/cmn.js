/**
 * @fileoverview <h3>Common functions using everywhere</h3>
 */

/**
 * Dictionary class
 * @class
 */
function Dictionary(startValues) {
	this.values = startValues || {};
}

Dictionary.prototype.store = function(name, value) {
	this.values[name] = value;
};

Dictionary.prototype.lookup = function(name) {
	return this.values[name];
};

Dictionary.prototype.contains = function(name) {
	return Object.prototype.hasOwnProperty.call(this.values, name) &&
	Object.prototype.propertyIsEnumerable.call(this.values, name);
};

Dictionary.prototype.each = function(action) {
	$.each(this.values, action);
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






