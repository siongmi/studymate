package org.codenova.studymate.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.Avatar;
import org.codenova.studymate.model.entity.StudyMember;
import org.codenova.studymate.model.entity.User;
import org.codenova.studymate.repository.AvatarRepository;
import org.codenova.studymate.repository.StudyMemberRepository;
import org.codenova.studymate.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@AllArgsConstructor
public class WelcomeController {
    private AvatarRepository avatarRepository;
    private StudyMemberRepository studyMemberRepository;

    @RequestMapping({"/", "/index"})
    public String indexHandle(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "index";
        } else {
            User user = (User) session.getAttribute("user");
            model.addAttribute("user", user);

            Avatar userAvatar = avatarRepository.findById(user.getAvatarId());
            model.addAttribute("userAvatar", userAvatar);

            List<StudyMember> studyList =studyMemberRepository.findByUserId(user.getId());
            model.addAttribute("studyList", studyList);


            return "index-authenticated";
        }

    }
}
