package org.codenova.studymate.repository;

import lombok.AllArgsConstructor;
import org.codenova.studymate.model.entity.StudyMember;
import org.codenova.studymate.model.query.StudyMemberWithGroupDetail;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class StudyMemberRepository {
    private SqlSessionTemplate sqlSessionTemplate;

    public int createApproved(StudyMember studyMember) {
        return sqlSessionTemplate.insert("studyMember.createApproved", studyMember);
    }

    public int createPending(StudyMember studyMember) {
        return sqlSessionTemplate.insert("studyMember.createPending", studyMember);
    }

    public int updateJoinedAtById(int id) {
        return sqlSessionTemplate.update("studyMember.updateJoinedAtById", id);
    }

    public List<StudyMember>findByUserId(String userId) {
        return sqlSessionTemplate.selectList("studyMember.findByUserId", userId);
    }

    public StudyMember findByUserIdAndGroupId(Map params) {
        return sqlSessionTemplate.selectOne("studyMember.findByUserIdAndGroupId", params);
    }

    public int deleteById(int id) {
        return sqlSessionTemplate.delete("studyMember.deleteById", id);
    }

    public int deleteByGroupId(String groupId) {
        return sqlSessionTemplate.delete("studyMember.deleteByGroupId", groupId);
    }

    public List<StudyMemberWithGroupDetail> findWithGroupDetailByUserId(String userId){
        return sqlSessionTemplate.selectList("studyMember.findWithGroupDetailByUserId", userId);
    }

}
