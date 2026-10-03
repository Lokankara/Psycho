package com.unconscious.collective.quiz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/", "/quiz", "/result", "/history", "/assessment"})
    public String index() {
        return "forward:/index.html";
    }
}
