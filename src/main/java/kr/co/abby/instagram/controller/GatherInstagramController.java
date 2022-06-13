package kr.co.abby.instagram.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import kr.co.abby.instagram.service.InstaServiceImpl;
import kr.co.abby.instagram.vo.LinkedAccountVo;
import kr.co.abby.instagram.vo.LogVo;
import kr.co.abby.youtube.controller.GatherYoutubeController;
@Controller
public class GatherInstagramController {

	@Autowired
	InstaServiceImpl instaservice;

	// 로거
	private static Log logger = LogFactory.getLog(GatherInstagramController.class);

	// 기본 변수들 
	static String accessToken = null;
	static String pageToken = null;
	static String businessId = null;
	static String pageId = null;
	static String fbName = null;

	GatherYoutubeController youtubeController = new GatherYoutubeController();
	

	// 3개의 메서드(account, media, comment)

	// account 수집
	@Scheduled(cron = "0 0 0/3 1/1 * *") // 3시간마다
	@GetMapping("/insta/account")
	public void acount() throws Exception {
		
		String id = null;
		String biography = null;
		int followers_count = 0;
		int follows_count = 0;
		int media_count = 0;
		String name = null;
		String profile_picture_url = null;
		String username = null;
		
		// 날짜, 시간
		String regdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String hh = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"));
		String datehh = date + hh;

		List<LinkedAccountVo> result = instaservice.getLinkedAccount();

		accessToken = result.get(0).getAccessToken();
		businessId = result.get(0).getBusinessId();
		
		LogVo logVo = new LogVo();

		try {
			logVo.setDate(date);
			logVo.setHh(hh);
			logVo.setDatehh(datehh);
			logVo.setRegdate(regdate);
			logVo.setType("account");
			logVo.setStatus(0);
			logVo.setMessage("");
			instaservice.addLog(logVo);
			
			
			URL url = new URL("https://graph.facebook.com/v13.0/" + businessId
					+ "?fields=id%2Cbiography%2Cfollowers_count%2Cfollows_count%2Cmedia_count%2Cname"
					+ "%2Cprofile_picture_url%2Cusername%2Cwebsite&access_token=" + accessToken);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET"); 
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("auth", "myAuth"); 
			conn.setDoOutput(true);

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = br.readLine()) != null) { 
				sb.append(line);
			}

			JSONObject obj = new JSONObject(sb.toString()); 
		
			id = obj.getString("id");
			biography = obj.getString("biography");
			followers_count = obj.getInt("followers_count");
			follows_count = obj.getInt("followers_count");
			media_count = obj.getInt("media_count");
			name = obj.getString("name");
			profile_picture_url = obj.getString("profile_picture_url");
			username = obj.getString("username");

			// 문자열 처리 (쉼표, \n 등 제거)
			youtubeController.handleString(biography);
			youtubeController.handleString(username);
			
