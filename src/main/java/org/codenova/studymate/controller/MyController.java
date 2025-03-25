package org.codenova.studymate.controller;


import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.LoginLog;
import org.codenova.studymate.model.entity.User;
import org.codenova.studymate.model.query.UserWithAvatar;
import org.codenova.studymate.repository.AvatarRepository;
import org.codenova.studymate.repository.LoginLogRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/my")
@AllArgsConstructor
public class MyController {
    private LoginLogRepository loginLogRepository;
    private AvatarRepository avatarRepository;
    @RequestMapping("/profile")
    public String profileHandle(Model model, @SessionAttribute("user") @Nullable UserWithAvatar user) {
        // User user =(User)session.getAttribute("user");
        if(user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("user",user);
        model.addAttribute("hiddenId", user.getId().substring(0, 2)+"******");
        LoginLog latestLog =loginLogRepository.findLatestByUserId(user.getId());
        model.addAttribute("latestLog",latestLog);


        return "my/profile";
    }
}
