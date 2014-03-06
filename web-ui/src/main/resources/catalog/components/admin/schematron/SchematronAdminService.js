(function () {
    "use strict";
    goog.provide('gn_schematronadminservice');

    var module = angular.module('gn_schematronadminservice', []);


    /**
     * Display harvester identification section with
     * name, group and icon
     */
    module.service('gnSchematronAdminService', ['$http',
        function ($http) {
            this.criteria = {
                remove: function(criteria, group) {
                    $http({
                        method: 'GET',
                        url: 'admin.schematroncriteria.delete@json',
                        params: {
                            id: criteria.id
                        }
                    }).success(function () {
                        var list = group.criteria;
                        var idx = list.indexOf(criteria);
                        if (idx != -1) {
                            list.splice(idx,1);
                        }
                    }).error(function(){
                        alert("Error deleting criteria: "+criteria.id);
                    });
                },
                update: function(updated, original) {
                    $http({
                        method: 'POST',
                        url: 'admin.schematroncriteria.update@json',
                        params: {
                            id: original.id,
                            type: updated.type,
                            value: updated.value,
                            uitype: updated.uitype,
                            uivalue: updated.uivalue
                        }
                    }).success(function() {
                        original.type = updated.type;
                        original.value = updated.value;
                        original.uitype = updated.uitype;
                        original.uivalue = updated.uivalue;
                    }).error(function(){
                        alert("Error updating criteria: "+criteria.id);
                    });
                },
                add: function(criteria, group) {
                    $http({
                        method: 'POST',
                        url: 'admin.schematroncriteria.add@json',
                        params: {
                            type: criteria.type,
                            value: criteria.value,
                            uitype: criteria.uitype,
                            uivalue: criteria.uivalue,
                            groupName: criteria.groupName,
                            schematronId: criteria.schematronId
                        }
                    }).success(function(response) {
                        criteria.id = response.id;
                        group.criteria.push(criteria);
                    }).error(function(){
                        alert("Error adding criteria: {\n\ttype:" + criteria.type + ",\n\tvalue:" + criteria.value +
                            ",\n\tgroupName:" + criteria.groupName + "schematronId: " + criteria.schematronId);
                    });
                }
            };
            this.group = {
                remove: function(group, groupList) {
                    $http({
                        method: 'GET',
                        url: 'admin.schematroncriteriagroup.delete@json',
                        params: {
                            groupName: group.id.name,
                            schematronId: group.id.schematronid
                        }
                    }).success(function () {
                        var idx = groupList.indexOf(group);
                        if (idx != -1) {
                            groupList.splice(idx,1);
                        }
                    }).error(function(){
                        alert("Error deleting Schematron Criteria Group: "+group.id);
                    });
                },
                update: function(updated, original) {
                    var params = {
                        groupName: original.id.name,
                        schematronId: original.id.schematronid,
                        requirement: updated.requirement
                    }
                    if (updated.id.name === original.id.name) {
                        params.newGroupName = updated.id.name;
                    }
                    if (updated.id.schematronId === original.id.schematronId) {
                        params.newSchematronId = updated.id.schematronId;
                    }
                    $http({
                        method: 'GET',
                        url: 'admin.schematroncriteriagroup.update@json',
                        params: params
                    }).success(function () {
                        original.id.name = updated.id.name;
                        original.id.schematronId = updated.id.schematronId;
                        original.requirement = updated.requirement;
                    }).error(function(){
                        alert("Error editing Schematron Criteria Group: "+original.id);
                    });
                },
                add: function(group, groupList) {
                    $http({
                        method: 'GET',
                        url: 'admin.schematroncriteriagroup.add@json',
                        params: {
                            groupName: group.id.name,
                            schematronId: group.id.schematronid,
                            requirement: group.requirement
                        }
                    }).success(function () {
                        groupList.push(group);
                    }).error(function(){
                        alert("Error adding new Schematron Criteria Group: "+group.id);
                    });
                },
                list: function(schematronId, successFunction) {
                    $http({
                        method: 'GET',
                        url: 'admin.schematroncriteriagroup.list@json',
                        params: {
                            includeCriteria: true,
                            schematronId: schematronId
                        }
                    }).success(successFunction).error(function(data, code){
                        alert("Error occured during loading schematron criteria groups for schematron: " + schematronId);
                    });

                }
            };
        }]);
})();
