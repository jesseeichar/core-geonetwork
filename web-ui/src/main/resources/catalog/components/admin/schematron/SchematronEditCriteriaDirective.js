(function () {
    "use strict";
    goog.provide('gn_schematronadmin_editcriteriadirective');
    goog.require('gn_schematronadminservice');

    var module = angular.module('gn_schematronadmin_editcriteriadirective', ['gn_schematronadminservice']);


    /**
     * Display harvester identification section with
     * name, group and icon
     */
    module.directive('gnCriteriaEditor', ['gnSchematronAdminService', '$compile', '$templateCache', '$q', '$translate', '$http',
        function (gnSchematronAdminService, $compile, $templateCache, $q, $translate, $http) {

            return {
                restrict: 'E',
                replace: true,
                transclude: false,
                scope: {
                    original: '=criteria',
                    schema: '=',
                    group: '='
                },

                templateUrl: '../../catalog/components/admin/schematron/partials/criteria-viewer.html',
                link: function (scope, element, attrs) {
                    scope.criteria = {
                        id: scope.original.id,
                        type: scope.original.type,
                        value: scope.original.value

                    };
                    scope.isDirty = function () {
                        return scope.criteria.type !== scope.original.type || scope.criteria.value !== scope.original.value;
                    };
                    scope.calculateClassOnDirty = function(whenDirty, whenClean) {
                        if (scope.isDirty()) {
                            return whenDirty;
                        } else {
                            return whenClean;
                        }
                    }
                    scope.editing = false;
                    scope.startEditing = function () {
                        scope.editing = true;
                    };
                    scope.describeCriteria = function () {
                        switch (scope.original.type) {
                            case 'ALWAYS_ACCEPT':
                                return "Always evaluates to true";
                            case 'NEW':
                                return $translate("new");
                            case 'XPATH':
                                return "Evaluates to true when xpath is satisfied: " + scope.original.value;
                            case 'GROUP':
                                return "Evaluates to true when metadata is owned by group: " + scope.original.value;
                            default:
                                return "Type: " + scope.original.type + " evaluationValue: " + scope.original.value;
                        }
                    };

                    scope.cancelEditing = function () {
                        scope.criteria.type = scope.original.type;
                        scope.criteria.value = scope.original.value;

                        scope.editing = false;
                    };

                    scope.newlyFocused = false;
                    scope.handleBlur = function() {
                        if (!scope.newlyFocused) {
                            scope.editing = false;
                        }
                    };
                    scope.handleFocus = function() {
                        scope.newlyFocused = true;
                        setTimeout(function() {scope.newlyFocused = false;}, 400)
                    };

                    scope.deleteCriteria = function () {
                        gnSchematronAdminService.criteria.remove(scope.criteria, scope.group);
                    };
                }
            };
        }]);
})();
