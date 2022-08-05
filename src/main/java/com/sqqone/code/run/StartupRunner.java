package com.sqqone.code.run;

import com.sqqone.code.entity.Article;
import com.sqqone.code.service.ArcTypeService;
import com.sqqone.code.service.ArticleService;
import com.sqqone.code.service.LinkService;
import com.sqqone.code.util.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;


import javax.servlet.ServletContext;
import java.util.List;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/13 11:24
 */

@Component
public class StartupRunner implements CommandLineRunner {
    @Autowired
    private ServletContext application;

    @Autowired
    private ArcTypeService arcTypeService;

    @Autowired
    private LinkService linkService;


    @Autowired
    @Lazy
    private ArticleService articleService;


    @Override
    public void run(String... args) throws Exception {
        loadData();
    }


    /*加载有所资源*/
    public void loadData(){
        application.setAttribute(Consts.ARC_TYPE_LIST,arcTypeService.listAll(Sort.Direction.ASC,"sort"));   //所有资源分类
        application.setAttribute(Consts.NEW_ARTICLE,articleService.getNewArticle(10));                  //10条最新资源
        application.setAttribute(Consts.CLICK_ARTICLE,articleService.getClickArticle(10));              //10条热门资源
//        application.setAttribute(Consts.RANDOM_ARTICLE,articleService.getRandomArticle(10));            //10条随机资源（热搜推荐）
        application.setAttribute(Consts.DOWNLOAD_MOST_ARTICLE,articleService.getMostDownLoadArticle(10));            //10条下载最多资源（热搜推荐）
        application.setAttribute(Consts.LINK_LIST,linkService.listAll(Sort.Direction.ASC,"sort"));          //所有友情链接
    }
}
