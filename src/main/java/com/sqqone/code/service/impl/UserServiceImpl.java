package com.sqqone.code.service.impl;

import com.sqqone.code.entity.User;
import com.sqqone.code.repository.UserRepository;
import com.sqqone.code.service.ArticleService;
import com.sqqone.code.service.UserService;
import com.sqqone.code.util.Consts;
import com.sqqone.code.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/10 9:07
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void save(User user) {

        if(user.getUserId()!=null){
            redisTemplate.delete("allarticleId");
            Set<Object> keys = redisTemplate.keys("article_" + "*");
            if(keys.size() > 0){
                redisTemplate.delete(keys);
            }
        }
        userRepository.save(user);

    }

    @Override
    public User getUserById(Integer userId) {
        return userRepository.getOne(userId);
    }

    @Override
    public Long getCount(User s_user, String s_blatelyLoginTime, String s_elatelyLoginTime) {
        return userRepository.count(new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(StringUtil.isNotEmpty(s_blatelyLoginTime)){
                    predicate.getExpressions().add(criteriaBuilder.greaterThanOrEqualTo(root.get("latelyLoginTime").as(String.class),s_blatelyLoginTime));
                }
                if(StringUtil.isNotEmpty(s_elatelyLoginTime)){
                    predicate.getExpressions().add(criteriaBuilder.lessThanOrEqualTo(root.get("latelyLoginTime").as(String.class),s_elatelyLoginTime));
                }
                if(s_user!=null){
                    if(StringUtil.isNotEmpty(s_user.getSex())){
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("sex"),s_user.getSex()));
                    }
                    if(StringUtil.isNotEmpty(s_user.getUserName())){
                        predicate.getExpressions().add(criteriaBuilder.like(root.get("userName"),"%"+s_user.getUserName()+"%"));
                    }
                }
                return predicate;
            }
        });
    }

    @Override
    public Integer todayRegister() {
        return userRepository.todayRegister();
    }

    @Override
    public Integer todayLogin() {
        return userRepository.todayLogin();
    }

    @Override
    public List<User> list(User s_user, String s_blatelyLoginTime, String s_elatelyLoginTime, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        Page<User> pageUser = userRepository.findAll(new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(StringUtil.isNotEmpty(s_blatelyLoginTime)){
                    predicate.getExpressions().add(criteriaBuilder.greaterThanOrEqualTo(root.get("latelyLoginTime").as(String.class),s_blatelyLoginTime));
                }
                if(StringUtil.isNotEmpty(s_elatelyLoginTime)){
                    predicate.getExpressions().add(criteriaBuilder.lessThanOrEqualTo(root.get("latelyLoginTime").as(String.class),s_elatelyLoginTime));
                }
                if(s_user!=null){
                    if(StringUtil.isNotEmpty(s_user.getSex())){
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("sex"),s_user.getSex()));
                    }
                    if(StringUtil.isNotEmpty(s_user.getUserName())){
                        predicate.getExpressions().add(criteriaBuilder.like(root.get("userName"),"%"+s_user.getUserName()+"%"));
                    }
                }
                return predicate;
            }
        }, PageRequest.of(page-1,pageSize,direction,properties));
        return pageUser.getContent();
    }

    @Override
    public User findByOpenId(String openId){
        return userRepository.findByOpenId(openId);
    }

    @Override
    public void removeBind(Integer userId) {
        userRepository.removeBind(userId);
    }
}
