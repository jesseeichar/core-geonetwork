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
        '$scope', '$routeParams', '$location', 'gnSchematronAdminService',
        function($scope, $routeParams, $location, gnSchematronAdminService) {
            var updateLocation = function (schema, schematron, group) {
                var path = '/metadata/schematron';
                if (schema && schematron) {
                    path += '/' + schema.name + '/' + schematron.id;
                }
                if (schema && schematron && group) {
                    path += '/' + group.id.name;
                }
                $location.path(path);
                return path;
            };

            var loadCriteria = function() {
                $scope.schematronGroups = null;
                gnSchematronAdminService.group.list($scope.selection.schematron.id, function (data) {
                    $scope.schematronGroups = data;
                    $scope.selection.group = data.length > 0 ? data[0] : null;
                });
            };

            $scope.selection = {
                schema: null,
                schematron : null,
                group: null
            };


            $scope.schematronGroups = null;
            $scope.isShowSchematronGroupHelp = false;
            $scope.isSelected = function(schematron) {return $scope.selection.schematron === schematron;};
            $scope.selectSchematron = function(schema, schematron) {
                if ($scope.selection.schema !== schema || $scope.selection.schematron !== schematron) {
                    updateLocation(schema, schematron);
                }
            };

            $scope.requirements=['REQUIRED', 'REQUEST_ONLY', 'DISABLED'];

            gnSchematronAdminService.criteriaTypes.list(function(data){
                $scope.schematrons = data;

                if ($routeParams.schemaName) {
                    var findSchema = function(schemaName) {
                        for (var i = 0; i < $scope.schematrons.length; i++) {
                            var schemaDef = $scope.schematrons[i];
                            if (schemaDef.name === $routeParams.schemaName) {
                                return schemaDef;
                            }
                        }
                    };
                    var findSchematron = function (schemaDef, schematronId) {
                        if (schematronId) {
                            for (var i = 0; i < schema.schematron.length; i++) {
                                var schematron = schema.schematron[i];
                                if (schematronId === schematron.id) {
                                    return schematron;
                                }
                            }
                        }
                    };

                    var schema = findSchema($routeParams.schemaName);

                    if (!schema) {
                        updateLocation();
                        return;
                    }

                    var schematron = findSchematron(schema, $routeParams.schematronId);

                    if (!schematron) {
                        if (schema.schematron.length == 0) {
                            updateLocation();
                        } else {
                            schematron = schema.schematron[0];
                        }
                    }

                    if (schematron) {
                        $scope.selection.schema = schema;
                        $scope.selection.schematron = schematron;
                        updateLocation(schema, schematron);
                        loadCriteria();
                    }
                }
            });
        }]);
})();
