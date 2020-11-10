app.service('uploadService',function ($http) {

    //定义上传文件方法
    this.upload=function () {
        //创建一个表单数据封装对象
        var formData=  new FormData();
        //把要上传文件封装到表单对象
        formData.append("file",file.files[0]);

        //发出上传请求
        return  $http({
            //配置请求方法
            method: 'POST',
            url: '/upload.do', //请求地址
            data: formData, //传递数据
            headers:{'Content-Type':undefined}, //设置请求头为 未定义，避免发送 application/json请求
            transformRequest: angular.identity //传输文件序列化方式
        });
    }
})