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
angular.module('numberFilters', []);

angular.module('numberFilters').filter('prettynumber', function() {
	return function(num) {
		return FORMAT.pretty(num);
	};
});
angular.module('numberFilters').filter('interventionAsPercent', [ '$filter', function($filter) {
	return function(intervention) {
		return $filter('number')(intervention.endValue * 100, 1) + '%';
	};
}]);

/**
 * Example usage <span>{{someNumber | percentage:2}}</span>
 */
// http://stackoverflow.com/questions/13668440/how-to-make-a-percent-formatted-input-work-on-latest-angularjs
angular.module('numberFilters').filter('percentage', [ '$filter', function($filter) {
			return function(input, decimals) {
				if (decimals) {
					return $filter('number')(input * 100, decimals) + '%';
				}
				return $filter('number')(input * 100) + '%';
			};
		} ]);

