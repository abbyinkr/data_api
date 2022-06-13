package kr.co.abby.youtube.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import kr.co.abby.youtube.vo.LinkedChannelVo;
import kr.co.abby.youtube.vo.LogVo;
import kr.co.abby.youtube.vo.PlayListItemsVo;


@Repository
@Mapper
public class YoutubeMapperImpl implements YoutubeMapper {
	
	@Qualifier("sqlSessionTemplate")
	@Autowired
	public SqlSessionTemplate sqlSession;
	
	@Override
	public List<LinkedChannelVo> getLinkedChannel() throws Exception {
		
		List<LinkedChannelVo> result = sqlSession.selectList("kr.co.abby.youtube.mapper.YoutubeMapper.getLinkedChannel");
				
		return result;
	}
	
	@Override
	public List<PlayListItemsVo> getVideoListSeq(String datehh) throws Exception {
		
		System.out.println("mapper datehh : " + datehh);
		
		List<PlayListItemsVo> result = sqlSession.selectList("kr.co.abby.youtube.mapper.YoutubeMapper.getVideoListSeq", datehh);
		
		return result;
		
	}
	
	
	@Override
	public int addLog(LogVo logVo) throws Exception {
		
		int result = 0;
		
		result = sqlSession.selectOne("kr.co.abby.youtube.mapper.YoutubeMapper.addLog", logVo);
		
		return result;
		
	}
	
	@Override
	public int updateLog(LogVo logVo) throws Exception {
		
		int result = 0;
		
		result = sqlSession.selectOne("kr.co.abby.youtube.mapper.YoutubeMapper.updateLog", logVo);
		
		return result;
		
	}
	
	@Override
	public LogVo getLog(int seq) throws Exception {
		
		LogVo result = sqlSession.selectOne("kr.co.abby.youtube.mapper.YoutubeMapper.getLog", seq);
		
		return result;
	}
	
	@Override
	public int insertChannel(String filePath) throws Exception {
		
		int result = 0;
		
		result = sqlSession.insert("kr.co.abby.youtube.mapper.YoutubeMapper.insertChannel", filePath);
		
		return result;
		
	}
	
	@Override
	public int insertPlayListItems(String filePath) throws Exception {

		int result = 0;
		
		result = sqlSession.insert("kr.co.abby.youtube.mapper.YoutubeMapper.insertPlayListItems", filePath);
		
		return result;
		
	}
	
	@Override
	public int insertVideos(String filePath) throws Exception {
		
		int result = 0;
		
		result = sqlSession.insert("kr.co.abby.youtube.mapper.YoutubeMapper.insertVideos", filePath);
		
		return result;
		
	}

	@Override
	public int insertComments(String filePath) throws Exception {
		
		int result = 0;
		
		result = sqlSession.insert("kr.co.abby.youtube.mapper.YoutubeMapper.insertComments", filePath);
		
		return result;
		
	}
	
}
