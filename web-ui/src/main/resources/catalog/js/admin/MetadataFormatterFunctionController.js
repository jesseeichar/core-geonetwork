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
        function: ''
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
        }).error(function(){

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
            function: $scope.edited.function
          }
        }).success(function(){
          if ($scope.userSelected) {
            angular.copy($scope.edited, $scope.userSelected);
            loadFunctions();
            for (var i = 0; i < $scope.functions.length; i++) {
              var f = $scope.functions[i];
              if (
                  f.namespace === $scope.edited.namespace &&
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

      $scope.delete = function () {
        if ($scope.userSelected) {
          $http({
            method: 'GET',
            url: 'md.formatter.function.set@json',
            params: {
              namespace: $scope.edited.namespace,
              name: $scope.edited.name,
              function: $scope.edited.function
            }
          }).success(function() {
            var idx = $scope.functions.indexOf($scope.userSelected);
            if (idx < 0) {
              $scope.functions.splice(idx, 1);
            }
          }).error(function(data){
            $rootScope.$broadcast('StatusUpdated', {
              title: $translate('formatterFunctionDeleteFailure'),
              error: data.jqXHR.responseJSON,
              timeout: 0,
              type: 'danger'});
          });
        }
      };

      loadFunctions();
    }]);
}());
