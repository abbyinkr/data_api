package kr.co.abby.instagram.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import kr.co.abby.instagram.vo.LinkedAccountVo;
import kr.co.abby.instagram.vo.LogVo;



@Repository
@Mapper
public interface InstaMapper {
	
	public int addInfo(LinkedAccountVo instaAccVo) throws Exception;
	
	public int checkId(String pageId) throws Exception;

	public List<LinkedAccountVo> getLinkedAccount() throws Exception;
	
	public int addLog(LogVo logVo) throws Exception;
	
	public int updateLog(LogVo logVo) throws Exception;
	
	public int addACcount(String filePath) throws Exception;
	
	public int addMedia(String filePath) throws Exception;
	
	public int addComment(String filePath) throws Exception;
	
	
}
