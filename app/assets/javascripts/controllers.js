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
'use strict';

if(!FISHERY) {
	var FISHERY = {};
}

/** Controllers */
angular.module('simulation.controllers', ['simulation.services'])

.controller('InputsCtrl', function ($scope, $http, simulationModel) {
	$scope.config = MODEL_CONFIG;
	
	$scope.spoilageAndOtherCatchRatioInvalid=function(spoilageCatchRatio, otherCatchRatio){
		if(isNaN(spoilageCatchRatio) || isNaN(otherCatchRatio)){
			return true;
		}
		return (spoilageCatchRatio + otherCatchRatio) > 1;
	}

	$scope.interventionMessageLine1=function(start, end){
		if(isNaN(start) || isNaN(end)){
			return "";
		}
		var duration = (end-start)
		if(duration == 0){
			return 'Apply at year ' + start; 
		}
		return 'Phase in at year ' + start; 
	}
	$scope.interventionMessageLine2=function(start, end){
		if(isNaN(start) || isNaN(end)){
			return "";
		}
		var duration = (end-start)
		if(duration == 0){
			return '';
		}
		var yearString = duration == 1 ? 'year' : 'years';
		return 'over ' + duration + ' ' + yearString; 
	}

	$scope.$watch('config', function() {
		//https://github.com/angular/angular.js/issues/5892
		//http://jsfiddle.net/TXazw/5/Edit/
		//iterate over all properties and convert to number
		//make AngularJS treat our input type="range" form elements as numeric
		var convert = function(thing) {
			var parsed = parseFloat(thing);
			if (thing !== null && typeof thing === 'object') { /* thing is an object http://stackoverflow.com/questions/8511281/check-if-a-variable-is-an-object */
				for (var prop in thing) {
					if(thing.hasOwnProperty(prop)){
						thing[prop] = convert(thing[prop]);
					}
				}
				return thing;
			}
			if(!isNaN(parsed)){
				return parsed;
			}
			return thing;
		}
		$scope.config = convert($scope.config)

		simulationModel.rerunSimulation($scope.config);
	}, true);
})

.controller('OutputsCtrl', function ($scope, $http, simulationModel) {
	$scope.status = simulationModel.status;
	$scope.results = simulationModel;
	$scope.overview = simulationModel.overview;	
	$scope.animationFrame = simulationModel.animationFrame;
	$scope.animationLength = simulationModel.animationLength;
	
	$scope.$watch('animationFrame', function() {
		simulationModel.animationFrame = $scope.animationFrame;
		simulationModel.updateAnimatedCharts();
	}, true);
	
	$scope.$on("simulationRan", function(){
        $scope.animationFrame = 0;
        $scope.animationLength = simulationModel.animationLength;
    });
	
	$scope.$watch('overview', function() {
		if(typeof(FISHERY.rangeCharts) != 'undefined'){
			for(var i=0; i<FISHERY.rangeCharts.length; i++){
				var chart = FISHERY.rangeCharts[i];
				chart.draw($scope.overview[chart.name].value, $scope.overview[chart.name].changeInInterventionFromStart, $scope.overview[chart.name].changeInBaselineFromStart);
			}
		}
	}, true);
});