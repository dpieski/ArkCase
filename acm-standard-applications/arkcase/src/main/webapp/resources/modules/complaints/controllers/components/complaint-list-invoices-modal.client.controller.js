'use strict';

angular.module('complaints').controller('Complaints.ListInvoicesModalController', ['$scope', '$modalInstance', '$config', '$stateParams', 'params', 'Complaint.BillingService',
    function($scope, $modalInstance, $config, $stateParams, params, ComplaintBillingService){
        $scope.config = $config;
        $scope.gridDetailOptions = params.billingConfig;
        $scope.isGridShown = false;
        params.parentObjectId = $stateParams.id;
        params.parentObjectType = 'COMPLAINT';

        $scope.gridOptions = {
            enableColumnResizing: true,
            enableRowSelection: true,
            enableRowHeaderSelection: false,
            multiSelect: false,
            noUnselect: false,
            columnDefs: $scope.config.columnDefs,
            totalItems: 0,
            data: []
        };

        ComplaintBillingService.getBillingInvoices(params.parentObjectId, params.parentObjectType).then(function(data){
            $scope.items = data.data;
            $scope.gridOptions = $scope.gridOptions || {};
            $scope.gridOptions.data = $scope.items;
            $scope.gridOptions.totalItems = $scope.items.length;
        });

        $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
        };

        $scope.toggle = function(){
            $scope.isGridShown = !$scope.isGridShown
        };

        $scope.viewInvoice = function(rowEntity){
            $scope.isGridShown = true;
            $scope.invoiceNumber = rowEntity.invoiceNumber;
            $scope.items = rowEntity.billingItems;
            $scope.gridDetailOptions = $scope.gridDetailOptions || {};
            $scope.gridDetailOptions.data = $scope.items;
        };

    }]);