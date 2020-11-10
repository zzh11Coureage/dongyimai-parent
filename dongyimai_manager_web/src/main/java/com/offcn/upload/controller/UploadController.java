package com.offcn.upload.controller;

import com.offcn.entity.Result;
import com.offcn.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    //文件服务器地址
    private String FILE_SERVER_URL;

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    //定义上传方法
    public Result upload(MultipartFile file)  {
        //获取文件的原始名称
        String filename = file.getOriginalFilename();
        //获取文件扩展名
        String extName = filename.substring(filename.lastIndexOf(".") + 1);

        //创建一个FastDFS客户端
        try {
        FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
        //执行上传
        String uploadFileId = fastDFSClient.uploadFile(file.getBytes(), extName);
        //实际的地址
       String url= FILE_SERVER_URL+uploadFileId;
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败"+e.getMessage());
        }

    }
}
