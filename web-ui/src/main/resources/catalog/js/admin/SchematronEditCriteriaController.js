/**
 * Created by Jesse on 2/12/14.
 */
(function() {
    'use strict';

    goog.provide('gn_schematroneditcriteria_controller');

    var module = angular.module('gn_schematroneditcriteria_controller', []);

    /**
     * GnAdminMetadataController provides administration tools
     * for metadata and templates
     */
    module.controller('GnSchematronEditCriteriaController', [
        '$scope', '$http',
        function($scope, $http) {
            $scope.selection = {
                schema: null,
                schematron : null
            };

            $scope.schematronGroups = null;
            $scope.isShowSchematronGroupHelp = false;
            $scope.isSelected = function(schematron) {return $scope.selection.schematron === schematron;};
            $scope.selectSchematron = function(schema, schematron) {
                $scope.selection.schema = schema;
                $scope.selection.schematron = schematron;
                $scope.loadCriteria();
            };

            $scope.loadCriteria = function() {
                $scope.schematronGroups = null;
                $http({
                    method: 'GET',
                    url: 'admin.schematroncriteriagroup.list@json',
                    params: {
                        includeCriteria: true,
                        schematronId: $scope.selection.schematron.id
                    }
                }).success(function (data) {
                    $scope.schematronGroups = data;
                }).error(function(data, code){
                    alert("Error occured during loading schematron criteria");
                });
            };

            $scope.describeCriteria = function(criteria){
                switch (criteria.type) {
                    case 'ALWAYS_ACCEPT':
                        return "Always evaluates to true";
                    case 'XPATH':
                        return "Evaluates to true when xpath is satisfied: "+criteria.value;
                    case 'GROUP':
                        return "Evaluates to true when metadata is owned by group: "+criteria.value;
                    default:
                        return "Type: " + criteria.type + " evaluationValue: "+criteria.value;
                }
            };
        }]);

})();
