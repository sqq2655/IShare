package com.sqqone.code.service.impl;

import com.sqqone.code.entity.UserDownload;
import com.sqqone.code.repository.UserDownloadRepository;
import com.sqqone.code.service.UserDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/26 10:43
 */
@Service
public class UserDownloadImpl implements UserDownloadService {

    @Autowired
    private UserDownloadRepository userDownloadReposity;

    @Override
    public Long getCountByUserIdAndArticleId(Integer userId, Integer articleId) {
        return userDownloadReposity.getCountByUserIdAndArticleId(userId,articleId);
    }

    @Override
    public Long getCountByUserIdAndArticleId(Integer articleId) {
        return userDownloadReposity.getCountByUserIdAndArticleId(articleId);
    }

    @Override
    public Page<UserDownload> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        return userDownloadReposity.findAll(new Specification<UserDownload>() {
            @Override
            public Predicate toPredicate(Root<UserDownload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        }, PageRequest.of(page-1,pageSize,direction,properties));
    }

    @Override
    public Long getCount(Integer userId) {
        return userDownloadReposity.count(new Specification<UserDownload>() {
            @Override
            public Predicate toPredicate(Root<UserDownload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        });
    }

    @Override
    public void save(UserDownload userDownload) {
        userDownloadReposity.save(userDownload);
    }
}