			logger.info("instagram acount 정보 수집 완료");
			
		} catch (Exception e) {
			
			catchExcetion(e, logVo, "instagram acount 정보 수집 실패");
			
		}

		// 수집한 데이터로 csv 파일 생성
		String filePath = "D:/devfolder/csv/instagram/account" +datehh+ ".csv";

		File file = null;

		BufferedWriter bw = null;

		String newLine = System.lineSeparator(); // 줄바꿈(\n)

		try {


			file = new File(filePath);

			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

			bw.write("date,hh,datehh,regdate,id,biography,followers_count,follows_count,media_count,name,profile_picture_url,username");
			bw.write(newLine);

			
			bw.write(date + "," + hh + "," + datehh + "," + regdate + "," + id + "," + biography + "," + followers_count
					+ "," + follows_count + "," + media_count + "," + name + "," + profile_picture_url + ","
					+ username);
			
			
			// 로그 남기기
			logger.info("instagram acount csv 파일 생성 완료");

		} catch (Exception e) {
			
			catchExcetion(e, logVo, "instagram acount csv 파일 생성 실패");
			
			
		} finally { // 닫기
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					
					e.printStackTrace();
					logger.error(e.getMessage());
					logger.error("instagram account BufferedReader close 실패");
					
				}
			}
		}


		try {
			
			// 생성한 CSV 파일을 account테이블에 입력
			instaservice.addACcount(filePath);
			logger.info("instagram account DB 입력 완료");
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			logger.error("instagram account DB LOAD DATA csv파일 입력 실패: Class Not Found Exection");
			logVo.setMessage(e.getMessage());
			logVo.setStatus(9);
			instaservice.updateLog(logVo);
			catchExcetion(e, logVo, "instagram account BufferedWriter close 실패");
			
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			logger.error("instagram account DB 입력 실패: SQL Exception");
			logVo.setMessage(e.getMessage());
			logVo.setStatus(9);
			instaservice.updateLog(logVo);
			catchExcetion(e, logVo, "instagram account BufferedWriter close 실패");
			
		} 
		
		// 로그 DB 업데이트
		logVo.setStatus(1);
		logVo.setMessage("");
		instaservice.updateLog(logVo);
		
	}
	
	// 미디어 수집
	@Scheduled(cron = "0 0 0/3 1/1 * *") //3시간마다
	@GetMapping("/insta/media")
	public void media() throws Exception {

		String media_id = null;

		String caption = null;
		int comments_count = 0;
		String id = null;
		// boolean is_comment_enabled = false;
		int like_count = 0;
		String media_product_type = null;
		String media_type = null;
		String media_url = null;
		String owner = null;
		String permalink = null;
		String shortcode = null;
		String timestamp = null;
		String username = null;
		String thumbnail_url = null;
		
		// 날짜, 시간
		String regdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String hh = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"));
		String datehh = date + hh;
		

		// csv 파일 설정
		String filePath = "D:/devfolder/csv/instagram/media" +datehh+ ".csv";

		File file = null;

		BufferedWriter bw = null;

		String newLine = System.lineSeparator(); // 줄바꿈(\n)
		
		
		// logVo 인스턴스 생성
		LogVo logVo = new LogVo();
		
		
		// try~ catch문으로 변경
		try {

			// DB로그 STEP1. 최초에 수집 DB로그 INSERT 
			// 최초에 메시지는 null
			// LogVo 세팅
			logVo.setDate(date);
			logVo.setHh(hh);
			logVo.setDatehh(datehh);
			logVo.setRegdate(regdate);
			logVo.setType("media");
			logVo.setStatus(0);
			logVo.setMessage("");
			// logVo INSERT 실행후 seq 를 받아와야 된다.
			instaservice.addLog(logVo);
			
			file = new File(filePath);
			// UTF-8로 지정하면 CSV 파일 자체에서는 한글이 깨지지만 LOADDATA에서 한글로 제대로 들어간다.
			// utf8로 생성, load data에서 UTF8MB4로 지정
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));
			
			// 디버깅을 위해 인코딩 변경
			//bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "MS949"));

			bw.write("date ,hh ,datehh ,regdate ,caption, comments_count, id, is_comment_enabled,"
					+ "like_count, media_product_type, media_type, media_url,"
					+ " owner, permalink, shortcode, timestamp, username");

			bw.write(newLine);
			
			// 로그
			logger.info("instagram media csv 파일 생성 및 컬럼 입력 완료");
			
			
		} catch (Exception e) {
			
			catchExcetion(e, logVo, "instagram media csv 파일 생성 및 컬럼 입력 실패");
			
		}
		

		// 비즈니스 아이디 가져오기
		List<LinkedAccountVo> result = instaservice.getLinkedAccount();
		accessToken = result.get(0).getAccessToken();
		businessId = result.get(0).getBusinessId();
		
	
		
		
		try {

			
			URL url = new URL("https://graph.facebook.com/v13.0/"+ businessId + "/media?access_token=" + accessToken);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET"); 
			conn.setRequestProperty("Content-Type", "application/json"); // header Content-Type 정보
			conn.setRequestProperty("auth", "myAuth"); // header의 auth 정보
			conn.setDoOutput(true);

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			JSONObject obj_media = new JSONObject(sb.toString()); // 

			for (int i = 0; i < obj_media.getJSONArray("data").length(); i++) {

				// 미디어 아이디
				media_id = obj_media.getJSONArray("data").getJSONObject(i).getString("id");
				// System.out.println(media_id);

				url = new URL("https://graph.facebook.com/v13.0/" + media_id + "?fields=caption%2Ccomments_count"
						+ "%2Cid%2Cis_comment_enabled%2Clike_count" + "%2Cmedia_product_type%2Cmedia_type"
						+ "%2Cmedia_url%2Cowner%2Cpermalink" + "%2Cshortcode%2Cthumbnail_url" + "%2Ctimestamp"
						+ "%2Cusername&access_token=" + accessToken);

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET"); 
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("auth", "myAuth"); 
				conn.setDoOutput(true);

				// 서버로부터 데이터 읽어오기
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				sb = new StringBuilder();
				line = null;

				while ((line = br.readLine()) != null) { 
					sb.append(line);
				}

				JSONObject obj = new JSONObject(sb.toString()); 
				
				id = obj.getString("id");
				caption = obj.getString("caption");
				comments_count = obj.getInt("comments_count");

				boolean is_comment_enabled = obj.getBoolean("is_comment_enabled");

				like_count = obj.getInt("like_count");
				media_product_type = obj.getString("media_product_type");
				media_type = obj.getString("media_type");

				media_url = obj.getString("media_url");
				owner = obj.getJSONObject("owner").getString("id");
				permalink = obj.getString("permalink");
				shortcode = obj.getString("shortcode");
				
				timestamp = obj.getString("timestamp");
				
				
				username = obj.getString("username");

				// thumbnail_url = obj.getString(thumbnail_url); // null 체크

				// 썸네일 url null 체크
				if (obj.has("thumbnail_url")) {
					thumbnail_url = obj.getString(thumbnail_url);
				} else {
					thumbnail_url = "";
				}

				bw.write(date + " , " + hh + " , " + datehh + " , " + regdate + " , " + caption + " , " + comments_count
						+ "," + id + " , " + is_comment_enabled + " , " + like_count + " , " + media_product_type + " , "
						+ media_type + " , " + media_url + " , " + owner + " , " + permalink + " , " + shortcode + " , "
						+ timestamp + " , " + username);

				bw.write(newLine);
				
				// bw 비우기
				bw.flush();


			} // end 반복문

			// 로그
			logger.info("instagram media 정보 수집 완료");
		
		} catch (Exception e) {
		
			catchExcetion(e, logVo, "instagram media 정보 수집 실패");
			
			
		} finally {
			
			// 닫기
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error(e.getMessage());
					logger.error("instagram media BufferedReader close 실패");
					
				}
			}
			
		}
		
		// 만들어진 csv파일을 media 에 입력
		
		try {
			logger.info("instagram media DB 입력 완료");
			
			instaservice.addMedia(filePath);
			
		} catch (ClassNotFoundException e) {
			
			catchExcetion(e, logVo, "instagram media DB 입력 실패 : Class Not Found Exection");
			
		} catch (SQLException e) {
			
			catchExcetion(e, logVo, "instagram media DB 입력 실패 : SQL Exception");
			
		}
		
		
		logVo.setStatus(1);
		logVo.setMessage("");
		instaservice.updateLog(logVo);
		

	}
	
	//코멘트 수집 

	@Scheduled(cron = "0 0 0/3 1/1 * *")  
	@GetMapping("/insta/comment")
	public void comment() throws Exception {
		
		String media_id = null;
		
		String id = null;
		String text = null;
		String timestamp = null;
		String parent_id = null;
		int isReplies = 0;

		// 날짜, 시간
		String regdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String hh = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"));
		String datehh = date + hh;
		
		// csv 파일 설정
		String filePath = "D:/devfolder/csv/instagram/comment"+datehh +".csv";

		File file = null;

		BufferedWriter bw = null;

		String newLine = System.lineSeparator(); 
		
		// logVo 생성
		LogVo logVo = new LogVo();
		
		
		try {
			
			logVo.setDate(date);
			logVo.setHh(hh);
			logVo.setDatehh(datehh);
			logVo.setRegdate(regdate);
			logVo.setType("comment");
			logVo.setStatus(0);
			logVo.setMessage("");
			instaservice.addLog(logVo);
			
			
			file = new File(filePath);
			
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

			bw.write("date,hh,datehh,regdate,media_id,from_id,id,text,timestamp,parent_id,isReplies");

			bw.write(newLine);
			
			bw.flush();
			
			logger.info("instagram comment csv파일 생성 및 컬럼 입력 완료");
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			logVo.setMessage(e.getMessage());
			logVo.setStatus(9);
			instaservice.updateLog(logVo);
			catchExcetion(e, logVo, "instagram account BufferedWriter close 실패");
		}
		
		List<LinkedAccountVo> result = instaservice.getLinkedAccount();
		accessToken = result.get(0).getAccessToken();
		businessId = result.get(0).getBusinessId();

		String from_id = businessId;

		try {

			URL url = new URL("https://graph.facebook.com/v13.0/"+businessId+"/media?access_token=" + accessToken);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET"); 
			conn.setRequestProperty("Content-Type", "application/json"); // header Content-Type 정보
			conn.setRequestProperty("auth", "myAuth"); // header의 auth 정보
			conn.setDoOutput(true);

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			JSONObject obj_media = new JSONObject(sb.toString()); 
			
			ArrayList<String> mediaIdList = new ArrayList<String>();

			for (int i = 0; i < obj_media.getJSONArray("data").length(); i++) {
				mediaIdList.add(obj_media.getJSONArray("data").getJSONObject(i).getString("id"));
			}
			
			for (int i = 0; i < mediaIdList.size() ; i++) {
				
				url = new URL("https://graph.facebook.com/v13.0/"+ mediaIdList.get(i)
						+ "/comments?access_token=" + accessToken);
				

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET"); 
				conn.setRequestProperty("Content-Type", "application/json"); 
				conn.setRequestProperty("auth", "myAuth"); 
				conn.setDoOutput(true);

				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				sb = new StringBuilder();
				line = null;

				while ((line = br.readLine()) != null) { 
					sb.append(line);
				}

				JSONObject comment = new JSONObject(sb.toString()); 
				boolean hasComment = !comment.getJSONArray("data").isEmpty();
				
				// 댓글이 있으면 csv 파일에 입력
				if (hasComment) {
					
					for (int j = 0; j <comment.getJSONArray("data").length() ; j++) {

						String commentId = comment.getJSONArray("data").getJSONObject(j).getString("id");
						
						url = new URL("https://graph.facebook.com/v13.0/"
								+ commentId
								+ "?fields=from%2Chidden%2Cid%2Clike_count%2Cparent_id%2Creplies"
								+ "%2Ctext%2Ctimestamp%2Cuser%2Cusername&access_token="+accessToken);

						conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.setRequestProperty("Content-Type", "application/json"); 
						conn.setRequestProperty("auth", "myAuth"); 
						conn.setDoOutput(true);

						br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						sb = new StringBuilder();
						line = null;

						while ((line = br.readLine()) != null) { 
							sb.append(line);
						}

						JSONObject commentInfo = new JSONObject(sb.toString());
						
						text = commentInfo.getString("text");
						timestamp = commentInfo.getString("timestamp");
						id = commentInfo.getString("id");
						media_id =  mediaIdList.get(i);
						
						// 문자열 처리 (쉼표, \n 등 제거)
						youtubeController.handleString(text);
						
						parent_id = null;
						isReplies = 0;
						
						
						bw.write(date+ "," + hh + "," + datehh + "," +regdate + "," + media_id + "," + from_id +", " + id + "," + 
						text+ " , " +timestamp+ " , " + parent_id+ "," + isReplies);

						bw.write(newLine);
						
						// parent 댓글에 replies 컬럼이 있으면 대댓글도 수집
						boolean hasReply = commentInfo.has("replies");
						
						if (hasReply) {
							
							// 대댓글 갯수
							int repliesCount = commentInfo.getJSONObject("replies").getJSONArray("data").length();
							//System.out.println("대댓 갯수: " +repliesCount);
							
							// 세번째 반복문(대댓글)
							for (int k = 0; k < repliesCount; k++) {
								
								text = commentInfo.getJSONObject("replies").getJSONArray("data").getJSONObject(i).getString("text");
								timestamp = commentInfo.getJSONObject("replies").getJSONArray("data").getJSONObject(i).getString("timestamp");
								id = commentInfo.getJSONObject("replies").getJSONArray("data").getJSONObject(i).getString("id");
								parent_id = commentInfo.getString("id");
								
								isReplies = 1;
								
								bw.write(date+ "," + hh + "," + datehh + "," +regdate + "," + media_id + "," +from_id + "," + id + "," + 
										text+ "," +timestamp+ "," + parent_id+ "," + isReplies);

								bw.write(newLine);
								
								bw.flush();
								
								
							}//end for문(대댓글)
							
						}// if문(대댓글여부)
						
					}// end for문(댓글아이디)
					
				}// end if(hasComment)
				
			}//end for문(미디어아이디)
			
			// bufferedWriter 닫기
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error(e.getMessage());
					logger.error("instagram comment BufferedReader close 실패");
				}
			}
			
			logger.info("instagram comment 정보 저장 완료");
			
		} catch (Exception e) {
		
			catchExcetion(e, logVo, "instagram comment 정보 저장 실패");
		}
		
		try {
			
			instaservice.addComment(filePath);
			
			logger.info("instagram comment DB 입력 성공");
			
		} catch (ClassNotFoundException e) {
			
			catchExcetion(e, logVo, "instagram comment DB 입력 실패 : Class Not Found Exection");
			
			
		} catch (SQLException e) {
			
			catchExcetion(e, logVo, "instagram comment DB 입력 실패 : Class Not Found Exection");
			
		}
		
		logVo.setStatus(1);
		logVo.setMessage("");
		instaservice.updateLog(logVo);

		
	}
	
	// catch block에 쓸 함수
	public void catchExcetion(Exception e, LogVo logVo, String message) throws Exception {
		e.getStackTrace();
		logger.error(e.getMessage());
		logger.error(message);
		logVo.setMessage(e.getMessage());
		logVo.setStatus(9);
		instaservice.updateLog(logVo);
		catchExcetion(e, logVo, message);
	}

}
