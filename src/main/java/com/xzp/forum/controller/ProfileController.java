package com.xzp.forum.controller;

import com.xzp.forum.dao.*;
import com.xzp.forum.model.Image;
import com.xzp.forum.model.Topic;
import com.xzp.forum.model.User;
import com.xzp.forum.service.QiniuService;
import com.xzp.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 个人简介接口
 */
@Controller
public class ProfileController {

    @Autowired
    QiniuService qiniuService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private TopicDao topicDao;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private ImageDao imageDao;
    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/profile", method = RequestMethod.GET)
    public String displayMyProfile(Model model) {
        User user = hostHolder.getUser();
        Long points = userDao.getPoints(user.getId());
//		jedisAdapter.zadd(rankKey, points, user.getUsername());

        Long numberOfTopics = topicDao.countTopicsByUser_Id(user.getId());
        Long numberOfAnswers = answerDao.countAnswersByUser_Id(user.getId());
        Long numberOfHelped = answerDao.countAnswersByUser_IdAndUseful(user.getId(), true);
        List<String> myImgs = imageDao.getImgByUserId(user.getId());
        List<String> myAllImgs = imageDao.getAllImgByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("newMessage", messageDao.countMessageByToId(user.getId()));
        model.addAttribute("points", points);
        model.addAttribute("numberOfTopics", numberOfTopics);
        model.addAttribute("numberOfAnswers", numberOfAnswers);
        model.addAttribute("numberOfHelped", numberOfHelped);
        model.addAttribute("myImgs", myImgs);
        model.addAttribute("isHasMoreImg", myAllImgs.size() > myImgs.size());
        model.addAttribute("switch", true);
        return "profile";
    }

    @RequestMapping(path = "/profile/{id}", method = RequestMethod.GET)
    public String displayProfileById(@PathVariable Long id, Model model) {
        User user = userDao.getUserById(id);
        Long points = userDao.getPoints(user.getId());
        Long numberOfTopics = topicDao.countTopicsByUser_Id(id);
        Long numberOfAnswers = answerDao.countAnswersByUser_Id(id);
        Long numberOfHelped = answerDao.countAnswersByUser_IdAndUseful(id, true);
        List<String> myImgs = imageDao.getImgByUserId(user.getId());
        List<String> myAllImgs = imageDao.getAllImgByUserId(user.getId());

        User otherUser = hostHolder.getUser();
        model.addAttribute("user", otherUser);
        model.addAttribute("otherUser", user);
        model.addAttribute("newMessage", messageDao.countMessageByToId(hostHolder.getUser().getId()));
        model.addAttribute("points", points);
        model.addAttribute("numberOfTopics", numberOfTopics);
        model.addAttribute("numberOfAnswers", numberOfAnswers);
        model.addAttribute("numberOfHelped", numberOfHelped);
        model.addAttribute("myImgs", myImgs);
        model.addAttribute("isHasMoreImg", myAllImgs.size() > myImgs.size());
        model.addAttribute("switch", (user.getId().equals(otherUser.getId())) ? true : false);

        return "profile";
    }

    @RequestMapping(path = "/profile", method = RequestMethod.POST)
    public View addTask(@RequestParam("category") String category, @RequestParam("title") String title,
                        @RequestParam("content") String content, @RequestParam("code") String code,
                        @RequestParam("id_user") String id_user, HttpServletRequest request) {
        Topic topic = new Topic();
        topic.setCategory(category);
        if (Objects.equals(code, "")) {
            topic.setCode(null);
        } else {
            topic.setCode(code);
        }
        topic.setContent(content);
        topic.setTitle(title);
        topic.setCreatedDate(new Date());
        topic.setIdUser(Integer.parseInt(id_user));
        topic.setUser(userDao.getUserById(Long.parseLong(id_user)));

        topicDao.addTopic(topic);
        String contextPath = request.getContextPath();
        return new RedirectView(contextPath + "/profile");
    }

    @RequestMapping(path = "/imageWall/{id}", method = RequestMethod.GET)
    public String imageWall(@PathVariable Long id, Model model) {
        User user = userDao.getUserById(id);
        List<String> myAllImgs = imageDao.getAllImgByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("myImgs", myAllImgs);
        model.addAttribute("newMessage", messageDao.countMessageByToId(user.getId()));
        return "imageWall";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request, Model model) {
        try {
            if (file != null) {
                //上传文件的存放路径
                String path = "E://sc//forum//src//main//resources//static//images//";

                //获取上传文件的文件名
                String fileName = file.getOriginalFilename();

                if (!fileName.isEmpty()) {
                    //如果没有path目录，创建它
                    File targetFile = new File(path);
                    if (!targetFile.exists()) {
                        System.out.println("创建" + targetFile.isDirectory());
                        System.out.println(targetFile.mkdirs());
                    }

                    try {
                        file.transferTo(new File(path + fileName));
                        //设置文件存放路径
                        Image image = new Image();
                        image.setImgUrl("/images/" + fileName);
                        image.setIdUser(hostHolder.getUser().getId());
                        imageDao.addImg(image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            User user = hostHolder.getUser();
            Long points = userDao.getPoints(user.getId());
            Long numberOfTopics = topicDao.countTopicsByUser_Id(user.getId());
            Long numberOfAnswers = answerDao.countAnswersByUser_Id(user.getId());
            Long numberOfHelped = answerDao.countAnswersByUser_IdAndUseful(user.getId(), true);
            List<String> myImgs = imageDao.getImgByUserId(user.getId());
            List<String> myAllImgs = imageDao.getAllImgByUserId(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("otherUser", user);
            model.addAttribute("newMessage", messageDao.countMessageByToId(user.getId()));
            model.addAttribute("points", points);
            model.addAttribute("numberOfTopics", numberOfTopics);
            model.addAttribute("numberOfAnswers", numberOfAnswers);
            model.addAttribute("numberOfHelped", numberOfHelped);
            model.addAttribute("myImgs", myImgs);
            model.addAttribute("isHasMoreImg", myAllImgs.size() > myImgs.size());
            model.addAttribute("switch", true);
            return "profile";
        } catch (Exception e) {
            e.printStackTrace();
            return "profile";
        }
    }

    @RequestMapping(path = "/profile/message", method = RequestMethod.GET)
    public View topicsTransform(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return new RedirectView(contextPath + "/message");
    }

    @RequestMapping(path = "/imageWall/message", method = RequestMethod.GET)
    public View messageTransform(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return new RedirectView(contextPath + "/message");
    }
}
