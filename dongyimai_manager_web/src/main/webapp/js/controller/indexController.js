app.controller('indexController',function ($scope,loginService) {
    //获取当前登录用户名
    $scope.showLoginName=function () {
         loginService.showLoginName().success(function (response) {
            $scope.loginName=response.userName;
         })
    }
})