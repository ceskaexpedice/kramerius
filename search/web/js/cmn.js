/**
 * @fileoverview <h3>Common functions using everywhere</h3>
 */

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
 * Bind callbacks functions to key event
 * @required JQuery 
 */
function bindArrows() {
    // keys - bind left and right arrows
	$(document).keyup(function(e) {
        if (e.keyCode == 39) {
            selectNext();
        } else if (e.keyCode == 37) {
            selectPrevious();
        }
    });
}

/** 
 * Remove binding keyup events
 * @required JQuery 
 */
function unbindArrows() {
	$(document).unbind('keyup');
}
