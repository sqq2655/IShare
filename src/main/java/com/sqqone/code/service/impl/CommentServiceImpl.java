package com.sqqone.code.service.impl;

import com.sqqone.code.entity.Comment;
import com.sqqone.code.repository.CommentRepository;
import com.sqqone.code.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


/**
 * 评论Service实现
 */
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public void save(Comment comment) {
        commentRepository.save(comment);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    public Page<Comment> list(Comment s_comment, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        return commentRepository.findAll(new Specification<Comment>() {
            @Override
            public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(s_comment!=null){
                    if(s_comment.getState()!=null) {              //审核状态
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("state"),s_comment.getState()));
                    }
                    if(s_comment.getArticle()!=null&&s_comment.getArticle().getArticleId()!=null){          //所属资源
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("article").get("articleId"),s_comment.getArticle().getArticleId()));
                    }
                }
                return predicate;
            }
        }, PageRequest.of(page-1,pageSize,direction,properties));
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    public Long getTotal(Comment s_comment) {
        return commentRepository.count(new Specification<Comment>() {
            @Override
            public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(s_comment!=null){
                    if(s_comment.getState()!=null) {              //审核状态
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("state"),s_comment.getState()));
                    }
                    if(s_comment.getArticle()!=null&&s_comment.getArticle().getArticleId()!=null){          //所属资源
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("article").get("articleId"),s_comment.getArticle().getArticleId()));
                    }
                }
                return predicate;
            }
        });
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    public Comment get(Integer id) {
        return commentRepository.getOne(id);
    }

    @Override
    public void delete(Integer id) {
        commentRepository.deleteById(id);
    }

    @Override
    public void deleteByArticleId(Integer articleId) {
        commentRepository.deleteByArticleId(articleId);
    }
}
