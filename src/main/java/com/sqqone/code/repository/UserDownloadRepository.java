package com.sqqone.code.repository;

import com.sqqone.code.entity.UserDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/26 10:36
 */
public interface UserDownloadRepository extends JpaRepository<UserDownload,Integer>, JpaSpecificationExecutor<UserDownload> {

    /**
     * 查询某个用户下载某个资源的次数
     */
    @Query(value = "select count(*) from user_download where user_id=?1 and article_id=?2",nativeQuery = true)
    public Long getCountByUserIdAndArticleId(Integer userId,Integer articleId);

    @Query(value = "select count(*) from user_download where article_id=?1",nativeQuery = true)
    public Long getCountByUserIdAndArticleId(Integer articleId);
}
