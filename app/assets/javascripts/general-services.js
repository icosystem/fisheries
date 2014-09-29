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
angular.module('generalservices', [])
.service('util', function(){
	var it = {};
	
	//https://github.com/angular/angular.js/issues/5892
	//http://jsfiddle.net/TXazw/5/Edit/
	//iterate over all properties and convert to number
	//make AngularJS treat our input type="range" form elements as numeric
	it.fixTypes = function(thing) {
		var parsed = parseFloat(thing);
		if (thing !== null && typeof thing === 'object') { /* thing is an object http://stackoverflow.com/questions/8511281/check-if-a-variable-is-an-object */
			for (var prop in thing) {
				if(thing.hasOwnProperty(prop)){
					thing[prop] = it.fixTypes(thing[prop]);
				}
			}
			return thing;
		}
		if(!isNaN(parsed)){
			return parsed;
		}
		return thing;
	}
	
	return it;
})

.service('remote', function($http, $rootScope){
	var it = {};
	
    var getJsonPlus = function(url, input, before, success, failure) {
        $rootScope.$apply(function(){
            before();
            
            $http.defaults.headers.common.postcontentType = 'application/json; charset=utf-8';
            $http.post(url, JSON.stringify(input)).
                success(function(results, status, headers, input) {
                    success(results);
                }).
                error(function(error, status, headers, input) {
                    failure(error, status, headers, input)
                });
        });
    };

    //coalesce multiple calls to service, don't make a request too often
    it.getJsonPlusDebounced = function(delay) {
        return $.debounce(delay, getJsonPlus);
    };

    return it;
});