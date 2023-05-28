package com.sqqone.code.controller;

import com.sqqone.code.service.ArcTypeService;
import com.sqqone.code.service.ArticleService;
import com.sqqone.code.util.Consts;
import com.sqqone.code.util.HTMLUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private ArcTypeService arcTypeService;

    @Autowired
    private ArticleService articleService;

    @RequestMapping("/")
    public ModelAndView index(){
        ModelAndView mov = new ModelAndView();
        List arcTypeList = arcTypeService.listAll(Sort.Direction.ASC,"sort");
        mov.setViewName("index");
        /*类型html代码*/
        mov.addObject("arcTypeStr",HTMLUtil.getArcTypeStr("all", arcTypeList));
        /*资源列表*/
        Map<String, Object> map = articleService.list("all", 1, Consts.PAGE_SIZE);
        mov.addObject("articleList",map.get("data"));
        /*分页html代码*/
        mov.addObject("pageStr",HTMLUtil.getPagation("/article/all",Integer.parseInt(String.valueOf(map.get("count"))),1,"该分类还没有数据"));
        return mov;
    }


    /**
     * 购买VIP
     */
    @RequestMapping("/buyVIP")
    public String buyVIP(){
        return "buyVIP";
    }

    /**
     * 发布资源赚积分
     */
    @RequestMapping("/fbzyzjf")
    public String fbzyzjf(){
        return "fbzyzjf";
    }
}
