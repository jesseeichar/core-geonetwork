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
        '$scope', 'gnSchematronAdminService',
        function($scope, gnSchematronAdminService) {
            $scope.selection = {
                schema: null,
                schematron : null,
                group: null
            };

            $scope.schematronGroups = null;
            $scope.isShowSchematronGroupHelp = false;
            $scope.isSelected = function(schematron) {return $scope.selection.schematron === schematron;};
            $scope.selectSchematron = function(schema, schematron) {
                if ($scope.selection.schematron !== schematron) {
                    $scope.selection.schema = schema;
                    $scope.selection.schematron = schematron;
                    $scope.loadCriteria();
                }
            };

            $scope.loadCriteria = function() {
                $scope.schematronGroups = null;
                gnSchematronAdminService.group.list($scope.selection.schematron.id, function (data) {
                    $scope.schematronGroups = data;
                    $scope.selection.group = data.length > 0 ? data[0] : null;
                });
            };

            $scope.requirements=['REQUIRED', 'REQUEST_ONLY', 'DISABLED'];
        }]);
})();
