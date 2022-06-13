package kr.co.abby.instagram.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.co.abby.instagram.mapper.InstaMapper;
import kr.co.abby.instagram.vo.LinkedAccountVo;
import kr.co.abby.instagram.vo.LogVo;


@Service
public class InstaServiceImpl implements InstaService {
	
	// 생성자로 의존성 주입
	private final InstaMapper instaMapper;
	
	public InstaServiceImpl(InstaMapper instaMapper) {
        this.instaMapper = instaMapper;
    }
	
	// 계정정보 DB 저장
	@Override
	public int addInfo(LinkedAccountVo instaAccVo) throws Exception {
		
		int result = 0;
		
		result = instaMapper.addInfo(instaAccVo);
		
		return result;
		
	}
	
	@Override
	public int checkId(String pageId) throws Exception {
		
		int result = 0;
		
		result = instaMapper.checkId(pageId);
		
		return result;
		
	}
	
	@Override
	public List<LinkedAccountVo> getLinkedAccount() throws Exception {
		
		List<LinkedAccountVo> result = instaMapper.getLinkedAccount();
		return result;
		
	}
	
	@Override
	public int addLog(LogVo logVo) throws Exception {
		
		int result = 0;
		result = instaMapper.addLog(logVo);
		return result;
		
	}
	
	@Override
	// updateErrorLog
	public int updateLog(LogVo logVo) throws Exception {
		
		int result = 0;
		result = instaMapper.updateLog(logVo);
		return result;
		
	}
	
	@Override
	public int addACcount(String filePath) throws Exception {
		
		int result = instaMapper.addACcount(filePath);
		return result;
		
	}
	
	@Override
	public int addMedia(String filePath) throws Exception {
		
		int result = instaMapper.addMedia(filePath);
		return result;
		
	}
	
	@Override
	public int addComment(String filePath) throws Exception {
		
		int result = instaMapper.addComment(filePath);
		return result;
		
	}
	
	
	
}
