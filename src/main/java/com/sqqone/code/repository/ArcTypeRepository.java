package com.sqqone.code.repository;

import com.sqqone.code.entity.ArcType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/11 13:03
 */
public interface ArcTypeRepository  extends JpaRepository<ArcType,Integer>, JpaSpecificationExecutor<ArcType> {
}
