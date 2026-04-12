package com.yhj.dictation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器 - 返回Thymeleaf模板视图
 */
@Controller
public class PageController {

    /**
     * 主页面 - 听写功能
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 主页面 - 听写功能
     */
    @GetMapping("/index")
    public String home() {
        return "index";
    }

    /**
     * 历史记录页面
     */
    @GetMapping("/history")
    public String history() {
        return "history";
    }

    /**
     * 生词本页面
     */
    @GetMapping("/difficult-words")
    public String difficultWords() {
        return "difficult-words";
    }

    /**
     * 报表页面
     */
    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }

    /**
     * 任务管理页面
     */
    @GetMapping("/tasks")
    public String tasks() {
        return "tasks";
    }
}