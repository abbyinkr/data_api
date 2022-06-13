package kr.co.abby.youtube.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.co.abby.youtube.vo.LinkedChannelVo;
import kr.co.abby.youtube.vo.LogVo;
import kr.co.abby.youtube.vo.PlayListItemsVo;

@Service
public interface YoutubeService {

	// key 가져오기
	public List<LinkedChannelVo> getLinkedChannel() throws Exception;
	
	// 재생목록 pk키 가져오기
	public List<PlayListItemsVo> getVideoListSeq(String datehh) throws Exception;
	
	// addLog
	public int addLog(LogVo logVo) throws Exception;
		
	// updateErrorLog
	public int updateLog(LogVo logVo) throws Exception;
	
	public LogVo getLog(int seq) throws Exception;
	
	public int insertChannel(String filePath) throws Exception;
	
	public int insertPlayListItems(String filePath) throws Exception;
	
	public int insertVideos(String filePath) throws Exception;
	
	public int insertComments(String filePath) throws Exception;
	
		
}

