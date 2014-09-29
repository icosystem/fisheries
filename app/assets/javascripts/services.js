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

/*
 * jQuery throttle / debounce - v1.1 - 3/7/2010
 * http://benalman.com/projects/jquery-throttle-debounce-plugin/
 * 
 * Copyright (c) 2010 "Cowboy" Ben Alman
 * Dual licensed under the MIT and GPL licenses.
 * http://benalman.com/about/license/
 */
(function(b,c){var $=b.jQuery||b.Cowboy||(b.Cowboy={}),a;$.throttle=a=function(e,f,j,i){var h,d=0;if(typeof f!=="boolean"){i=j;j=f;f=c}function g(){var o=this,m=+new Date()-d,n=arguments;function l(){d=+new Date();j.apply(o,n)}function k(){h=c}if(i&&!h){l()}h&&clearTimeout(h);if(i===c&&m>e){l()}else{if(f!==true){h=setTimeout(i?k:l,i===c?e-m:e)}}}if($.guid){g.guid=j.guid=j.guid||$.guid++}return g};$.debounce=function(d,e,f){return f===c?a(d,e,false):a(d,f,e!==false)}})(this);

function createChart(id, title, hLabel, vLabel, colors) {
	return {
	  "type": "LineChart",
	  "data": [['',''],[0,0],[1,0]],
	  "options": FISHERY.chartOptions(id, title, hLabel, vLabel, colors)
	 };
}

function createLineAsTimelineChart(id, title, hLabel, vLabel, colors) {
	var defaultOptions = FISHERY.chartOptions(id, title, hLabel, vLabel, colors);
	defaultOptions.lineWidth = 5;
	defaultOptions.pointSize = 15;
	defaultOptions.pointShape = 'square';
	defaultOptions.vAxis.textPosition = 'none';
	defaultOptions.hAxis.baselineColor = 'white';
	defaultOptions.interpolateNulls = false;
	return {
	  "type": "LineChart",
	  "data": [['',''],[0,0],[1,0]],
	  "options": defaultOptions
	 };
}

function createTable() {
	return {
	  "type": "Table",
	  "data": [['',''],[0,0],[1,0]],
	  "options": FISHERY.tableOptions()
	 };
}

function createStackedAreaChart(id, title, hLabel, vLabel, colors) {
	var options = FISHERY.chartOptions(id, title, hLabel, vLabel, colors);
	options.isStacked = true;

	return {
	  "type": "AreaChart",
	  "data": [['',''],[0,0],[1,0]],
	  "options": options
	 };
}

function createStackedColumnChart(id, title, hLabel, vLabel, colors) {
	var options = FISHERY.chartOptions(id, title, hLabel, vLabel, colors);
	options.isStacked = true;

	return {
	  "type": "ColumnChart",
	  "data": [['Scenario', 'Fisher', 'Middleman', 'Exporter'],
	           ['Baseline', 10, 20, 30],
	           ['Intervention', 16, 17, 27]],
	  "options": options
	 };
}

function createColumnChart(id, title, hLabel, vLabel, colors) {
	var options = FISHERY.chartOptions(id, title, hLabel, vLabel, colors);
	options.isStacked = false;

	return {
	  "type": "ColumnChart",
	  "data": [['', '', '', ''],
	           ['Baseline', 10, 20, 30],
	           ['Intervention', 16, 17, 27]],
	  "options": options
	 };
}

//http://stackoverflow.com/questions/123999/how-to-tell-if-a-dom-element-is-visible-in-the-current-viewport/7557433#7557433
function isElementInViewport(el) {
    var rect = el.getBoundingClientRect();

    return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && /*or $(window).height() */
        rect.right <= (window.innerWidth || document.documentElement.clientWidth) /*or $(window).width() */
    );
}

angular.module('simulation.services', [])

