/**
 * Created by Jesse on 2/12/14.
 */
(function() {
    'use strict';

    goog.provide('gn_schematronadmin_controller');
    goog.require('gn_schematronadmin_editcriteriadirective')
    var module = angular.module('gn_schematronadmin_controller', ['gn_schematronadmin_editcriteriadirective']);

    /**
     * GnAdminMetadataController provides administration tools
     * for metadata and templates
     */
    module.controller('GnSchematronEditCriteriaController', [
        '$scope', '$http',
        function($scope, $http) {
            $scope.selection = {
                schema: null,
                schematron : null,
                group: null
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
                    $scope.selection.group = data.length > 0 ? data[0] : null;
                }).error(function(data, code){
                    alert("Error occured during loading schematron criteria");
                });
            };

            $scope.requirements=['REQUIRED', 'REQUEST_ONLY', 'DISABLED'];

            $scope.criteriaEditor = {
                html: null,
                object: null
            };
        }]);
})();
