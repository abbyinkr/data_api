package kr.co.abby.instagram.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.co.abby.instagram.vo.LinkedAccountVo;
import kr.co.abby.instagram.vo.LogVo;


// 인스타 인터페이스
@Service
public interface InstaService {

	// 연동계정 DB 저장
	public int addInfo(LinkedAccountVo instaAccVo) throws Exception;
	
	// 연동계정 페이지아이디 중복체크
	public int checkId(String pageId) throws Exception;
	
	// 연동계정 정보 가져오기
	public List<LinkedAccountVo> getLinkedAccount() throws Exception;

	// addLog
	public int addLog(LogVo logVo) throws Exception;
	
	// updateErrorLog
	public int updateLog(LogVo logVo) throws Exception;
	
	public int addACcount(String filePath) throws Exception;

	public int addMedia(String filePath) throws Exception;
	
	public int addComment(String filePath) throws Exception;
	
	
}
