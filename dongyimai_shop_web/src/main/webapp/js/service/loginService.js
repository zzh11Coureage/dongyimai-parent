app.service('loginService',function ($http) {
    //定义获取当前登录用户名方法
    this.showLoginName=function () {
      return  $http.get('/login/showLoginName.do');
    }
})