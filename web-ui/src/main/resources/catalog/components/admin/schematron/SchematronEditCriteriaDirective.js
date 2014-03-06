(function () {
    "use strict";
    goog.provide('gn_schematronadmin_editcriteriadirective');
    goog.require('gn_schematronadminservice');

    var module = angular.module('gn_schematronadmin_editcriteriadirective', ['gn_schematronadminservice']);


    /**
     * Display harvester identification section with
     * name, group and icon
     */
    module.directive('gnCriteriaEditor', ['gnSchematronAdminService', '$compile', '$templateCache', '$q', '$translate', '$timeout',
        function (gnSchematronAdminService, $compile, $templateCache, $q, $translate, $timeout) {
            return {
                restrict: 'E',
                replace: true,
                transclude: false,
                scope: {
                    original: '=criteria',
                    schema: '=',
                    group: '=',
                    lang: '='
                },

                templateUrl: '../../catalog/components/admin/schematron/partials/criteria-viewer.html',
                link: function (scope, element, attrs) {
                    var findValueInput = function () {
                        return element.find("input.form-control")
                    }
                    scope.criteria = angular.copy(scope.original);

                    // Keep a map of the values that belong to each criteria type
                    // so when a type is changed the value field can be updated with a
                    // value that makes sense for the type.
                    var criteriaTypeToValueMap = {
                        currentType__ : scope.criteria.uitype
                    };
                    scope.criteriaTypes = {};
                    if (scope.criteria.type === 'NEW') {
                        scope.criteriaTypes.NEW = {name: 'NEW', type: 'NEW'};
                        criteriaTypeToValueMap.NEW = '';
                    }
                    for (var i = 0; i < scope.schema.criteriaTypes.length; i++) {
                        var type = scope.schema.criteriaTypes[i];
                        scope.criteriaTypes[type.name] = type;
                        criteriaTypeToValueMap[type.name] = '';
                    }
                    scope.isDirty = function () {
                        return scope.criteria.uitype !== scope.original.uitype || scope.criteria.uivalue !== scope.original.uivalue;
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
                        if (scope.schema.editObject) {
                            scope.schema.editObject.editing = false;
                        }
                        scope.schema.editObject = scope;
                        scope.editing = true;
                        scope.updateTypeAhead();
                    };
                    scope.updateValueField = function () {
                        var oldType = criteriaTypeToValueMap.currentType__;
                        var newType = scope.criteria.uitype;
                        var oldValue = scope.criteria.uivalue;
                        var newValue = criteriaTypeToValueMap[newType];

                        criteriaTypeToValueMap.currentType__ = newType;
                        criteriaTypeToValueMap[oldType] = oldValue;
                        scope.criteria.uivalue = newValue;
//                        $timeout(function(){findValueInput().select()});

                    };
                    scope.$watch('editing', function (newValue){
                        if (newValue) {
                            var input = findValueInput();
                            input.focus();
                            input.select();
                        }
                    });
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
                        scope.schema.editObject = null;
                        scope.criteria = angular.copy(scope.original, scope.criteria);

                        scope.editing = false;
                    };

                    scope.deleteCriteria = function () {
                        gnSchematronAdminService.criteria.remove(scope.criteria, scope.group);
                    };

                    scope.updateTypeAhead = function () {
                        var input = findValueInput();
                        input.typeahead('destroy');

                        var criteriaTypeTypeAhead = scope.criteriaTypes[scope.criteria.uitype].typeahead;

                        if (criteriaTypeTypeAhead && criteriaTypeTypeAhead.url) {
                            var typeaheadOptions = {
                                name: scope.criteria.uitype
                            };

                            var parseResponseFunction = function(parsedResponse) {
                                var selectRecordArray = criteriaTypeTypeAhead.selectRecordArray;
                                var selectValueFunction = criteriaTypeTypeAhead.selectValueFunction;
                                var selectLabelFunction = criteriaTypeTypeAhead.selectLabelFunction;
                                var selectTokensFunction = criteriaTypeTypeAhead.selectTokensFunction;

                                function doEval(propertyName) {
                                    if (typeof(criteriaTypeTypeAhead[propertyName]) != "function") {
                                        eval(propertyName + " = function "+criteriaTypeTypeAhead[propertyName]);
                                    }
                                }
                                doEval('selectRecordArray');
                                doEval('selectLabelFunction');
                                doEval('selectValueFunction');
                                if (criteriaTypeTypeAhead.selectTokensFunction) {
                                    doEval('selectTokensFunction');
                                } else {
                                    selectTokensFunction = function (record, scope) {
                                        return selectLabelFunction(record, scope).split(/\s+/g);
                                    };

                                    criteriaTypeTypeAhead.selectTokensFunction = selectTokensFunction;
                                }

                                var data = selectRecordArray(parsedResponse, scope);

                                var finalData = [];
                                for (var i = 0; i < data.length; i++) {
                                    var record = data[i];
                                    var name = selectLabelFunction(record, scope);
                                    var value = selectValueFunction(record, scope);
                                    finalData.push({
                                        value: name,
                                        data: value,
                                        tokens: selectTokensFunction(record, scope)
                                    })
                                }

                                return finalData;
                            };

                            if (criteriaTypeTypeAhead.cacheTime && criteriaTypeTypeAhead.cacheTime > 0) {
                                typeaheadOptions.prefetch = {
                                    url: criteriaTypeTypeAhead.url,
                                    ttl: parseInt(criteriaTypeTypeAhead.cacheTime),
                                    filter: parseResponseFunction
                                };
                            }

                            input.typeahead (typeaheadOptions);
                            input.on("typeahead:selected", function(event, data){
                                scope.criteria.value = data.data;
                                input.focus();
                            });
                        }
                    };
                }
            };
        }]);
})();
