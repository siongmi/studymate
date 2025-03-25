package org.codenova.studymate.repository;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.PostReaction;
import org.codenova.studymate.model.query.FeelingStats;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class PostRectionRepository {
    private SqlSessionTemplate sqlSessionTemplate;

    public int create(PostReaction postReaction){
        return sqlSessionTemplate.insert("postReaction.create", postReaction);
    }

    public List<PostReaction> findByPostId(int postId){
        return sqlSessionTemplate.selectList("postReaction.findByPostId", postId);
    }

    public PostReaction findByWriterIdAndPostId(Map map) {
        return sqlSessionTemplate.selectOne("postReaction.findByWriterIdAndPostId", map);
    }

    public int deleteById(int id) {
        return sqlSessionTemplate.delete("postReaction.deleteById", id);
    }

    public List<FeelingStats> countFeelingByPostId(int postId) {
        return sqlSessionTemplate.selectList("postReaction.countFeelingByPostId", postId);
    }
}
