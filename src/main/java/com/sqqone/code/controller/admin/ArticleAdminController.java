package com.sqqone.code.controller.admin;

import com.sqqone.code.entity.Article;
import com.sqqone.code.entity.Message;
import com.sqqone.code.lucene.ArticleIndex;
import com.sqqone.code.service.ArticleService;
import com.sqqone.code.service.CommentService;
import com.sqqone.code.service.MessageService;
import com.sqqone.code.util.StringUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/14 9:53
 */
@RestController
@RequestMapping("/admin/article")
public class ArticleAdminController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    private ArticleIndex articleIndex;

    @Autowired
    private CommentService commentService;

    @Autowired
    private MessageService messageService;

    @RequestMapping("/list")
    @RequiresPermissions("分页查询资源信息列表")
    public Map<String,Object> list(Article s_article, @RequestParam(value = "nickName",required = false) String nickName,
                                                      @RequestParam(value = "publishDates",required = false) String publishDates,
                                                      @RequestParam(value = "page",required = false) Integer page,
                                                      @RequestParam(value = "pageSize",required = false) Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        String s_bpublishDate = null;//开始时间
        String s_epublishDate = null;//结束时间
        if(StringUtil.isNotEmpty(publishDates)){
            String[]  dates = publishDates.split(" - ");
            s_bpublishDate = dates[0];
            s_epublishDate = dates[1];
        }
        map.put("data",articleService.list(s_article,nickName,s_bpublishDate,s_epublishDate,page,pageSize, Sort.Direction.DESC,"publishDate"));
        map.put("total",articleService.getCount(s_article,nickName,s_bpublishDate,s_epublishDate));
        map.put("errorNo",0);
        return map;
    }

    @RequestMapping("/delete")
    @RequiresPermissions("删除资源")
    public Map<String,Object> delete(@RequestParam(value = "articleId") String ids){
        Map<String, Object> map = new HashMap<>();
        String[]  idStr = ids.split(",");
        for (int i = 0; i < idStr.length; i++) {
            // 删除评论，删除lucene
            commentService.deleteByArticleId(Integer.parseInt(idStr[i]));
            articleService.delete(Integer.parseInt(idStr[i]));
            articleIndex.deleteIndex(idStr[i]);
        }
        map.put("errorNo",0);
        return map;
    }

    /*查看或审核页面数据*/
    @RequestMapping("/findById")
    @RequiresPermissions("修改帖子")
    public Map<String,Object> toEditArticle(Integer articleId){
        Article article = articleService.getById(articleId);
        Map<String, Object> map = new HashMap<>();
        Map<String,Object> tempMap = new HashMap<>();
        tempMap.put("articleId",article.getArticleId());
        tempMap.put("name",article.getName());
        tempMap.put("arcType",article.getArcType().getArcTypeId());
        tempMap.put("points",article.getPoints());
        tempMap.put("content",article.getContent());
        tempMap.put("download",article.getDownload());
        tempMap.put("password",article.getPassword());
        tempMap.put("click",article.getClick());
        tempMap.put("keywords",article.getKeywords());
        tempMap.put("description",article.getDescription());
        map.put("data",tempMap);
        map.put("errorNo",0);
        return map;

    }

    /**
     * 审核资源
     */
    @RequestMapping("/updateState")
    @RequiresPermissions(value = "审核资源")
    public Map<String,Object> updateState(Article article){
        Article oldArticle = articleService.getById(article.getArticleId());
        Message message = new Message();
        message.setUser(oldArticle.getUser());
        message.setPublishDate(new Date());
//        message.setArticleId(oldArticle.getArticleId());
        oldArticle.setCheckDate(new Date());
        if(article.getState()==2){
            message.setContent("【<font color='#00ff7f'>审核成功</font>】您发布的【"+oldArticle.getName()+"】资源审核成功！");
            oldArticle.setState(2);
        }else if(article.getState()==3){
            message.setContent("【<font color='#B22222'>审核失败</font>】您发布的【"+oldArticle.getName()+"】资源审核未成功！");
            message.setCause(article.getReason());
            oldArticle.setState(3);
            oldArticle.setReason(article.getReason());
        }
        messageService.save(message);
        articleService.save(oldArticle);
        if(oldArticle.getState()==2){
            oldArticle.setContentNoTag(StringUtil.stripHtml(oldArticle.getContent()));
            articleIndex.addIndex(oldArticle);          //添加索引
            redisTemplate.opsForList().leftPush("allarticleId",oldArticle.getArticleId());
            redisTemplate.opsForList().leftPush("article_type_"+oldArticle.getArcType().getArcTypeId(),oldArticle.getArticleId());
        }
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("errorNo",0);
        return resultMap;
    }


    @RequestMapping("/getAllIndex")
    @RequiresPermissions(value = "生成所有资源帖子索引")
    public boolean genAllIndex(){
        List<Article> articleList = articleService.listStatePass();
        if(articleList==null||articleList.size()==0){
            return false;
        }
        for(Article article:articleList){
            try {
                article.setContentNoTag(StringUtil.stripHtml(article.getContent()));
                articleIndex.addIndex(article);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    @RequestMapping("/updateHotState")
    @RequiresPermissions(value = "修改热门资源状态")
    public Map<String,Object>  updateHotState(Integer articleId,boolean isHot) {
        Map<String,Object> resultMap = new HashMap<>();
        Article oldArticle = articleService.getById(articleId);
        oldArticle.setHot(isHot);
        articleService.save(oldArticle);
        resultMap.put("success",true);
        resultMap.put("errorNo",0);
        return resultMap;
    }

    /**
     * 修改免费资源状态
     */
    @RequestMapping("/updateFreeState")
    @RequiresPermissions(value = "修改免费资源状态")
    public Map<String,Object> updateFreeState(Integer articleId,boolean isFree){
        Article oldArticle = articleService.getById(articleId);
        oldArticle.setFree(isFree);
        articleService.save(oldArticle);
        Map<String,Object> map = new HashMap<>();
        map.put("success",true);
        return map;
    }



}
