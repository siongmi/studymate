package org.codenova.studymate.controller;

import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Select;
import org.codenova.studymate.model.entity.*;
import org.codenova.studymate.model.query.UserWithAvatar;
import org.codenova.studymate.model.vo.PostMeta;
import org.codenova.studymate.model.vo.StudyGroupWithCreator;
import org.codenova.studymate.repository.*;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/study")
@AllArgsConstructor
public class StudyController {
    private StudyGroupRepository studyGroupRepository;
    private StudyMemberRepository studyMemberRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;
    private AvatarRepository avatarRepository;
    private PostRectionRepository postRectionRepository;





    @RequestMapping("/create")
    public String createHandle() {
        System.out.println("create...");
        return "study/create";
    }


    @Transactional
    @RequestMapping("/create/verify")
    public String createVerifyHandle(@ModelAttribute StudyGroup studyGroup,
                                     @SessionAttribute("user") UserWithAvatar user) {
        String randomId = UUID.randomUUID().toString().substring(24);

        studyGroup.setId(randomId);
        studyGroup.setCreatorId(user.getId());
        studyGroupRepository.create(studyGroup);

        StudyMember studyMember = new StudyMember();
        studyMember.setUserId(user.getId());
        studyMember.setGroupId(studyGroup.getId());
        studyMember.setRole("리더");
        studyMemberRepository.createApproved(studyMember);

        studyGroupRepository.addMemberCountById(studyGroup.getId());

        return "redirect:/study/" + randomId;
    }

    @RequestMapping("/search")
    public String searchHandle(@RequestParam("word") Optional<String> word, Model model) {
        if (word.isEmpty()) {
            return "redirect:/";
        }
        String wordValue = word.get();
        List<StudyGroup> result = studyGroupRepository.findByNameLikeOrGoalLike("%" + wordValue + "%");
        List<StudyGroupWithCreator> convertedResult = new ArrayList<>();

        for (StudyGroup one : result) {
            User found = userRepository.findById(one.getCreatorId());

            // StudyGroupWithCreator c = new StudyGroupWithCreator(one, found);
/*            StudyGroupWithCreator c = new StudyGroupWithCreator();
                c.setCreator(found);
                c.setGroup(one);
*/
            StudyGroupWithCreator c = StudyGroupWithCreator.builder().group(one).creator(found).build();
            convertedResult.add(c);
        }


        System.out.println("search count : " + result.size());
        model.addAttribute("count", convertedResult.size());
        model.addAttribute("result", convertedResult);


        return "study/search";
    }


    // 스터디 상세보기 핸들러
    @RequestMapping("/{id}")
    public String viewHandle(@PathVariable("id") String id, Model model, @SessionAttribute("user") UserWithAvatar user) {
        // System.out.println(id);
        StudyGroup group = studyGroupRepository.findById(id);
        if (group == null) {
            return "redirect:/";
        }
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", id);
        map.put("userId", user.getId());
        StudyMember status = studyMemberRepository.findByUserIdAndGroupId(map);
        if (status == null) {
            // 아직 참여한적이 없다
            model.addAttribute("status", "NOT_JOINED");
        } else if (status.getJoinedAt() == null) {
            // 승인대기중
            model.addAttribute("status", "PENDING");
        } else if (status.getRole().equals("멤버")) {
            // 멤버이다
            model.addAttribute("status", "MEMBER");
        } else {
            // 리더이다.
            model.addAttribute("status", "LEADER");
        }

        model.addAttribute("group", group);

        List<Post> posts = postRepository.findByGroupId(id);
        List<PostMeta> postMetas = new ArrayList<>();

        PrettyTime prettyTime = new PrettyTime();
        for (Post post : posts) {
//            long b = Duration.between(post.getWroteAt(), LocalDateTime.now()).getSeconds();
//            System.out.println(b);
            PostMeta cvt = PostMeta.builder()
                    .id(post.getId())
                    .content(post.getContent())
                    .writerName(userRepository.findById(post.getWriterId()).getName())
                    .writerAvatar(avatarRepository.findById(userRepository.findById(post.getWriterId()).getAvatarId()).getImageUrl())
                    .time(prettyTime.format(post.getWroteAt()))
                    .reactions(postRectionRepository.countFeelingByPostId(post.getId()))
                    .build();
            postMetas.add(cvt);
        }

        model.addAttribute("postMetas", postMetas);

        return "study/view";
    }


