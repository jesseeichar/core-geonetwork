/**
 * Created by Jesse on 2/12/14.
 */
(function() {
  'use strict';

  goog.provide('gn_metadataformatter_controller');
  var module = angular.module('gn_metadataformatter_controller', []);

  /**
   * GnMetadataFormatterController provides administration tools
   * for metadata formatters and formatter functions.
   */
  module.controller('GnMetadataFormatterController', [
    '$scope', '$routeParams', '$location', '$translate', '$http',
    function($scope, $routeParams, $location, $translate, $http) {
      var loadFormatter, loadFormatterError;

      $scope.formatterSelected = null;
      $scope.formatters = [];
      $scope.formatterFiles = [];
      $scope.metadataId = '';

      /**
       * Load list of logos
       */
      loadFormatter = function() {
        $scope.formatters = [];
        $http.get('md.formatter.list@json').success(function(data) {
          if (data !== 'null') {
            $scope.formatters = data; // TODO: check multiple
          }
        }).error(function(data) {
          // TODO
        });
      };

      /**
       * Callback when error uploading file.
       */
      loadFormatterError = function(e, data) {
        $rootScope.$broadcast('StatusUpdated', {
          title: $translate('formatterUploadError'),
          error: data.jqXHR.responseJSON,
          timeout: 0,
          type: 'danger'});
      };
      /**
       * Configure logo uploader
       */
      $scope.formatterUploadOptions = {
        autoUpload: true,
        done: loadFormatter,
        fail: loadFormatterError
      };

      $scope.listFormatterFiles = function(f) {
        //md.formatter.files?id=sextant
        $scope.formatterFiles = [];
        $http.get('md.formatter.files@json?id=' + f).success(function(data) {
          if (data !== 'null') {
            // Format files
            angular.forEach(data.file, function(file) {
              file.dir = '.'; // File from root directory
              file['@path'] = file['@name'];
              $scope.formatterFiles.push(file);
            });
            angular.forEach(data.dir, function(dir) {
              // One file only, convert to array
              if (dir.file) {
                if (!angular.isArray(dir.file)) {
                  dir.file = [dir.file];
                }
              }
              angular.forEach(dir.file, function(file) {
                file.dir = dir['@name'];
                $scope.formatterFiles.push(file);
              });
            });
            $scope.selectedFile = $scope.formatterFiles[0];
          }
        }).error(function(data) {
          // TODO
        });
      };

      $scope.selectFormatter = function(f) {
        //md.formatter.files?id=sextant
        $scope.formatterSelected = f;
        $scope.listFormatterFiles(f);
      };


      $scope.downloadFormatter = function(f) {
        location.replace('md.formatter.download?id=' + f, '_blank');
      };

      $scope.formatterDelete = function(f) {
        $http.get('md.formatter.remove?id=' + f)
          .success(function(data) {
            $scope.formatterSelected = null;
            loadFormatter();
          })
          .error(function(data) {
            $rootScope.$broadcast('StatusUpdated', {
              title: $translate('formatterRemovalError'),
              error: data,
              timeout: 0,
              type: 'danger'});
          });
      };

      $scope.$watch('selectedFile', function() {
        if ($scope.selectedFile) {
          var params = {
            id: $scope.formatterSelected,
            fname: $scope.selectedFile['@path']
          };
          $http({
            url: 'md.formatter.edit@json',
            method: 'POST',
            data: $.param(params),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
          }).success(function(fileContent) {
            $scope.formatterFile = fileContent[0];
          });
        }
      });

      $scope.saveFormatterFile = function(formId) {
        $http({
          url: 'md.formatter.update@json',
          method: 'POST',
          data: $(formId).serialize(),
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).then(
          function(response) {
            if (response.status === 200) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('formatterFileUpdated',
                  {file: $scope.selectedFile['@name']}),
                timeout: 2,
                type: 'success'});
            } else {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('formatterFileUpdateError',
                  {file: $scope.selectedFile['@name']}),
                error: data,
                timeout: 0,
                type: 'danger'});
            }
          });
      };

      $scope.testFormatter = function(mode) {
        var service = 'md.formatter.' + (mode == 'HTML' ? 'html' : 'xml');
        var url = service + '?id=' + $scope.metadataId +
          '&xsl=' + $scope.formatterSelected;

        if (mode == 'DEBUG') {
          url += '&debug=true';
        }

        window.open(url, '_blank');
      };

      loadFormatter();
    }]);
}());
