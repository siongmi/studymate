package org.codenova.studymate.repository;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.Avatar;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class AvatarRepository {
    private SqlSessionTemplate template;

    public List<Avatar> findAll(){
        return template.selectList("avatar.findAll");
    }
}
