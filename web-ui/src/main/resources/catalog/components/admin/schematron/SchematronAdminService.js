(function () {
    "use strict";
    goog.provide('gn_schematronadminservice');

    var module = angular.module('gn_schematronadminservice', []);


    /**
     * Display harvester identification section with
     * name, group and icon
     */
    module.service('gnSchematronAdminService', ['$http', '$cacheFactory',
        function ($http, $cacheFactory) {
            var TIMEOUT = 60000; // refresh cache every minute
            var cache = $cacheFactory('gnSchematronAdminService');
            var getDataFromCache = function(id, timeout) {
                if (!timeout) {
                    timeout = TIMEOUT;
                }
                var lastUsed = cache.get('lastUsed_'+id);
                var now = new Date();
                if (!lastUsed || (now - lastUsed) > timeout) {
                    return;
                }
                return cache.get(id);
            };
            var putDataIntoCache = function (id, data) {
                var now = new Date().getTime();
                cache.put("lastUsed_"+id, now);
                cache.put(id, data);
            };
            var removeElementFromCache = function (id) {
                cache.remove(id);
                cache.remove("lastUsed_"+id);
            }
            var groupCacheId = function(schematronId) { return "criteriaGroup_"+schematronId};

            var updateCacheOnGroupChange = function (schematronId) {
                removeElementFromCache('criteriaTypes');
                removeElementFromCache(groupCacheId(schematronId));
            };
            var updateCacheOnCriteriaChange = function (schematronId, groupName) {
                removeElementFromCache(groupCacheId(schematronId));
            };
            var findIndex = function(array, object, comparator) {
                for (var i = 0; i < array.length; i++) {
                    var arrayObj = array[i];
                    if (comparator(arrayObj, object)) {
                        return i;
                    }
                }
                return -1;
            };
            var criteriaComparator = function (o1, o2) {return o1.id === o2.id};
            var groupComparator = function (o1, o2) {return o1.id.schematronid === o2.id.schematronid && o1.id.name === o2.id.name};

            this.criteria = {
                remove: function(criteria, group) {
                    $http({
                        method: 'GET',
                        url: 'admin.schematroncriteria.delete@json',
                        params: {
                            id: criteria.id
                        }
                    }).success(function () {
                        updateCacheOnCriteriaChange(group.id.schematronid, group.id.name);
                        var list = group.criteria;
                        var idx = findIndex(list, criteria, criteriaComparator);
                        if (idx != -1) {
                            list.splice(idx,1);
                        }
                    }).error(function(){
                        alert("Error deleting criteria: "+criteria.id);
                    });
                },
                update: function(updated, original, group) {
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
                        updateCacheOnCriteriaChange(group.id.schematronid, group.id.name);

                        angular.copy(updated, original);
                    }).error(function(){
                        alert("Error updating criteria: "+criteria.id);
                    });
                },
                add: function(criteria, original, group) {
                    $http({
                        method: 'POST',
                        url: 'admin.schematroncriteria.add@json',
                        params: {
                            type: criteria.type,
                            value: criteria.value,
                            uitype: criteria.uitype,
                            uivalue: criteria.uivalue,
                            groupName: group.id.name,
                            schematronId: group.id.schematronid
                        }
                    }).success(function(response) {
                        updateCacheOnCriteriaChange(group.id.schematronid, group.id.name);
                        var added = angular.copy(criteria);
                        added.id = response.id;
                        if (!group.criteria) {
                            group.criteria = [];
                        }
                        group.criteria.push(added);
                        angular.copy(original, criteria);
                    }).error(function(){
                        alert("Error adding criteria: {\n\ttype:" + criteria.type + ",\n\tvalue:" + criteria.value +
                            ",\n\tgroupName:" + group.id.name + "schematronId: " + group.id.schematronid);
                    });
                }
            };
            this.group = {
                remove: function(group, groupList, successCallback) {
                    $http({
                        method: 'GET',
                        url: 'admin.schematroncriteriagroup.delete@json',
                        params: {
                            groupName: group.id.name,
                            schematronId: group.id.schematronid
                        }
                    }).success(function () {
                        updateCacheOnGroupChange(group.id.schematronid);
                        var idx = findIndex(groupList, group, groupComparator);
                        if (idx != -1) {
                            groupList.splice(idx,1);
                        }
                        successCallback();
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
                    if (updated.id.name !== original.id.name) {
                        params.newGroupName = updated.id.name;
                    }
                    if (updated.id.schematronid !== original.id.schematronid) {
                        params.newSchematronid = updated.id.schematronid;
                    }
                    $http({
                        method: 'GET',
                        url: 'admin.schematroncriteriagroup.update@json',
                        params: params
                    }).success(function () {
                        original.id.name = updated.id.name;
                        original.id.schematronId = updated.id.schematronid;
                        original.requirement = updated.requirement;
                    }).error(function(){
                        alert("Error editing Schematron Criteria Group: "+original.id);
                    });
                },
                add: function(group, groupList, successCallback) {
                    $http({
                        method: 'GET',
                        url: 'admin.schematroncriteriagroup.add@json',
                        params: {
                            groupName: group.id.name,
                            schematronId: group.id.schematronid,
                            requirement: group.requirement
                        }
                    }).success(function () {
                        updateCacheOnGroupChange(group.id.schematronid);
                        groupList.push(group);
                        successCallback(group);
                    }).error(function(){
                        alert("Error adding new Schematron Criteria Group: "+group.id);
                    });
                },
                list: function(schematronId, successFunction) {
                    var data = getDataFromCache(groupCacheId(schematronId));
                    if (data) {
                        successFunction(data);
                    } else {
                        $http({
                            method: 'GET',
                            url: 'admin.schematroncriteriagroup.list@json',
                            params: {
                                includeCriteria: true,
                                schematronId: schematronId
                            }
                        }).success(function (data){
                            if (data === 'null') {
                                data = [];
                            }
                            putDataIntoCache(groupCacheId(schematronId), data);
                            successFunction(data);
                        }).error(function(data, code){
                            alert("Error occured during loading schematron criteria groups for schematron: " + schematronId);
                        });
                    }
                }
            };

            this.criteriaTypes = {
                list: function (successCallback) {
                    var cachedCriteriaTypes = getDataFromCache('criteriaTypes');
                    if (cachedCriteriaTypes) {
                        successCallback(cachedCriteriaTypes);
                    } else {
                        $http.get('admin.schematrontype@json').
                            success(function (data) {
                                putDataIntoCache('criteriaTypes', data);
                                successCallback(data);
                            }).error(function (data) {
                                alert("An Error occurred with the admin.schematrontype@json request:" + data)
                            });
                    }
                }
            }
        }]);
})();
