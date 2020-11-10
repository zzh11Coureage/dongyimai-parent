package com.offcn.content.controller;

import com.offcn.content.service.ContentService;
import com.offcn.pojo.TbContent;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class contentController {
    @Reference
    private ContentService contentService;
@RequestMapping("/findByCategoryId")
    public List<TbContent> findByCategoryId(Long categoryId){
        return  contentService.findByCategoryId(categoryId);
    }
}
