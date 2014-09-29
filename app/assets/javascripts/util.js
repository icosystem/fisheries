/*
 *  Copyright (C) 2014 Icosystem
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
if (!ARRAY) {
	var ARRAY = {};
}
if (!TABLE) {
	var TABLE = {};
}
if (!FORMAT) {
	var FORMAT = {};
}
if (!REMOTE) {
	var REMOTE = {};
}
if (!BASIC) {
	var BASIC = {};
}

/**
 * Find the minimum and maximum value in an array 
 * (possibly skipping some initial elements).
 */
ARRAY.minAndMax = function(values, skip, alwaysShowZero) {
	if(!skip){skip=0;}
	var max = 0;
	var min = 0;
	if(!alwaysShowZero){
		for (var i=skip;i<values.length && i<skip+1;i++) {
			max = values[i];
			min = values[i];
		}
	}
	
	for (var i=skip;i<values.length;i++) {
	  var current = values[i];	  
	  if (current > max) { max = current; }
	  if (current < min) { min = current; }
	}
	
	return { max: max, min: min };
}

/**
 * Sum all the positive values into a max
 * and all the negative values into a min
 * in an array (possibly skipping some initial elements).
 */
ARRAY.posAndNeg = function(values, skip, alwaysShowZero) {
	if(!skip){skip=0;}
	var pos = 0;
	var neg = 0;
	
	for (var i=skip;i<values.length;i++) {
	  var current = values[i];	  
	  if (current > 0) { pos += current; }
	  if (current < 0) { neg += current; }
	}
	
	return { max: pos, min: neg };
}

/**
 * Keep the largest Max and Smallest min of an array of {min, max} objects
 */
ARRAY.minAndMaxReduce = function(minMaxArray){
	var max = 0;
	var min = 0;
	for(var i=0; i<minMaxArray.length && i<1; i++) {
		max = minMaxArray[i].max;
		min = minMaxArray[i].min;
	}
	
	for(var i=0; i<minMaxArray.length; i++) {		
	  var current = minMaxArray[i];
	  if (current.max > max) { max = current.max; }
	  if (current.min < min) { min = current.min; }
	}
	return { max: max, min: min };
}


// ====================

/**
 * Run a function which takes (array, skip) for each row
 */
TABLE.forEachRow = function(table, rowSkip, colSkip, alwaysShowZero, apply){
	var perRow = [];
	for (var i=rowSkip;i<table.length;i++) {
		perRow.push(apply(table[i], colSkip, alwaysShowZero));		
	}
	return perRow;
}

TABLE.minAndMax = function(table, alwaysShowZero) {
	return ARRAY.minAndMaxReduce(TABLE.forEachRow(table, 1, 1, alwaysShowZero, ARRAY.minAndMax));
}

TABLE.posAndNeg = function(table, alwaysShowZero) {
	return ARRAY.minAndMaxReduce(TABLE.forEachRow(table, 1, 1, false, ARRAY.posAndNeg));
}

TABLE.replace = function(table, find, replace) {
	var rowSkip = 1;
	for (var i=rowSkip;i<table.length;i++) {
		for(var j=0;j<table[i].length; j++){
			var current = table[i][j];
			if(current === find){
				table[i][j] = replace;
			}
		}
	}
	return table;
}

/**
 * Format a number for nice tight display, rounding
 * scaling for millions & thousands 
 */
FORMAT.pretty = function(num) {
	if (isNaN(num)) {
		return num; // cannot format
	}
	if (num === 0) {
		return 0;
	}
	if (Math.abs(num) < 0.001) {
		// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/toExponential
		// display in scientific notation with at most 2 decimals of
		// precision
		return num.toExponential(2);
	}
	if (Math.abs(num) < 1) {
		if(Math.abs(num) >= 0.001){
			return Math.round(num * 1000) / 1000;
		}
		return num;
	}

	if (Math.abs(num) >= 1000000) {
		var millions = Math.round(num / 10000) * 10000.0; // truncate anything less than a 10-thousand
		millions = millions / 1000000.0; // scale to millions
		// display at most 2 decimal points
		return (Math.round(millions * 100.0) / 100) + 'M';
	}

	if (Math.abs(num) >= 10000) { // larger than 10K, simplify to thousands
		var thousands = Math.round(num / 10) * 10.0; // truncate anything less than a 10
		thousands = thousands / 1000.0; // scale to thousands
		// display at most 2 decimal points
		return (Math.round(thousands * 100.0) / 100) + 'K';
	}

	// http://stackoverflow.com/questions/11832914/round-to-at-most-2-decimal-places-in-javascript
	// display at most 2 decimal points
	return Math.round(num * 100) / 100;
}

REMOTE.requestResultTable = function(resultsId, tableId, successCb, errorCb) {
	if(typeof(jQuery) != 'undefined' && resultsId != null && tableId != null && typeof(successCb) === 'function'){
		$.getJSON('/results/' + resultsId + '/' + tableId)
		.done(function(data) {
			successCb(data);
		})
		.fail(function(jqxhr, textStatus, error) {
			if(typeof(errorCb) === 'function'){
				errorCb(jqxhr, textStatus, error);
			}else{
				var err = textStatus + ", " + error;
				console.log("Request Failed: " + err);
			}
		});
	}
}

//http://stackoverflow.com/questions/2061325/javascript-object-key-value-coding-dynamically-setting-a-nested-value
//set nested properties from string
BASIC.setOn = function(key,val,data) {
	var setData = function(key,val,obj) {
		if (!obj) obj = data; //outside (non-recursive) call, use "data" as our base object
		var ka = key.split(/\./); //split the key by the dots
		if (ka.length < 2) { 
			obj[ka[0]] = val; //only one part (no dots) in key, just set value
		} else {
			if (!obj[ka[0]]) obj[ka[0]] = {}; //create our "new" base obj if it doesn't exist
			obj = obj[ka.shift()]; //remove the new "base" obj from string array, and hold actual object for recursive call
			setData(ka.join("."),val,obj); //join the remaining parts back up with dots, and recursively set data on our new "base" obj
		}    
	}
	setData(key,val);
}
