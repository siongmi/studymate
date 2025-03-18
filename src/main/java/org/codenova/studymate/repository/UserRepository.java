package org.codenova.studymate.repository;

import org.codenova.studymate.model.User;
import org.mybatis.spring.SqlSessionTemplate;

public class UserRepository {
    private SqlSessionTemplate template;

    public int create(User user){
        return template.insert("user.create", user);
    }

    public User findById(String id){
        return template.selectOne("user.findById" , id);
    }
}