.service('simulationModel', function($http, $rootScope){
  var that = {};
  
  that.status = {};
  that.status.statusMessageVisible = false;
  that.status.message = "";
  that.status.status = "";
  
  that.overview = {}
  for(var i=0; i<FISHERY.OVERVIEW_IDS.length; i++){
	  var c = FISHERY.OVERVIEW_IDS[i];
	  that.overview[c] = 0.0;
  }
  that.test = 5;
  
  that.animationFrame = 0;
  that.animationLength = 0;
  
  that.updateStatus = function(status) {
	if(status === 'running'){
		that.status.statusMessageVisible = true;
		that.status.message = "Running Simulation...";
	}else if(status === 'charts'){
		that.status.statusMessageVisible = true;
		that.status.message = "Creating Charts...";
	}else{
		that.status.statusMessageVisible = false;
		that.status.message = "";
	}
  }

  that.chartData = { baseline: {}, interventions: {}, comparison: {}, animated: {}};
  that.chartMetaData = { baseline: {}, interventions: {}, comparison: {}, animated: {}};
  that.setChartData = function(chartConfig, chartDataGetter) {
	  var chartData = chartDataGetter()
	  if(chartConfig.chartType === 'Line'){
		  chartData[chartConfig.id] = createChart(chartConfig.id, chartConfig.title, chartConfig.hLabel, chartConfig.vLabel, chartConfig.colors);
	  }else if(chartConfig.chartType === 'StackedArea'){
		  chartData[chartConfig.id] = createStackedAreaChart(chartConfig.id, chartConfig.title, chartConfig.hLabel, chartConfig.vLabel, chartConfig.colors);
	  }else if (chartConfig.chartType === 'StackedColumn'){
		  chartData[chartConfig.id] = createStackedColumnChart(chartConfig.id, chartConfig.title, chartConfig.hLabel, chartConfig.vLabel, chartConfig.colors);
	  }else if (chartConfig.chartType === 'Column'){
		  chartData[chartConfig.id] = createColumnChart(chartConfig.id, chartConfig.title, chartConfig.hLabel, chartConfig.vLabel, chartConfig.colors);
	  }else if(chartConfig.chartType === 'LineAsTimeline'){
		  chartData[chartConfig.id] = createLineAsTimelineChart(chartConfig.id, chartConfig.title, chartConfig.hLabel, chartConfig.vLabel, chartConfig.colors);
	  }
  }
  for(var i=0; i<RESULTS_CONFIG.baseline.length; i++){
	  that.setChartData(RESULTS_CONFIG.baseline[i], function() {return that.chartData.baseline;});
	  that.chartMetaData.baseline[RESULTS_CONFIG.baseline[i].id] = createTable();
  }
  for(var i=0; i<RESULTS_CONFIG.intervention.length; i++){
	  that.setChartData(RESULTS_CONFIG.intervention[i], function() {return that.chartData.interventions;});
	  that.chartMetaData.interventions[RESULTS_CONFIG.intervention[i].id] = createTable();
  }
  for(var i=0; i<RESULTS_CONFIG.comparison.length; i++){
	  that.setChartData(RESULTS_CONFIG.comparison[i], function() {return that.chartData.comparison;});
	  that.chartMetaData.comparison[RESULTS_CONFIG.comparison[i].id] = createTable();
  }
  for(var i=0; i<RESULTS_CONFIG.animated.length; i++){
	  that.setChartData(RESULTS_CONFIG.animated[i], function() {return that.chartData.animated;});
	  that.chartMetaData.animated[RESULTS_CONFIG.animated[i].id] = createTable();
  }
	
  that.pendingRequests = (function() {
	 var that = {};
	 var latestSubmition = new Date().getTime();
	 that.submit = function(mytime) {
		 if(mytime > latestSubmition){
			 latestSubmition = mytime;
		 }
	 }
	 that.iamlatest = function(mytime) {
		 return mytime >= latestSubmition;
	 }
	 
	 return that;
  }());
  that.rerunSimulationRaw = function(config) {
	  $.isFunction(window.history.pushState) && window.history.pushState("", "", '/model/custom?config=' + encodeURI(JSON.stringify(config)));
	  
	  $rootScope.$apply(function(){
		  var mytime = new Date().getTime();
		  that.pendingRequests.submit(mytime);
		  
		  that.updateStatus('running');
		  
		  $http.defaults.headers.common.postcontentType = 'application/json; charset=utf-8';
		  $http.post('/model/run', JSON.stringify(config)).
		  success(function(data, status, headers, config) {
			  if(that.pendingRequests.iamlatest(mytime)){
				  that.animationFrame = 0;
				  that.updateChartData(data);
				  $rootScope.$broadcast( "simulationRan" );
				  that.updateStatus('done');
				  that.updateOverviewData(data);
			  }
		  }).
		  error(function(error, status, headers, config) {
			  console.log(status);
			  console.log(error.responseText);
			  console.log(error);
			  that.updateStatus('done');
		  });
	  });
  };
  //coalesce multiple calls to updatemodel, don't make a request more often than once per 300ms
  that.rerunSimulation = $.debounce(200, that.rerunSimulationRaw);
  
  that.updateAnimatedCharts = function() {
	  for(var i=0; i<RESULTS_CONFIG.animated.length; i++){
		  var c = RESULTS_CONFIG.animated[i];
		  if (FISHERY.animatedChartData) {
			  that.animationLength = FISHERY.animatedChartData[c.id].length - 1;
			  that.chartData.animated[c.id].data = FISHERY.animatedChartData[c.id][that.animationFrame];
			  
			  var maxY = 0;
			  var minY = 0;
			  
			  for (var frameNum=0;frameNum<FISHERY.animatedChartData[c.id].length;frameNum++) {
				  var frame = FISHERY.animatedChartData[c.id][frameNum]
				  for (var seriesNum = 1; seriesNum < frame.length;seriesNum++) {
					  var series = frame[seriesNum];
					  
					  var positive = 0;
					  var negative = 0;
					  
					  for (var dataNum = 1;dataNum < series.length; dataNum++) {
						  if (series[dataNum] > 0) {
							  positive += series[dataNum]
						  } else {
							  negative += series[dataNum]
						  }
					  }
					  
					  if (positive > maxY) { maxY = positive; }
					  if (negative < minY) { minY = negative; }
				  }
			  }
			  
			  that.chartData.animated[c.id].options.vAxis.maxValue = maxY;
			  that.chartData.animated[c.id].options.vAxis.minValue = minY;
		  }
	  }
  }
  
  var updateAxis = function(chart, min, max) {
	  chart.options.vAxis.viewWindow = {
			  'min': min, 'max': max
	  };
	  
	  FISHERY.updateChartView(chart, min, max);
  }
  
  var matchChartAxes = function(chartA, chartB){
	  var minMax = null;
	  var chartType = chartA.type;
	  
	  var vAxisRange = null;
	  if(chartType === "LineChart" || chartType === "LineAsTimeline" || chartType === 'Column'){
		  var alwaysShowZero = !FISHERY.scientist;
		  vAxisRange = ARRAY.minAndMaxReduce([TABLE.minAndMax(chartA.data, alwaysShowZero), TABLE.minAndMax(chartB.data, alwaysShowZero)]);
	  } else if(chartType === "AreaChart"){
		  vAxisRange = ARRAY.minAndMaxReduce([TABLE.posAndNeg(chartA.data, alwaysShowZero), TABLE.posAndNeg(chartB.data)]);
	  } else {
	  	  throw "unrecognized kind of chart type " + chartType;
	  }
	  updateAxis(chartA, vAxisRange.min, vAxisRange.max);
	  updateAxis(chartB, vAxisRange.min, vAxisRange.max);
  }

  var updateChartAxes = function(chart){
	  var minMax = null;
	  var chartType = chart.type;
	  
	  var vAxisRange = null;
	  if(chartType === "LineChart" || chartType === "LineAsTimeline" || chartType === 'Column'){
		  var alwaysShowZero = !FISHERY.scientist;
		  vAxisRange = ARRAY.minAndMaxReduce([TABLE.minAndMax(chart.data, alwaysShowZero)]);
	  } else if(chartType === "AreaChart"){
		  vAxisRange = ARRAY.minAndMaxReduce([TABLE.posAndNeg(chart.data, alwaysShowZero)]);
	  } else {
	  	  throw "unrecognized kind of chart type " + chartType;
	  }
	  updateAxis(chart, vAxisRange.min, vAxisRange.max);
  }

  that.updateChartData = function(chartData) {
	  for(var i=0; i<RESULTS_CONFIG.baseline.length; i++){
		  var c = RESULTS_CONFIG.baseline[i];
		  that.chartData.baseline[c.id].data = chartData.baseline.tableData[c.id];
		  that.chartMetaData.baseline[c.id].data = FISHERY.createChartMetaData(chartData.baseline.tableData[c.id]);
		  
		  that.chartData.interventions[c.id].data = chartData.interventions.tableData[c.id];
		  that.chartMetaData.interventions[c.id].data = FISHERY.createChartMetaData(chartData.interventions.tableData[c.id]);

		  if(c.chartType === 'LineAsTimeline'){
			  // JSON does not support NaN, convert here
			  TABLE.replace(that.chartData.baseline[c.id].data, -1, Number.NaN);
			  TABLE.replace(that.chartData.interventions[c.id].data, -1, Number.NaN);
		  }

		  if(!isNaN(c.min) && !isNaN(c.max)){
			  updateAxis(that.chartData.baseline[c.id], c.min, c.max);
			  updateAxis(that.chartData.interventions[c.id], c.min, c.max);
		  }else{
			  matchChartAxes(that.chartData.baseline[c.id], that.chartData.interventions[c.id]);
		  }
	  }
	  
	  for(var i=0; i<RESULTS_CONFIG.comparison.length; i++){
		  var c = RESULTS_CONFIG.comparison[i];
		  that.chartData.comparison[c.id].data = chartData.comparison.tableData[c.id];
		  that.chartMetaData.comparison[c.id].data = FISHERY.createChartMetaData(chartData.comparison.tableData[c.id]);
		  
		  if(!isNaN(c.min) && !isNaN(c.max)){
			  updateAxis(that.chartData.comparison[c.id], c.min, c.max);
			  updateAxis(that.chartData.comparison[c.id], c.min, c.max);
		  }else{
			  updateChartAxes(that.chartData.comparison[c.id]);
		  }
	  }
	  
	  FISHERY.animatedChartData = chartData.animated;
	  that.updateAnimatedCharts();
  }
  
  that.updateOverviewData = function(data) {
	  for(var i=0; i<FISHERY.OVERVIEW_IDS.length; i++){
		  var c = FISHERY.OVERVIEW_IDS[i];
		  that.overview[c] = data.overview[c];
	  }
  }
  
  return that;
});