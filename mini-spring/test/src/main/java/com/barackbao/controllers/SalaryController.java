package com.barackbao.controllers;

import com.barackbao.beans.AutoWired;
import com.barackbao.context.WebContext;
import com.barackbao.service.SalaryService;
import com.barackbao.web.mvc.Controller;
import com.barackbao.web.mvc.RequestMapping;
import com.barackbao.web.mvc.RequestParam;
import com.barackbao.web.view.View;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by barackbao on 2019-06-07
 */

@Controller
public class SalaryController {

    // 使用依赖注入
    @AutoWired
    private SalaryService service;


    @RequestMapping("/justdo/test")
    public View getSalary(@RequestParam("name") String name,
                          @RequestParam("experience") String experience) {

        HttpServletRequest request = WebContext.requestHolder.get();
        System.out.println(service.calSalary(Integer.parseInt(experience)));


        return new View("/index.jsp");
    }
}
