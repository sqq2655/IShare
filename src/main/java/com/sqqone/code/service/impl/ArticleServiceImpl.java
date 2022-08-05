package com.sqqone.code.service.impl;

import com.sqqone.code.entity.ArcType;
import com.sqqone.code.entity.Article;
import com.sqqone.code.entity.UserDownload;
import com.sqqone.code.lucene.ArticleIndex;
import com.sqqone.code.repository.ArticleRepository;
import com.sqqone.code.run.StartupRunner;
import com.sqqone.code.service.ArcTypeService;
import com.sqqone.code.service.ArticleService;
import com.sqqone.code.service.UserDownloadService;
import com.sqqone.code.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/11 13:42
 */
@Service
@Transactional(propagation = Propagation.REQUIRED,readOnly = false)
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArcTypeService arcTypeService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    @Lazy
    private StartupRunner startupRunner;

    @Autowired
    private ArticleIndex articleIndex;

    @Autowired
    private UserDownloadService userDownloadService;

    private RedisSerializer redisSerializer = new StringRedisSerializer();


    @Override
    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    public List<Article> list(Article s_article, String nickname, String s_bpublishDate, String s_epublishDate, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        Page<Article> articlePage = articleRepository.findAll(new Specification<Article>() {
            @Override
            public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return getPredicate(root, criteriaBuilder, s_bpublishDate, s_epublishDate, nickname, s_article);
            }
        }, PageRequest.of(page-1,pageSize,direction,properties));
        return articlePage.getContent();
    }

    /*分页条件*/
    private Predicate getPredicate(Root<Article> root, CriteriaBuilder criteriaBuilder, String s_bpublishDate, String s_epublishDate, String nickname, Article s_article) {
        Predicate predicate = criteriaBuilder.conjunction();
        if(StringUtil.isNotEmpty(s_bpublishDate)){
            predicate.getExpressions().add(criteriaBuilder.greaterThanOrEqualTo(root.get("publishDate").as(String.class),s_bpublishDate));
        }
        if(StringUtil.isNotEmpty(s_epublishDate)){
            predicate.getExpressions().add(criteriaBuilder.lessThanOrEqualTo(root.get("publishDate").as(String.class),s_epublishDate));
        }
        if(StringUtil.isNotEmpty(nickname)){
            predicate.getExpressions().add(criteriaBuilder.like(root.get("user").get("nickName"),"%"+nickname+"%"));
        }
        if(s_article!=null){
            if(StringUtil.isNotEmpty(s_article.getName())){
                predicate.getExpressions().add(criteriaBuilder.like(root.get("name"),"%"+s_article.getName()+"%"));
            }
            if(s_article.isHot()){
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("isHot"),1));
            }
            if (s_article.getArcType() != null && s_article.getArcType().getArcTypeId() != null) {      //类型
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("arcType").get("arcTypeId"), s_article.getArcType().getArcTypeId()));
            }
            if (s_article.getUser() != null && s_article.getUser().getUserId() != null) {               //用户
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"), s_article.getUser().getUserId()));
            }
            if (s_article.getState() != null) {                                         //审核状态
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("state"), s_article.getState()));
            }
            if (!s_article.isUseful()) {                                         //资源链接是否有效
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("isUseful"), false));
            }
        }
        return predicate;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    public Long getCount(Article s_article, String nickname, String s_bpublishDate, String s_epublishDate) {
        Long count = articleRepository.count(new Specification<Article>() {
            @Override
            public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return getPredicate(root,criteriaBuilder,s_bpublishDate,s_epublishDate,nickname,s_article);
            }
        });
        return count;
    }

    @Override
    public void save(Article article) {
        if(article.getState()==2){
            redisTemplate.setKeySerializer(redisSerializer);
            redisTemplate.opsForValue().set("article_"+article.getArticleId(),article);//把审核通过的资源放到redis
            startupRunner.loadData();//刷新缓存
            articleIndex.updateIndex(article);

        }
        articleRepository.save(article);
    }

    @Override
    public void delete(Integer id) {
        redisTemplate.opsForList().remove("allarticleId",0,id);
        int arcTypeId = articleRepository.getArcTypeIdByArticleId(id);
        redisTemplate.opsForList().remove("article_type_"+arcTypeId,0,id);
        redisTemplate.delete("article_"+id);
        articleRepository.deleteById(id);
        startupRunner.loadData();                           //刷新缓存
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    public Article getById(Integer id) {
        if(redisTemplate.hasKey("article_"+id)){
            return (Article) redisTemplate.opsForValue().get("article_"+id);
        }else{
            Article article = articleRepository.getOne(id);
            if(article.getState().intValue()==2){
                redisTemplate.setKeySerializer(redisSerializer);
                redisTemplate.opsForValue().set("article_"+article.getArticleId(),article);//把审核通过的资源放到redis
            startupRunner.loadData();                           //刷新缓存
            }
            return article;
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    public Map<String, Object> list(String type, Integer page, Integer pageSize) {
        /*初始化模板，*/
        redisTemplate.setKeySerializer(redisSerializer);
        ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
        ListOperations<Object, Object> opsForList = redisTemplate.opsForList();

        Map<String, Object> map = new HashMap<>();
        List<Article> tempList = new ArrayList<>();
        /*判断redis有无资源列表*/
        Boolean flag = redisTemplate.hasKey("allarticleId");
        /*如果redis没有资源去数据库查询*/
        if(!flag){
            List<Article> listStatePass = listStatePass();
            for (Article article : listStatePass) {
                /*将每一个资源放入redis*/
                opsForValue.set("article_"+article.getArticleId(),article);
                /*将每个资源放入redis的资源列表*/
                opsForList.rightPush("allarticleId",article.getArticleId()) ;
                List<ArcType> arcTypeList = arcTypeService.listAll(Sort.Direction.ASC,"sort");
                for (ArcType arcType : arcTypeList) {
                    if(article.getArcType().getArcTypeId().intValue() == arcType.getArcTypeId().intValue()){
                        opsForList.rightPush("article_type_"+arcType.getArcTypeId(),article.getArticleId());
                    }
                }
            }

        }

        //4、分页资源列表并且返回当前页
        long start = (page-1)*pageSize;
        long end = pageSize*page - 1;
        List idList;
        long count;
        if("all".equals(type)){
            idList = opsForList.range("allarticleId",start,end);
            count = opsForList.size("allarticleId");

        }else{
            idList = opsForList.range("article_type_"+type,start,end);
            count = opsForList.size("article_type_"+type);

        }
        for(Object id:idList){
            tempList.add((Article) opsForValue.get("article_"+id));
        }
        map.put("data",tempList);
        map.put("count",count);

        return map;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    public List<Article> listStatePass() {
        return articleRepository.findAll(new Specification<Article>() {
            @Override
            public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("state"),2));
                return predicate;
            }
        },Sort.by(Sort.Direction.DESC,"publishDate"));

    }

    @Override
    public void updateClick(Integer articleId) {
        articleRepository.updateClick(articleId);
        Article article = articleRepository.getOne(articleId);
        articleIndex.updateIndex(article);
        if(article.getState()==2) {          //把审核通过的资源放到redis
            redisTemplate.setKeySerializer(redisSerializer);
            redisTemplate.opsForValue().set("article_" + article.getArticleId(), article);
        }
    }

    @Override
    public Integer todayPublish() {
        return articleRepository.todayPublish();
    }

    @Override
    public Integer noAudit() {
        return articleRepository.noAudit();
    }

    @Override
    public List<Article> getNewArticle(Integer n) {
        return articleRepository.getNewArticle(n);
    }

    @Override
    public List<Article> getClickArticle(Integer n) {
        return articleRepository.getClickArticle(n);
    }

    @Override
    public List<Article> getRandomArticle(Integer n) {
        return articleRepository.getRandomArticle(n);
    }

    @Override
    public List<Article> getMostDownLoadArticle(Integer n) {
        List<Article> mostDownLoadArticle = articleRepository.getMostDownLoadArticle(n);
        for (Article article : mostDownLoadArticle) {
            article.setDownloadCount(userDownloadService.getCountByUserIdAndArticleId(article.getArticleId()));
        }
        return mostDownLoadArticle;
    }

}
