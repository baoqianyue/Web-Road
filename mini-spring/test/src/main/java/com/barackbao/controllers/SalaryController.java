package com.barackbao.controllers;

import com.barackbao.beans.AutoWired;
import com.barackbao.service.SalaryService;
import com.barackbao.web.mvc.Controller;
import com.barackbao.web.mvc.RequestMapping;
import com.barackbao.web.mvc.RequestParam;

/**
 * Created by barackbao on 2019-06-07
 */

@Controller
public class SalaryController {

    // 使用依赖注入
    @AutoWired
    private SalaryService service;


    @RequestMapping("/get_salary.json")
    public Integer getSalary(@RequestParam("name") String name,
                             @RequestParam("experience") String experience) {
        return service.calSalary(Integer.parseInt(experience));
    }
}
