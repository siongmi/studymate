package org.codenova.studymate.controller;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/my")
@AllArgsConstructor
public class Mycontroller {

    @RequestMapping("/profile")
    public String myHandle(@ModelAttribute User user){
        return "my/view";
    }
}
