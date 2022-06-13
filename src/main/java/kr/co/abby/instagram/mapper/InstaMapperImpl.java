package kr.co.abby.instagram.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import kr.co.abby.instagram.vo.LinkedAccountVo;
import kr.co.abby.instagram.vo.LogVo;

@Repository
@Mapper
public class InstaMapperImpl implements InstaMapper {
	
	@Qualifier("sqlSessionTemplate")
	@Autowired
	public SqlSessionTemplate sqlSession;
	
	
	// 계정정보 DB 저장
	@Override
	public int addInfo(LinkedAccountVo instaAccVo) throws Exception{
		
		int result = 0;
		
		result = sqlSession.insert("kr.co.abby.instagram.mapper.InstaMapper.addInfo", instaAccVo);
		
		return result;
		
	}
	
	@Override
	public int checkId(String pageId) throws Exception {
		
		int result = 0;
		
		result = sqlSession.selectOne("kr.co.abby.instagram.mapper.InstaMapper.checkId", pageId);
		
		return result;
		
	}
	
	@Override
	public List<LinkedAccountVo> getLinkedAccount() throws Exception {
		
		 List<LinkedAccountVo> result = sqlSession.selectList("kr.co.abby.instagram.mapper.InstaMapper.getLinkedAccount");
		 
		 return result;
		
	}
	
	@Override
	public int addLog(LogVo logVo) throws Exception {
		
		int result = 0;
		
		result = sqlSession.selectOne("kr.co.abby.instagram.mapper.InstaMapper.addLog", logVo);
		
		return result;
		
	}
	
	@Override
	public int updateLog(LogVo logVo) throws Exception {
		
		int result = 0;
		
		result = sqlSession.selectOne("kr.co.abby.instagram.mapper.InstaMapper.updateLog", logVo);
		
		return result;
		
	}
	
	@Override
	public int addACcount(String filePath) throws Exception {
		
		int result = 0;
		result = sqlSession.selectOne("kr.co.abby.instagram.mapper.InstaMapper.addACcount", filePath);
		return result;
	}
	
	@Override
	public int addMedia(String filePath) throws Exception {
		
		int result = 0;
		result = sqlSession.selectOne("kr.co.abby.instagram.mapper.InstaMapper.addMedia", filePath);
		return result;
	}
	
	@Override
	public int addComment(String filePath) throws Exception {
		
		int result = 0;
		result = sqlSession.selectOne("kr.co.abby.instagram.mapper.InstaMapper.addComment", filePath);
		return result;
	}
	

}
