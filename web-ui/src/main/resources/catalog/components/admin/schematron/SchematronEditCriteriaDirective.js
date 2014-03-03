(function () {
    "use strict";
    goog.provide('gn_schematronadmin_editcriteriadirective');
    goog.require('gn_schematronadminservice');

    var module = angular.module('gn_schematronadmin_editcriteriadirective', ['gn_schematronadminservice']);


    /**
     * Display harvester identification section with
     * name, group and icon
     */
    module.directive('gnCriteriaEditor', ['gnSchematronAdminService', '$compile', '$templateCache', '$q', '$translate',
        function (gnSchematronAdminService, $compile, $templateCache, $q, $translate) {

            return {
                restrict: 'E',
                replace: true,
                transclude: false,
                scope: {
                    original: '=criteria',
                    editor: '=',
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
                    scope.startEditing = function () {
                        scope.editor.object = scope.criteria;
                        scope.asyncLoadTemplate(scope.editor).then(function success(linkFunc) {
                            if (scope.editor.html) {
                                scope.cancelEditing();
                            }
                            scope.editor.html = linkFunc(scope);
                            scope.editor.linkFunction = linkFunc;

                            element.after(scope.editor.html);
                            scope.editor.html.find("input").focus();
                        },function error(msg){
                            alert(msg)}
                        );
                    };
                    scope.asyncLoadTemplate = function(editor) {
                        var deferred = $q.defer();
                        if (scope.editor.linkFunction) {
                            deferred.resolve(scope.editor.linkFunction);
                        } else {
                            var templateUrl = '../../catalog/components/admin/schematron/partials/criteria-editor.html';
                            var template = $templateCache.get(templateUrl);
                            if (!template) {
                                $http.get(templateUrl).then(
                                    function success(response){
                                        var linkFunc = $compile(angular.element(response.data));
                                        deferred.resolve(linkFunc);
                                    },
                                    function failure(){
                                        deferred.reject('Failed to load template from '+templateUrl)
                                });
                            } else {
                                var linkFunc = $compile(angular.element(template));
                                deferred.resolve(linkFunc);
                            }
                        }

                        return deferred.promise;
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

                    scope.disposeOfEditHtml = function () {
                        scope.editor.html.remove();
                        scope.editor.html = null;
                        scope.editor.object = null;
                    }
                    scope.cancelEditing = function () {
                        scope.criteria.type = scope.original.type;
                        scope.criteria.value = scope.original.value;

                        scope.disposeOfEditHtml();
                    };

                    scope.newlyFocused = false;
                    scope.handleBlur = function() {
                        if (!scope.newlyFocused) {
                            scope.disposeOfEditHtml();
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
