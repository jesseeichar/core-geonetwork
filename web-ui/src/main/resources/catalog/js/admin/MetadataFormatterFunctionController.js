/**
 * Created by Jesse on 2/12/14.
 */
(function() {
  'use strict';

  goog.provide('gn_metadataformatterfunction_controller');
  var module = angular.module('gn_metadataformatterfunction_controller', []);

  /**
   * GnMetadataFormatterController provides administration tools
   * for metadata formatters and formatter functions.
   */
  module.controller('GnMetadataFormatterFunctionController', [
    '$scope', '$routeParams', '$location', '$translate', '$http',
    function($scope, $routeParams, $location, $translate, $http) {
      var loadFunctions;
      $scope.userSelected = null;
      $scope.edited = {
        namespace: '',
        name: '',
        functionBody: ''
      };
      $scope.userSelected = null;
      $scope.functions = [];

      loadFunctions = function() {
        $http({
          method: 'GET',
          url: 'md.formatter.function.list@json'
        }).success(function(data){
          if (!data || data === "null") {
            $scope.functions = [];
          } else {
            $scope.functions = data;
          }
        }).error(function(data){
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate('formatterListError'),
            error: data.jqXHR.responseJSON,
            timeout: 0,
            type: 'danger'});
        });
      };
      $scope.selectFunction = function (formatterFunction) {
        $scope.userSelected = formatterFunction;
        $scope.edited = angular.copy(formatterFunction);
      };

      $scope.update = function() {
        $http({
          method: 'POST',
          url: 'md.formatter.function.set@json',
          params: {
            namespace: $scope.edited.namespace,
            name: $scope.edited.name,
            functionBody: $scope.edited.functionBody
          }
        }).success(function(){
          var i, f;
          if ($scope.userSelected) {
            angular.copy($scope.edited, $scope.userSelected);
            loadFunctions();
            for (i = 0; i < $scope.functions.length; i++) {
              f = $scope.functions[i];
              if (f.namespace === $scope.edited.namespace &&
                  f.name === $scope.edited.name) {
                $scope.userSelected = f;
                break;
              }
            }
          }
        }).error(function(data){
          $rootScope.$broadcast('StatusUpdated', {
            title: $translate('formatterFunctionSetFailure'),
            error: data.jqXHR.responseJSON,
            timeout: 0,
            type: 'danger'});
        });
      };

      $scope.deleteFunction = function () {
        if ($scope.userSelected) {
          $http({
            method: 'GET',
            url: 'md.formatter.function.delete',
            params: {
              namespace: $scope.edited.namespace,
              name: $scope.edited.name
            }
          }).success(function() {
            loadFunctions();
            $scope.edited = {
              namespace: $scope.userSelected.namespace,
              name: '',
              functionBody: ''
            };
            $scope.userSelected = null;
          }).error(function(data){
            $rootScope.$broadcast('StatusUpdated', {
              title: $translate('formatterFunctionDeleteFailure'),
              error: data.jqXHR.responseJSON,
              timeout: 0,
              type: 'danger'});
          });
        }
      };
      $scope.expandNamespace = function (functionNamespace) {
        $('#collapsable_'+functionNamespace).collapse('toggle');
        $scope.edited = {
          namespace: functionNamespace,
          name: '',
          functionBody: ''
        };
      };
      loadFunctions();
    }]);
}());
