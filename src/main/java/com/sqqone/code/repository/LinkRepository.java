package com.sqqone.code.repository;

import com.sqqone.code.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/28 10:57
 */
public interface LinkRepository extends JpaRepository<Link,Integer>, JpaSpecificationExecutor<Link> {
}
