/*
 * functions for common using
 */

/** reduce function  */
function reduce(combine, base, array) {
	$.each(array,function(index, element) {
		base = combine(base, element);
	});
	return base;
}

/** map function */
function map(func, array) {
	var result = [];
	$.each(array,function(index, element) {
		result.push(func(element));
	});
	return result;
}

/** returns rest of array */
function rest(array, intfrom) {
	var result = [];
	$.each(array,function(index, element) {
		if (index >= intfrom) {
			result.push(element);
		}
	});
	return result;
}


/** head function */
function head(array, intto) {
	var result = [];
	$.each(array,function(index, element) {
		if (index <= intto) {
			result.push(element);
		}
	});
	return result;
}


/** partial applying - fixing arguments */
	function asArray(quasiArray, start) {
		var result = [];
		for (var i = (start || 0); i < quasiArray.length; i++)
			result.push(quasiArray[i]);
		return result;
	}

function partial(func) {
	var fixedArgs = asArray(arguments, 1);
	return function(){
		return func.apply(null, fixedArgs.concat(asArray(arguments)));
	};
}


/** function's composition */
function compose(func1, func2) {
	return function() {
		return func1(func2.apply(null, arguments));
	};
}

/** negate function */
function negate(func) {
  return function() {
    return !func.apply(null, arguments);
  };
}