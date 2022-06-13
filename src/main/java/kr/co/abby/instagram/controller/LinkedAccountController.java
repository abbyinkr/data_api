package kr.co.abby.instagram.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import kr.co.abby.instagram.service.InstaServiceImpl;
import kr.co.abby.instagram.vo.LinkedAccountVo;


@Controller
public class LinkedAccountController {
	
	@Autowired 
	InstaServiceImpl instaservice;
	
	// 로거
	private static Log logger = LogFactory.getLog(GatherInstagramController.class);
	
	@PostMapping("/insta/linkedaccount")
	public ResponseEntity<String> linkedAccount(HttpServletRequest request, LinkedAccountVo linkedAccVo) throws Exception {
		
		String accessToken = null;
		String pageToken = null;
		String businessId = null;
		String pageId= null;
		String fbName = null;
		String regdate = null;
		int status;
		
		// 폼데이터 받기
		status = Integer.parseInt(request.getParameter("status"));
		accessToken = request.getParameter("longAccessToken");
		String userId = request.getParameter("userId");

		// 유저네임
		try {
			
			URL url = new URL("https://graph.facebook.com/v13.0/me?access_token="+ accessToken);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET"); // http 메서드
			conn.setRequestProperty("Content-Type", "application/json"); // header Content-Type 정보
			conn.setRequestProperty("auth", "myAuth"); // header의 auth 정보
			conn.setDoOutput(true); 
			
			// 서버로부터 데이터 읽어오기
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			
			while((line = br.readLine()) != null) { // 읽을 수 있을 때 까지 반복
				sb.append(line);
			}
			
			JSONObject obj = new JSONObject(sb.toString()); // json으로 변경 (역직렬화)
			//System.out.println(obj);
			
			fbName = obj.getString("name");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			
			URL url = new URL("https://graph.facebook.com/v13.0/me/accounts?access_token="+accessToken);
			
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			
			conn.setRequestMethod("GET"); // http 메서드
			conn.setRequestProperty("Content-Type", "application/json"); // header Content-Type 정보
			conn.setRequestProperty("auth", "myAuth"); // header의 auth 정보
			conn.setDoOutput(true); // 서버로부터 받는 값이 있다면 true
			
			// 서버로부터 데이터 읽어오기
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			
			while((line = br.readLine()) != null) { // 읽을 수 있을 때 까지 반복
				sb.append(line);
			}
			
			JSONObject obj = new JSONObject(sb.toString()); // json으로 변경 (역직렬화)
			
			// 장기페이지토큰
			pageToken = obj.getJSONArray("data").getJSONObject(0).getString("access_token");
			
			// 페이지 아이디
			pageId = obj.getJSONArray("data").getJSONObject(0).getString("id");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println("pageId : " + pageId);
		int checkIdResult = instaservice.checkId(pageId);
		
		System.out.println("checkIdResult : " + checkIdResult);
		if (checkIdResult >= 1) {
			logger.info("instgram 계정정보 : 중복계정 존재");
			return new ResponseEntity<>("DB에 이미 존재하는 계정입니다.", HttpStatus.OK);
			
		}
		
		// 인스타그램 비즈니스 아이디 
		try {

			URL url = new URL("https://graph.facebook.com/v13.0/"
					+ pageId
					+ "?fields=instagram_business_account&access_token="+accessToken);
			
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			
			conn.setRequestMethod("GET"); // http 메서드
			conn.setRequestProperty("Content-Type", "application/json"); // header Content-Type 정보
			conn.setRequestProperty("auth", "myAuth"); // header의 auth 정보
			conn.setDoOutput(true); // 서버로부터 받는 값이 있다면 true
			
			// 서버로부터 데이터 읽어오기
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			
			while((line = br.readLine()) != null) { // 읽을 수 있을 때 까지 반복
				sb.append(line);
			}
			
			JSONObject obj = new JSONObject(sb.toString()); // json으로 변경 (역직렬화)
			System.out.println("------");
			System.out.println(obj);
			//System.out.println(obj.getJSONArray("data").getJSONObject(0).getString("access_token"));
			
			System.out.println(obj.getJSONObject("instagram_business_account"));
			System.out.println(obj.getJSONObject("instagram_business_account").getString("id")); 
			
			businessId = obj.getJSONObject("instagram_business_account").getString("id");
			
			regdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		linkedAccVo.setAccessToken(accessToken);
		linkedAccVo.setPageToken(pageToken);
		linkedAccVo.setBusinessId(businessId);
		linkedAccVo.setPageId(pageId);
		linkedAccVo.setRegdate(regdate);
		linkedAccVo.setFbName(fbName);
		linkedAccVo.setStatus(status);
		
		int addResult = instaservice.addInfo(linkedAccVo);
		
		return addResult == 1 ? new ResponseEntity<>("sucess", HttpStatus.OK) : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		
		
	}
	
	
	
	

}
