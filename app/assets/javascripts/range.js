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
if (!FISHERY) {
	var FISHERY = {};
}

/* requires http://fortawesome.github.io/Font-Awesome/ */
FISHERY.rangeGlyphChart = function(name, target, max) {
	var chart = {};
	chart.max = 10;
	chart.name = name;
	
	if(!isNaN(max)){
		chart.max = Math.abs(Math.round(max));
	}
	
	chart.draw = function(value, changeInInterventionFromStart, changeInBaselineFromStart) {
		if(typeof(jQuery) != 'undefined' && !isNaN(value)) {
			var el = $(target);
			el.html(function() {
				if(value == 0 && changeInBaselineFromStart == 0){
					return '<span class="no-change-text">No change</span>';
				}
				
				var range = '<span class="range-chart">';
				range += '<span class="range-label">large<br>decrease</span>';
				
				// decreasing
				for(var i=-chart.max; i<0; i++){
					var on = value < 0 && value <= i ? ' decrease' : ' off';
					var worstThanStart = changeInInterventionFromStart < 0 ? ' worse-than-start' : '';
					range += '<i class="fa fa-arrow-down'+ on + worstThanStart + '"></i>';
				}
				range += '<span class="no-change"></span>';

				// increasing
				for(var i=1; i<=chart.max; i++){
					var on = value > 0 && value >= i ? ' increase' : ' off';
					var betterThanStart = changeInInterventionFromStart > 0 ? ' better-than-start' : '';
					range += '<i class="fa fa-arrow-up'+ on + betterThanStart + '"></i>';
				}

				range += '<span class="range-label">large<br>increase</span>';
				range += '</span>';
				return range;
			});
		}
	}
	
	return chart;
}

/* requires http://fortawesome.github.io/Font-Awesome/ */
FISHERY.rangeBaselineGlyphChart = function(name, target, max) {
	var chart = {};
	chart.max = 10;
	chart.name = name;
	
	if(!isNaN(max)){
		chart.max = Math.abs(Math.round(max));
	}
	
	chart.draw = function(value, changeInInterventionFromStart, changeInBaselineFromStart) {
		if(typeof(jQuery) != 'undefined' && !isNaN(value)) {
			var el = $(target);
			el.html(function() {
				if(changeInBaselineFromStart == 0){
					return '<span class="no-change-text">-</span>';
				}
				
				var range = '<span class="range-chart">';
				
				// change in baseline from start
				var changeInBaselineClass = ' baselineNoChange';
				if(changeInBaselineFromStart > 0){
					changeInBaselineClass = ' baselineBetterThanStart';
				}else if(changeInBaselineFromStart < 0){
					changeInBaselineClass = ' baselineWorseThanStart';
				}
				range += '<i class="change-in-baseline-from-start fa fa-circle-o'+ changeInBaselineClass + '"></i>';
				
				range += '</span>';
				return range;
			});
		}
	}
	
	return chart;
}