    @Transactional
    @RequestMapping("/{id}/join")
    public String joinHandle(@PathVariable("id") String id, @SessionAttribute("user") UserWithAvatar user) {
        /*
            StudyMember member = new StudyMember();
            member.setUserId(user.getId());
            member.setGroupId(id);
            member.setRole("멤버");
        */
        boolean exist = false;
        List<StudyMember> list = studyMemberRepository.findByUserId(user.getId());
        for (StudyMember one : list) {
            if (one.getGroupId().equals(id)) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            StudyMember member = StudyMember.builder().
                    userId(user.getId()).groupId(id).role("멤버").build();
            StudyGroup group = studyGroupRepository.findById(id);
            if (group.getType().equals("공개")) {
                studyMemberRepository.createApproved(member);
                studyGroupRepository.addMemberCountById(id);
            } else {
                studyMemberRepository.createPending(member);
            }
        }

        return "redirect:/study/" + id;
    }


    // 탈퇴 요청 처리 핸들러
    @RequestMapping("/{groupId}/leave")
    public String leaveHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") UserWithAvatar user, Model model) {
        String userId = user.getId();
        Map map = Map.of("groupId", groupId, "userId", userId);

        StudyMember found = studyMemberRepository.findByUserIdAndGroupId(map);
        studyMemberRepository.deleteById(found.getId());

        studyGroupRepository.subtractMemberCountById(groupId);
        return "redirect:/";
    }

    // 신청 철회 요청 핸들러
    @RequestMapping("/{groupId}/cancel")
    public String cancelHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") UserWithAvatar user, Model model) {
        String userId = user.getId();
        Map map = Map.of("groupId", groupId, "userId", userId);

        StudyMember found = studyMemberRepository.findByUserIdAndGroupId(map);
        if (found != null && found.getJoinedAt() == null && found.getRole().equals("멤버")) {
            studyMemberRepository.deleteById(found.getId());
        }

        return "redirect:/study/" + groupId;
    }

    @Transactional
    @RequestMapping("/{groupId}/remove")
    public String removeHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") UserWithAvatar user) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId);

        if (studyGroup != null && studyGroup.getCreatorId().equals(user.getId())) {
            studyMemberRepository.deleteByGroupId(groupId);
            studyGroupRepository.deleteById(groupId);
            return "redirect:/";
        } else {
            return "redirect:/study/" + groupId;
        }
    }

    @RequestMapping("/{groupId}/approve")
    public String approveHandle(@PathVariable("groupId") String groupId,
                                @RequestParam("targetUserId") String targetUserId,
                                @SessionAttribute("user") UserWithAvatar user) {

        StudyGroup studyGroup = studyGroupRepository.findById(groupId);


        if (studyGroup != null && studyGroup.getCreatorId().equals(user.getId())) {
            StudyMember found = studyMemberRepository.findByUserIdAndGroupId(
                    Map.of("userId", targetUserId, "groupId", groupId)
            );

            if (found != null) {
                studyMemberRepository.updateJoinedAtById(found.getId());
                studyGroupRepository.addMemberCountById(groupId);
            }
        }

        return "redirect:/study/" + groupId;
    }

    // 그룹내 새글 등록
    @RequestMapping("/{groupId}/post")
    public String postHandle(@PathVariable("groupId") String id,
                             @ModelAttribute Post post,
                             @SessionAttribute("user") UserWithAvatar user) {
        /*
         모델 attribute 로 파라미터는 받았을텐데, 빠진 정보들이 있을거임. 이걸 추가로 set  .
         postRepository를 이용해서 create 메서드 작성
         */
        post.setWriterId(user.getId());
        post.setWroteAt(LocalDateTime.now());
//        post.setGroupId();
//        post.setContent();
        postRepository.create(post);


        return "redirect:/study/" + id;
    }

    // 글에 감정 남기기 요청 처리 핸들
    @RequestMapping("/{groupId}/post/{postId}/reaction")
    public String postReactionHandle(@ModelAttribute PostReaction postReaction, @SessionAttribute("user") UserWithAvatar user) {

        PostReaction found =
                postRectionRepository.findByWriterIdAndPostId(Map.of("writerId", user.getId(), "postId", postReaction.getPostId()));

        if (found != null) {
            postRectionRepository.deleteById(found.getId());
        }

        postReaction.setWriterId(user.getId());
        postRectionRepository.create(postReaction);


        return "redirect:/study/" + postReaction.getGroupId();
    }




    @ModelAttribute("user")
    public UserWithAvatar addUser(@SessionAttribute("user") UserWithAvatar user) {
        System.out.println("addUser...");
        return user;
    }
}
