package com.sqqone.code.service;

import com.sqqone.code.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/30 9:48
 */
public interface MessageService {


    /*分页查看消息列表*/
    Page<Message> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction,String... properties);


    /*获取消息总数*/
    Long getCount(Integer userId);

    public void save(Message message);

    /*查询某用户下载总数*/
    public Integer getCountByUserId(Integer userId);

    /*查看消息*/
    public void updateState(Integer userId);
}
