package kr.co.abby.youtube.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.co.abby.youtube.mapper.YoutubeMapper;
import kr.co.abby.youtube.vo.LinkedChannelVo;
import kr.co.abby.youtube.vo.LogVo;
import kr.co.abby.youtube.vo.PlayListItemsVo;

@Service
public class YoutubeServiceImpl implements YoutubeService {


	// 생성자로 의존성 주입
	private final YoutubeMapper youtubeMapper;
	
	public YoutubeServiceImpl(YoutubeMapper youtubeMapper) {
        this.youtubeMapper = youtubeMapper;
    }
	
	// key 가져오기
	@Override
	public List<LinkedChannelVo> getLinkedChannel() throws Exception
	{
		List<LinkedChannelVo> result = youtubeMapper.getLinkedChannel();
		return result;
		
	}
	
	// 재생목록 pk키 가져오기
	@Override
	public List<PlayListItemsVo> getVideoListSeq(String datehh) throws Exception {
		
		//System.out.println("service datehh: " + datehh); // 확인완료
		
		List<PlayListItemsVo> result = youtubeMapper.getVideoListSeq(datehh);
		
		return result;
		
	}
	
	
	@Override
	public int addLog(LogVo logVo) throws Exception {
		
		int result = 0;
		result = youtubeMapper.addLog(logVo);
		return result;
		
	}
	
	// updateErrorLog
	@Override
	public int updateLog(LogVo logVo) throws Exception {
		
		int result = 0;
		result = youtubeMapper.updateLog(logVo);
		return result;
		
	}
	
	@Override
	public LogVo getLog(int seq) throws Exception {
		
		LogVo result = youtubeMapper.getLog(seq);
		return result;
		
	}

	@Override
	public int insertChannel(String filePath) throws Exception {
		
		int result = 0;
		result = youtubeMapper.insertChannel(filePath);
		return result;
		
	}

	@Override
	public int insertPlayListItems(String filePath) throws Exception {
	
		int result = 0;
		result = youtubeMapper.insertPlayListItems(filePath);
		return result;
	}
	
	@Override
	public int insertVideos(String filePath) throws Exception {
		
		int result = 0;
		result = youtubeMapper.insertVideos(filePath);
		return result;
		
	}
	
	@Override
	public int insertComments(String filePath) throws Exception {
		
		int result = 0;
		result = youtubeMapper.insertComments(filePath);
		return result;
	}
	
}

