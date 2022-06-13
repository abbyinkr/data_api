package kr.co.abby.youtube.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import kr.co.abby.youtube.vo.LinkedChannelVo;
import kr.co.abby.youtube.vo.LogVo;
import kr.co.abby.youtube.vo.PlayListItemsVo;


@Repository
@Mapper
public interface YoutubeMapper {

	public List<LinkedChannelVo> getLinkedChannel() throws Exception;
	
	public List<PlayListItemsVo> getVideoListSeq(String datehh) throws Exception;
	
	public int addLog(LogVo logVo) throws Exception;
	
	public int updateLog(LogVo logVo) throws Exception;
	
	public LogVo getLog(int seq) throws Exception;
	
	public int insertChannel(String filePath) throws Exception;
	
	public int insertPlayListItems(String filePath) throws Exception;
	
	public int insertVideos(String filePath) throws Exception;
	
	public int insertComments(String filePath) throws Exception;
	
}
