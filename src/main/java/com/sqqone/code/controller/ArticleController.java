package com.sqqone.code.controller;

import com.sqqone.code.entity.Article;
import com.sqqone.code.lucene.ArticleIndex;
import com.sqqone.code.run.StartupRunner;
import com.sqqone.code.service.ArcTypeService;
import com.sqqone.code.service.ArticleService;
import com.sqqone.code.util.Consts;
import com.sqqone.code.util.HTMLUtil;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/25 11:12
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArcTypeService arcTypeService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleIndex articleIndex;

    @Autowired
    private StartupRunner startupRunner;

    @RequestMapping("/{type}/{currentPage}")
    public ModelAndView index(@PathVariable(value = "type",required = false) String type, @PathVariable(value = "currentPage",required = false)Integer currentPage){
        ModelAndView mov = new ModelAndView();
        List arcTypeList = arcTypeService.listAll(Sort.Direction.ASC,"sort");
        mov.setViewName("index");
        /*类型html代码*/
        mov.addObject("arcTypeStr", HTMLUtil.getArcTypeStr(type, arcTypeList));
        /*资源列表*/
        Map<String, Object> map = articleService.list(type, currentPage, Consts.PAGE_SIZE);
        mov.addObject("articleList",map.get("data"));
        /*分页html代码*/
        mov.addObject("pageStr",HTMLUtil.getPagation("/article/"+type,Integer.parseInt(String.valueOf(map.get("count"))),currentPage,"该分类还没有数据"));
        return mov;
    }

    @RequestMapping("/search")
    public ModelAndView search(String keywords,@RequestParam(value = "page",required = false) Integer page) throws ParseException, InvalidTokenOffsetsException, org.apache.lucene.queryparser.classic.ParseException, IOException {
        if(page==null){
            page = 1;
        }
        ModelAndView mov = new ModelAndView();
        mov.setViewName("index");
        List arcTypeList = arcTypeService.listAll(Sort.Direction.ASC,"sort");
        /*类型html代码*/
        mov.addObject("arcTypeStr",HTMLUtil.getArcTypeStr("all", arcTypeList));
        /*资源列表*/
        List<Article> articleList = articleIndex.search(keywords);
        Integer toIndex = articleList.size()>=page*Consts.PAGE_SIZE?page*Consts.PAGE_SIZE:articleList.size();
        mov.addObject("articleList",articleList.subList((page-1)*Consts.PAGE_SIZE,toIndex));
        mov.addObject("keywords",keywords);
        /*分页html代码*/

        int totalPage = articleList.size()%Consts.PAGE_SIZE==0?articleList.size()/Consts.PAGE_SIZE:articleList.size()/Consts.PAGE_SIZE+1;
        String targetUrl = "/article/search?keywords="+keywords;
        String msg = "没有关键字是 \"<font style=\"border: 0px;color:red;font-weight:bold;padding-left: 3px; padding-right: 3px;\" >" +keywords + "</font>\" 的相关资源，请联系站长！";
        mov.addObject("pageStr",HTMLUtil.getPagation2(targetUrl,totalPage,page,msg));

        return mov;
    }

    @RequestMapping("/detail/{articleId}")
    public ModelAndView detail(@PathVariable(value = "articleId",required = false) String articleId) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        ModelAndView mav = new ModelAndView();
        String replace = articleId.replace(".html","");
        articleService.updateClick(Integer.parseInt(replace));
        Article article = articleService.getById(Integer.parseInt(replace));
        if(article.getState()!=2){
            return null;
        }
        mav.addObject("article",article);
        //类型的html代码
        List arcTypeList = arcTypeService.listAll(Sort.Direction.ASC,"sort");
        mav.addObject("arcTypeStr", HTMLUtil.getArcTypeStr(article.getArcType().getArcTypeId().toString(),arcTypeList));

        //通过lucene分词查找相似资源
        List<Article> articleList = articleIndex.searchNoHighLighter(article.getName().replace("视频","").replace("教程","")
                .replace("下载","").replace("PDF",""));
        if(articleList!=null&&articleList.size()>0){
            mav.addObject("similarityArticleList",articleList);
        }
        startupRunner.loadData();
        mav.setViewName("detail");
        return mav;
    }

    /*判断某资源是否免费*/
    @RequestMapping("/isFree")
    @ResponseBody
    public boolean isFree(Integer articleId){
        Article article = articleService.getById(articleId);
        return article.isFree();
    }

}
