package com.sweet.hzy.service.imp;

import com.sweet.bean.WeChat;
import com.sweet.hzy.mapper.WeChatMapper;
import com.sweet.hzy.service.WeChatService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WeChatServiceImp implements WeChatService {
    @Resource
    private WeChatMapper weChatMapper;
    @Override
    public Integer putUserMessage(WeChat weChat) {
       return weChatMapper.putUserMessage(weChat);
    }
}