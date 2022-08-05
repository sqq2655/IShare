package com.sqqone.code.repository;

import com.sqqone.code.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/30 9:43
 */
public interface MessageRepository extends JpaRepository<Message,Integer>, JpaSpecificationExecutor<Message> {

    /*查询某用户下载总数*/
    @Query(value = "select count(*) from message where is_see = false and user_id = ?1",nativeQuery = true)
    public Integer getCountByUserId(Integer userId);

    @Query(value = "update message set is_see = 1 where user_id = ?1",nativeQuery = true)
    @Modifying
    public void updateState(Integer userId);
}
