package com.sqqone.code.service;

import com.sqqone.code.entity.UserDownload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/26 10:38
 */
public interface UserDownloadService {

    /*查询某用户下载次数*/
    public Long getCountByUserIdAndArticleId(Integer userId, Integer articleId);

    /*查询某资源下载次数*/
    public Long getCountByUserIdAndArticleId(Integer articleId);

    /*查询某用户下载的所有资源*/
    public Page<UserDownload> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction,String... properties);

    /*查询某用户下载资源数*/
    public Long getCount(Integer userId);

    /*添加或修改某用户的下载信息*/
    public void save(UserDownload userDownload);
}
