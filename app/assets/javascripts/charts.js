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

FISHERY.createChartMetaData = function(data) {
	var valid = function(data) {
		if(_.isUndefined(data) || !_.isArray(data)){
			return false;
		}

		if(data.length > 1 && data[0].length > 1){
			var cols = data[0].length;
			// all rows have same dimension
			return _.every(data, function(v) { return _.isArray(v) && v.length == cols; }); 
		}
		
		return false;
	}
	
	if(typeof(_) == 'undefined'){
		console.log('requires underscore - http://underscorejs.org');
		return data;
	}
	if(!valid(data)){
		return data;
	}
	
	var stats = [['name','pct diff','min','max','start','end','diff']];
	var f = FORMAT.pretty;
	
	cols = _.zip.apply(_, data); //transpose http://underscorejs.org/#zip
	_.each(cols, function(col){
		var header = _.first(col);
		var data = _.rest(col);
		
		var min = _.min(data);
		var max = _.max(data);
		var start = _.first(data);
		var end = _.last(data);
		var diff = end - start;
		var pctDiff = diff / Math.abs(start) * 100;
		
		stats.push([header,f(pctDiff)+'%',f(min),f(max),f(start),f(end),f(diff)]);
	});
	
	return stats;
}

FISHERY.chartOptions = function(id, title, hLabel, vLabel, colors) {
	return {
		'explorer': { actions: ['dragToZoom', 'rightClickToReset'], keepInBounds: true, maxZoomIn: .001 },
		'title' : title,
		'titleTextStyle' : {
			color : '#333',
			bold : true,
			italic : false,
			fontSize : 16
		},
		'width' : 515,
		'height' : 300,
		'chartArea': {left:100, width: 265}, 
		'colors' :  colors,
		
		'hAxis' : {
			title : hLabel,
			gridlines : {
				color : 'transparent'
			},
			baselineColor : '#ccc',
			titleTextStyle : {
				color : '#333',
				bold : false,
				italic : false,
				fontSize : 12
			},
			textStyle : {
				color : '#333',
				bold : false,
				italic : false,
				fontSize : 9
			}
		},
		'vAxis' : {
			title : vLabel,
			minValue : 0,
			gridlines : {
				color : 'transparent'
			},
			baselineColor : '#ccc',
			titleTextStyle : {
				color : '#333',
				bold : false,
				italic : false,
				fontSize : 12
			},
			textStyle : {
				color : '#333',
				bold : false,
				italic : false,
				fontSize : 9
			},
			format : '####'
		},
		'legend' : {
			'textStyle' : {
				fontSize : 10,
				bold : false,
				italic : false
			}
		}
	};
}

FISHERY.tableOptions = function() {
	return {};
}

FISHERY.updateChartView = function(chart, min, max) {
	var million  = 1000000.0;
	var appendTo = function(str, suffix){
		if(_.isString(str) && _.isString(suffix)){
			if(str.length >= suffix.length && str.substring(str.length-suffix.length)==suffix){
				//already contains suffix
				return str;
			}
			return str + suffix;
		}
		return str;
	}
	
	if(max > million){
		var myFunc = function(col) {
			return function(d,r){
				var scaled = d.getValue(r, col) / million;
				/* f is displayed text in tooltip, v is actual value used to plot values */
				return {v: scaled, f: scaled + ' million'};
			}
		}
		var colspec = [0]; //skip column 0, time
		for(var i=1; i<chart.data[0].length; i++){
			colspec.push({calc:myFunc(i),type:'number',label:chart.data[0][i]});
		}
		chart.view = {columns: colspec}; 
//		chart.options.vAxis.format = '#M'; //format axis labels
		
		chart.options.vAxis.title = appendTo(chart.options.vAxis.title, ' (M)');
		chart.options.vAxis.viewWindow = { 'min': min/million, 'max': max/million };
	}
}