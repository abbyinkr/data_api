package kr.co.abby.youtube.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import kr.co.abby.youtube.service.YoutubeServiceImpl;
import kr.co.abby.youtube.vo.ChannelVo;
import kr.co.abby.youtube.vo.LinkedChannelVo;
import kr.co.abby.youtube.vo.LogVo;
import kr.co.abby.youtube.vo.PlayListItemsVo;

// -- LinkedChannel 2개로 넣어놔서 비디오 100*2 = 200개씩 수집돼요!

@Controller
public class GatherYoutubeController {

	@Autowired
	YoutubeServiceImpl youtubeService;

	// 로거

	private static Log logger = LogFactory.getLog(GatherYoutubeController.class);

	// db 드라이버 설정
	static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver"; // jdbc 드라이버 주소
	// 인코딩 UTF8MB4 로 지정
	static final String DB_URL = "jdbc:mariadb://localhost:3306/gather_youtube?characterEncoding=UTF8MB4&serverTimezone=UTC&?allowUrlInLocalInfile=true";
	static final String USERNAME = "root"; // DB ID
	static final String PASSWORD = ""; // DB Password

	// 기본 변수들
	static String apiKey = null;
	static String playlistId = null;

	// 재생목록 pk키
	int playlist_seq = 0;

	// channel 수집
	// @Scheduled(cron = "0 0/1 * 1/1 * *") // 1분마다(테스트)
	// @Scheduled(cron = "0 0 0/1 1/1 * *") // 1시간마다(테스트)
	@Scheduled(cron = "0 0 10 1/1 * *") // daily
	@GetMapping("/youtube/channels")
	public void channels() throws Exception {

		// 테이블에 넣을 값 생성
		String id = null;
		String title = null;
		String description = null;
		String publishedAt = null;
		String thumbnailUrl = null;
		String uploads = null;
		String viewCount = null;
		String subscriberCount = null;
		boolean hiddenSubscriberCount = false;
		String videoCount = null;

		// 날짜, 시간
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String hh = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"));
		String datehh = date + hh;
		String regdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));


		// 유튜브 apiKey, 채널아이디 받기
		List<LinkedChannelVo> linkedChannelList = youtubeService.getLinkedChannel();
		List<ChannelVo> channelList = new ArrayList<>();

		
		LogVo logVo = new LogVo();

		// 파일명에 날짜 추가
		String filePath = "D:/devfolder/csv/youtube/channels/channels" + datehh + ".csv";

		BufferedWriter bw = null;

		String newLine = System.lineSeparator(); // 줄바꿈(\n)

		for (LinkedChannelVo linkChannel : linkedChannelList) {

			String channelId = linkChannel.getChannelId();
			apiKey = linkChannel.getApiKey();

			ChannelVo channel = new ChannelVo();

			try {

				// LogVo 세팅
				logVo.setDate(date);
				logVo.setHh(hh);
				logVo.setDatehh(datehh);
				logVo.setRegdate(regdate);
				logVo.setType("channels");
				logVo.setStatus(0);
				logVo.setMessage("");
				logVo.setFileName("channels" + datehh + ".csv");

				// logVo INSERT
				youtubeService.addLog(logVo);

				URL url = new URL("https://www.googleapis.com/youtube/v3/channels?id=" + channelId + "&key=" + apiKey
						+ "&part=id,snippet,statistics,contentDetails&fields=items(id,snippet,statistics,contentDetails)");

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET"); // http 메서드
				conn.setRequestProperty("Content-Type", "application/json"); // header Content-Type 정보
				conn.setRequestProperty("auth", "myAuth"); // header의 auth 정보
				conn.setDoOutput(true);

				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line = null;

				while ((line = br.readLine()) != null) { 
					sb.append(line);
				}
				JSONObject obj = new JSONObject(sb.toString()); 
				// System.out.println(obj);


				JSONObject items_obj = obj.getJSONArray("items").getJSONObject(0);
				JSONObject snippet_obj = obj.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
				JSONObject statistics_obj = obj.getJSONArray("items").getJSONObject(0).getJSONObject("statistics");

				id = items_obj.getString("id");
				title = snippet_obj.getString("title");
				description = snippet_obj.getString("description");

				publishedAt = snippet_obj.getString("publishedAt");
				thumbnailUrl = snippet_obj.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
				uploads = items_obj.getJSONObject("contentDetails").getJSONObject("relatedPlaylists")
						.getString("uploads");

				viewCount = statistics_obj.getString("viewCount");
				subscriberCount = statistics_obj.getString("subscriberCount");
				videoCount = statistics_obj.getString("videoCount");
				hiddenSubscriberCount = statistics_obj.getBoolean("hiddenSubscriberCount");

				// 문자열 처리
				description = handleString(description);

				channel.setDate(date);
				channel.setDatehh(datehh);
				channel.setHh(hh);
				channel.setRegdate(regdate);
				channel.setId(id);
				channel.setTitle(title);
				channel.setDescription(description);
				channel.setPublishedAt(publishedAt);
				channel.setThumbnailUrl(thumbnailUrl);
				channel.setUploads(uploads);
				channel.setViewCount(viewCount);
				channel.setSubscriberCount(subscriberCount);
				channel.setVideoCount(videoCount);
				channel.setHiddenSubscriberCount(hiddenSubscriberCount);

				logger.info("youtube channels 정보 수집 완료");

			} catch (Exception e) {
				catchExcetion(e, logVo, "youtube channels 정보 수집 실패");
			}
			// 채널리스트에 추가
			channelList.add(channel);
		}

		// 수집한 데이터로 csv 파일 만들기

		try {

			File file = new File(filePath);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));


			bw.write("date,hh,datehh,regdate,id,title,description,publishedAt,thumbnailUrl"
					+ ",uploads,viewCount,subscriberCount,hiddenSubscriberCount,videoCount");
			bw.write(newLine);

			for (ChannelVo channel : channelList) {
				bw.write(channel.getDate() + "," + channel.getHh() + "," + channel.getDatehh() + ","
						+ channel.getRegdate() + "," + channel.getId() + ", " + channel.getTitle() + " , "
						+ channel.getDescription() + " , " + channel.getPublishedAt() + " , "
						+ channel.getThumbnailUrl() + "," + channel.getUploads() + "," + channel.getViewCount() + ","
						+ channel.getSubscriberCount() + "," + channel.isHiddenSubscriberCount() + ","
						+ channel.getVideoCount());
				bw.write(newLine);
			}

			logger.info("youtube channels csv 파일 생성 완료");

		} catch (Exception e) {

			catchExcetion(e, logVo, "youtube channels csv 파일 생성 실패");

		} finally { // 닫기
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					catchExcetion(e, logVo, "youtube channels BufferedWriter close 실패");					

				}
			}
		}

		// 만들어진 CSV 파일을 channel 테이블에 입력

		try {

			youtubeService.insertChannel(filePath);

			logger.info("youtube channels DB 입력 완료");

		} catch (Exception e) {
			catchExcetion(e, logVo, "youtube channels DB 입력 실패");
		}

		// 정상적으로 DB입력되었다면 로그 DB 업데이트
		logVo.setStatus(1);
		logVo.setMessage("");
		youtubeService.updateLog(logVo);

	}

	// --------------------------------------------------------------------------------------------------------------------------

	// 비디오 목록 playlistitems
	// @Scheduled(cron = "0 0/1 * 1/1 * *") // 1분마다(테스트)
	// @Scheduled(cron = "0 0 0/1 1/1 * *") // 1시간마다(테스트)
	@Scheduled(cron = "0 0 10 1/1 * *") // daily
	@GetMapping("/youtube/playlistitems")
	public void playlistItems() throws Exception {

		String id = null;
		String publishedAt = null;
		String title = null;
		String description = null;
		String thumbnailUrl = null;
		String channelId = null;
		String playlistId = null;
		int position = 0;
		String kind = null;
		String channelTitle = null;
		String videoId = null;
		String videoPublishedAt = null;
		String nextPageToken = null;

		// 날짜, 시간
		String regdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String hh = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"));
		String datehh = date + hh;

		// csv 파일 설정
		String filePath = "D:/devfolder/csv/youtube/playlistitems/playlistitems" + datehh + ".csv";

		File file = null;

		BufferedWriter bw = null;

		String newLine = System.lineSeparator(); // 줄바꿈(\n)

		// 유튜브 apiKey, 채널아이디 받기
		List<LinkedChannelVo> linkedChannelList = youtubeService.getLinkedChannel();

		LogVo logVo = new LogVo();

		// LogVo 세팅
		logVo.setDate(date);
		logVo.setHh(hh);
		logVo.setDatehh(datehh);
		logVo.setRegdate(regdate);
		logVo.setType("playListItems");
		logVo.setStatus(0);
		logVo.setMessage("");
		logVo.setFileName("playlistitems" + datehh + ".csv");

		// logVo INSERT
		youtubeService.addLog(logVo);

		// csv 파일 컬럼 입력
		try {

			file = new File(filePath);
			
			// 잠깐 수정
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

			bw.write("date,hh,datehh,regdate,id,publishedAt,title,description,thumbnailUrl,position"
					+ ",kind,channelTitle,channelId,videoId,videoPublishedAt");

			bw.write(newLine);

			// 남은데이터 내보내기
			bw.flush();

			// 로그
			logger.info("youtube playlistItems csv 파일 생성 및 컬럼 입력 완료");

		} catch (Exception e) {
			catchExcetion(e, logVo, "youtube playlistItems csv 파일 생성 및 컬럼 입력 실패");

		}

		for (LinkedChannelVo linkChannel : linkedChannelList) {

			channelId = linkChannel.getChannelId();
			apiKey = linkChannel.getApiKey();
			playlistId = linkChannel.getPlaylistId();

			int result_int = 0;

			// while 문으로 수정완료, 100개 수집완료하면 break

			while (true) {
				
				// 반복문 break 조건
				if (result_int == 100)
					break;

				try {

					URL url = new URL("https://www.googleapis.com/youtube/v3/playlistItems?playlistId=" + playlistId
							+ "&key=" + apiKey + "&part=id,snippet,contentDetails&maxResults=50");

					if (nextPageToken != null) {
						url = new URL("https://www.googleapis.com/youtube/v3/playlistItems?playlistId=" + playlistId
								+ "&key=" + apiKey + "&part=id,snippet,contentDetails&maxResults=50" + "&pageToken="
								+ nextPageToken);

					}
					
					//System.out.println("nexPageToken : " + nextPageToken);

					HttpURLConnection conn = (HttpURLConnection) url.openConnection();

					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET"); // http 메서드
					conn.setRequestProperty("Content-Type", "application/json"); // header Content-Type 정보
					conn.setRequestProperty("auth", "myAuth"); // header의 auth 정보
					conn.setDoOutput(true);

					
					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line = null;

					while ((line = br.readLine()) != null) { 
						sb.append(line);
					}

					JSONObject obj = new JSONObject(sb.toString()); 

					if (obj.has("nextPageToken")) {
						
						nextPageToken = obj.getString("nextPageToken");
					} else {
						nextPageToken = null;
					}
					

					// 반복문으로 변수에 값 저장
					for (int k = 0; k < obj.getJSONArray("items").length(); k++) {
						

						// 반복문 break 조건
						if (result_int == 100)
							break;

						JSONObject snippet = obj.getJSONArray("items").getJSONObject(k).getJSONObject("snippet");

						id = obj.getJSONArray("items").getJSONObject(k).getString("id");
						publishedAt = snippet.getString("publishedAt");
						title = snippet.getString("title");
						description = snippet.getString("description");

						// 문자열 \n 과 쉼표 치환
						title = handleString(title);
						description = handleString(description);
						
						// System.out.println(i + "번째 : title : " + title + "설명 : " + description );

						thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
						position = snippet.getInt("position");
						kind = obj.getJSONArray("items").getJSONObject(k).getString("kind");
						channelTitle = snippet.getString("videoOwnerChannelTitle");
						channelId = snippet.getString("videoOwnerChannelId");
						videoId = obj.getJSONArray("items").getJSONObject(k).getJSONObject("contentDetails")
								.getString("videoId");
						videoPublishedAt = obj.getJSONArray("items").getJSONObject(k).getJSONObject("contentDetails")
								.getString("videoPublishedAt");


						bw.write(date + "," + hh + "," + datehh + "," + regdate + "," + id + "," + publishedAt + " , "
								+ title + " , " + description + " , " + thumbnailUrl + " , " + position + " ," + kind
								+ "," + channelTitle + "," + channelId + "," + videoId + "," + videoPublishedAt);

						bw.write(newLine);

						bw.flush();

						//System.out.println("resultInt 반복 : " + result_int);
						result_int++;
						
						

					} // end for 문

				} catch (Exception e) {
					catchExcetion(e, logVo, "youtube playlistItems 정보 수집 및 csv 파일 입력 실패");

				}

			} // end for문
		} // end 채널리스트 for 문
		logger.info("youtube playlistItems 정보 수집 및 csv 파일 입력 완료");

		if (bw != null) {
			try {
				bw.flush();
				bw.close();
			} catch (IOException e) {
				catchExcetion(e, logVo, "youtube playlistItems BufferedWriter close 실패");

			}

			try {

				youtubeService.insertPlayListItems(filePath);

				logger.info("youtube playlistItems DB 입력 완료");

			} catch (Exception e) {
				catchExcetion(e, logVo, "youtube playlistItems DB 입력 실패");

			}

			logVo.setStatus(1);
			logVo.setMessage("");
			youtubeService.updateLog(logVo);
			
			// 피드백 반영 전
			// 정상적으로 업데이트 되었을시 메소드 호출
			/*
			if (logVo.getStatus() == 1 ) {

				// videos 메소드 호출
				videos();

				// comments 메소드 호출
				comments();

			}
			*/
			
			// 함수 호출 수정
			LogVo check_logVo = youtubeService.getLog(logVo.getSeq());
			
			//System.out.println("playListItems seq : " + logVo.getSeq());
			//System.out.println("playListItems check_logVo.getStatus() : " + check_logVo.getStatus());
			
			// 실행 X 조건, 이미 수집을 완료했거나 데이터 수집이 안됐을 경우
			
			if (check_logVo.getDatehh() == datehh || check_logVo.getStatus() != 1) {
				
			} else {
				
				videos();
			}
			
			
		}

	}

	// --------------------------------------------------------------------------------------------------------------------------
	// videos

	// playListItems 먼저 실행되고 후에 실행되어야 함
	// @Scheduled(cron = "0 0 0/1 1/1 * *") // 1시간마다(테스트)
	// @Scheduled(cron = "0 0 0/3 1/1 * *") // daily
	@GetMapping("/youtube/videos")
	public void videos() throws Exception {

		int playlistSeq = 0;
		String id = null;
		String categoryId = null;
		String liveBroadcastContent = null;
		String viewCount = null;
		String likeCount = null;
		String favoriteCount = null;
		String commentCount = null;

		// 날짜, 시간
		String regdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String hh = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"));
		String datehh = date + hh;
		LogVo logVo = new LogVo();

		// 수집한 데이터로 csv 파일 만들기
		String filePath = "D:/devfolder/csv/youtube/videos/videos" + datehh + ".csv";

		File file = null;

		BufferedWriter bw = null;

		String newLine = System.lineSeparator();

		// db로 list<playlistItemsVo>를 한꺼번에 가져온다
		List<PlayListItemsVo> playListItemsList = youtubeService.getVideoListSeq(datehh);

		// 먼저 실행된 playListItem메소드에서 저장된 데이터가 없을 경우
		if (playListItemsList.size() == 0) {

			logger.error("같은 일시에 저장된 재생목록 데이터 없음, video 데이터 수집 중지");
			throw new Exception();
		}

		// videoId 리스트 생성
		ArrayList<String> videoIdList = new ArrayList<String>();

		try {

			// LogVo 세팅
			logVo.setDate(date);
			logVo.setHh(hh);
			logVo.setDatehh(datehh);
			logVo.setRegdate(regdate);
			logVo.setType("video");
			logVo.setStatus(0);
			logVo.setMessage("");
			logVo.setFileName("videos" + datehh + ".csv");

			// log insert
			youtubeService.addLog(logVo);

			// 값 확인

			for (int i = 0; i < playListItemsList.size(); i++) {
				videoIdList.add(playListItemsList.get(i).getVideoId());
			}

			logger.info("youtube videos videoId 수 : " + videoIdList.size());
			logger.info("youtube videos DB조회후 videoId 가져오기 완료");

		} catch (Exception e) {
			catchExcetion(e, logVo, "youtube videos DB조회후 videoId 가져오기 실패");
		}

		// csv 파일 컬럼 입력
		try {

			file = new File(filePath);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

			bw.write("date,hh,datehh,regdate,id,playlistSeq,categoryId,liveBroadcastContent,viewCount,likeCount,favoriteCount,commentCount");

			bw.write(newLine);

			// 남은데이터 내보내기
			bw.flush();

			// 로그
			logger.info("youtube videos csv 파일 생성 및 컬럼 입력 완료");

		} catch (Exception e) {
			catchExcetion(e, logVo, "youtube videos csv 파일 생성 및 컬럼 입력 실패");
		}

		// 비디오 아이디 리스트 생성
		int maxResult = 50;
		String videoIds = "";
		ArrayList<String> videoIdsList = new ArrayList<String>();

		for (int i = 1; i <= playListItemsList.size(); i++) {

			// 50개씩 videoIds 에 추가한다
			videoIds += playListItemsList.get(i - 1).getVideoId() + ",";

			// i가 50의 배수가 되면 리스트에 담고 초기화
			if (i % maxResult == 0) {

				// System.out.println(i + " / " + i % maxResult);
				// 마지막 쉼표 제거
				videoIds = videoIds.substring(0, videoIds.length() - 1);
				// 50개가 되면 ArrayList에 추가한다.
				videoIdsList.add(videoIds);
				// videoIds를 초기화 시키고 다시 50개 저장한다
				videoIds = "";
			}

		}

		try {


			// videoId 50개씩 한번에 비디오 api 호출로 수정
			for (int i = 0; i < videoIdsList.size(); i++) {


				URL url = new URL("https://www.googleapis.com/youtube/v3/videos?id=" + videoIdsList.get(i) + "&key="
						+ apiKey + "&part=id,snippet,statistics");

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET"); // http 메서드
				conn.setRequestProperty("Content-Type", "application/json"); // header Content-Type 정보
				conn.setRequestProperty("auth", "myAuth"); // header의 auth 정보
				conn.setDoOutput(true);

				
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line = null;

				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

				JSONObject obj = new JSONObject(sb.toString()); 
				// System.out.println(obj);

				// 재생목록seq리스트에 데이터가 있을 때
				if (playListItemsList.size() != 0) {

					// System.out.println("playListItemsList.size() :" + playListItemsList.size());
					// // 100
					// sqlList 데이터 조회
					for (int k = 0; k < maxResult; k++) {

						// playlist_seq 는 videoID로 데려온다
						String videoId = obj.getJSONArray("items").getJSONObject(k).getString("id");
						// System.out.println(videoId);

						// videoId 가 일치하면 playlist_seq 에 값 대입
						if (videoId.equals(playListItemsList.get(k).getVideoId())) {

							playlistSeq = playListItemsList.get(k).getSeq();

							// System.out.println("설정한 playlisSeq : " + playlistSeq);
						}

						JSONObject snippet_obj = obj.getJSONArray("items").getJSONObject(k).getJSONObject("snippet");
						JSONObject statistics_obj = obj.getJSONArray("items").getJSONObject(k)
								.getJSONObject("statistics");

						categoryId = snippet_obj.getString("categoryId");
						liveBroadcastContent = snippet_obj.getString("liveBroadcastContent");
						viewCount = statistics_obj.getString("viewCount");
						likeCount = statistics_obj.getString("likeCount");
						favoriteCount = statistics_obj.getString("favoriteCount");
						commentCount = statistics_obj.getString("commentCount");

						bw.write(date + "," + hh + "," + datehh + "," + regdate + "," + videoId + "," + playlistSeq
								+ "," + categoryId + "," + liveBroadcastContent + "," + viewCount + "," + likeCount
								+ "," + favoriteCount + "," + commentCount);

						bw.write(newLine);

						bw.flush();

					}

				}

			}

			
			logger.info("youtube videos 정보 수집 완료");

		} catch (Exception e) {
			
			catchExcetion(e, logVo, "youtube videos 정보 수집 실패");
			
		} finally {
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					catchExcetion(e, logVo, "youtube videos BufferedWriter close 실패");

				}
			}
		}
		try {

			youtubeService.insertVideos(filePath);

			logger.info("youtube videos DB 입력 완료");

		} catch (Exception e) {
			catchExcetion(e, logVo, "youtube videos DB 입력 실패");

		}

		// 로그 DB 업데이트
		logVo.setStatus(1);
		logVo.setMessage("");
		youtubeService.updateLog(logVo);
		
		LogVo check_logVo = youtubeService.getLog(logVo.getSeq());
		LogVo check_PlayList_logVo = youtubeService.getLog(logVo.getSeq()-1);
		
		//System.out.println("videos seq : " + logVo.getSeq());
		//System.out.println("videos check_logVo.getStatus() : " + check_logVo.getStatus());
		//System.out.println("videos check_PlayList_logVo.getSeq() : " + check_PlayList_logVo.getSeq());
		//System.out.println("videos check_PlayList_logVo.getStatus() : " + check_PlayList_logVo.getStatus());
		
		
		// 실행 X 조건, 이미 수집을 완료했거나 상위 데이터 수집이 안됐을 경우
		if (check_logVo.getDatehh() == datehh || check_PlayList_logVo.getStatus() != 1) {
			
			logger.error("videos : 이미 금일 수집을 완료했거나 playlistItems 데이터 수집이 되지않았습니다.");
			
		} else {
			
			comments();
		}
		

	}

	// --------------------------------------------------------------------------------------------------------------------------
	// comments // playListItems 먼저 실행되고 후에 실행되어야 함
	// @Scheduled(cron = "0 0 0/3 1/1 * *") // daily
	// @Scheduled(cron = "0 0 0/1 1/1 * *") // 1시간마다(테스트)
	@GetMapping("/youtube/comments")
	public void comments() throws Exception {

		String id = null;
		String videoId = null;
		String textOriginal = null;
		String authorDisplayName = null;
		String authorProfileImageUrl = null;
		String authorChannelUrl = null;
		String authorChannelId = null;
		boolean canRate = false;
		String viewerRating = null;
		int likeCount = 0;
		String publishedAt = null;
		String updatedAt = null;
		boolean canReply = false;
		int totalReplyCount = 0;
		boolean isPublic = false;

		// 날짜, 시간
		String regdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String hh = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"));
		String datehh = date + hh;

		// csv 파일 설정
		String filePath = "D:/devfolder/csv/youtube/comments/comments" + datehh + ".csv";

		File file = null;

		BufferedWriter bw = null;

		String newLine = System.lineSeparator(); // 줄바꿈(\n)

		// 유튜브 apiKey, 채널아이디 받기
		List<LinkedChannelVo> result = youtubeService.getLinkedChannel();

		apiKey = result.get(0).getApiKey();

		LogVo logVo = new LogVo();

		List<PlayListItemsVo> PlayListItemsList = youtubeService.getVideoListSeq(datehh);
		logger.info("youtube comments seqList.size() : " + PlayListItemsList.size());

		// 먼저 실행된 playListItem메소드에서 저장된 데이터가 없을 경우
		if (PlayListItemsList.size() == 0) {

			logger.error("같은 일시에 저장된 재생목록 데이터 없음, video 데이터 수집 중지");
			throw new Exception();
		}

		ArrayList<String> videoIdList = new ArrayList<String>();

		try {

			// LogVo 세팅
			logVo.setDate(date);
			logVo.setHh(hh);
			logVo.setDatehh(datehh);
			logVo.setRegdate(regdate);
			logVo.setType("comment");
			logVo.setStatus(0);
			logVo.setMessage("");
			logVo.setFileName("comments" + datehh + ".csv");

			youtubeService.addLog(logVo);

			// 반복문으로 videoId를 list 에 담는다.
			for (int i = 0; i < PlayListItemsList.size(); i++) {

				// videoId ArrayList 에 담기
				videoIdList.add(PlayListItemsList.get(i).getVideoId());

			} // end for문(비디오 아이디)

			
			logger.info("youtube comments DB 조회후 videoId 가져오기 완료");

		} catch (Exception e) {
			
			catchExcetion(e, logVo, "youtube comments DB 조회후 videoId 가져오기 실패");
			
		}

		// csv 파일 컬럼 입력
		try {

			file = new File(filePath);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));

			bw.write(
					"date,hh,datehh,regdate,id,videoId,textOriginal,authorDisplayName,authorProfileImageUrl"
					+ ",authorChannelUrl,authorChannelId,canRate,viewerRating"
					+ ",likeCount,publishedAt,updatedAt,canReply,totalReplyCount,isPublic");

			bw.write(newLine);

			bw.flush();

			// 로그
			logger.info("youtube comments csv 파일 생성 및 컬럼 입력 완료");

		} catch (Exception e) {
			
			catchExcetion(e, logVo, "youtube comments csv 파일 생성 및 컬럼 입력 실패");

		}

		// 비디오 1개 당 댓글데이터 가져오기

		// 누적데이터 수 확인
		int sum = 0;

		try {

			for (int i = 0; i < PlayListItemsList.size(); i++) {

				URL url = new URL("https://www.googleapis.com/youtube/v3/commentThreads?part=id,snippet&videoId="
						+ PlayListItemsList.get(i).getVideoId() + "&key=" + apiKey + "&maxResults=100");
				

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET"); // http 메서드
				conn.setRequestProperty("Content-Type", "application/json"); // header Content-Type 정보
				conn.setRequestProperty("auth", "myAuth"); // header의 auth 정보
				conn.setDoOutput(true);

				
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line = null;

				while ((line = br.readLine()) != null) { 
					sb.append(line);
				}

				JSONObject comments_obj = new JSONObject(sb.toString()); 
				JSONArray items_obj = comments_obj.getJSONArray("items");

				// 반복문 2
				for (int j = 0; j < items_obj.length(); j++) {

					sum += 1;
					JSONObject snippet1_obj = comments_obj.getJSONArray("items").getJSONObject(j)
							.getJSONObject("snippet");
					JSONObject snippet2_obj = comments_obj.getJSONArray("items").getJSONObject(j)
							.getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet");

					id = items_obj.getJSONObject(j).getString("id");
					videoId = videoIdList.get(i);
					textOriginal = snippet2_obj.getString("textOriginal");
					authorDisplayName = snippet2_obj.getString("authorDisplayName");
					authorProfileImageUrl = snippet2_obj.getString("authorProfileImageUrl");
					authorChannelUrl = snippet2_obj.getString("authorChannelUrl");
					
					
					// 채널 아이디 없을때 처리 
					if ( !snippet2_obj.has("authorChannelId") ) 
						authorChannelId = "";
					else 
						authorChannelId = snippet2_obj.getJSONObject("authorChannelId").getString("value");
					
					canRate = snippet2_obj.getBoolean("canRate");
					viewerRating = snippet2_obj.getString("viewerRating");
					likeCount = snippet2_obj.getInt("likeCount");
					publishedAt = snippet2_obj.getString("publishedAt");
					updatedAt = snippet2_obj.getString("updatedAt");
					canReply = snippet1_obj.getBoolean("canReply");
					totalReplyCount = snippet1_obj.getInt("totalReplyCount");
					isPublic = snippet1_obj.getBoolean("isPublic");

					// 문자열 처리
					authorChannelId = handleString(authorChannelId);
					textOriginal = handleString(textOriginal);
					authorDisplayName = handleString(authorDisplayName);
					//System.out.println(textOriginal);

					bw.write(date + "," + hh + "," + datehh + "," + regdate + "," + id + "," + videoId + ","
							+ textOriginal + " , " + authorDisplayName + "," + authorProfileImageUrl + ", "
							+ authorChannelUrl + ", " + authorChannelId + "," + canRate + "," + viewerRating + ","
							+ likeCount + "," + publishedAt + "," + updatedAt + "," + canReply + "," + totalReplyCount
							+ "," + isPublic);

					bw.write(newLine);

					bw.flush();

				} // 비디오당 댓글 for 문

			} // end for 문

			
			logger.info("youtube comments 정보 수집 및 csv 파일 입력 완료");
			logger.info("youtube comments 총 데이터 수 : " + sum);

			// System.out.println("총 데이터 수 : " + sum);
			// 4985개 확인완료

		} catch (Exception e) {
			catchExcetion(e, logVo, "youtube comments 정보 수집 및 csv 파일 입력 실패");

		} finally {
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					catchExcetion(e, logVo, "youtube comments BufferedWriter close 실패");

				}
			}
		}


		try {

			youtubeService.insertComments(filePath);

			logger.info("youtube comments DB 입력 완료");

		} catch (Exception e) {
			catchExcetion(e, logVo, "youtube comments DB 입력 실패");

		}

		logVo.setStatus(1);
		logVo.setMessage("");
		youtubeService.updateLog(logVo);

	}// end 코멘트 메소드

	
	// catch block에 쓸 함수
	public void catchExcetion(Exception e, LogVo logVo, String message) throws Exception {
		e.getStackTrace();
		logger.error(e.getMessage());
		logger.error(message);
		logVo.setMessage(e.getMessage());
		logVo.setStatus(9);
		youtubeService.updateLog(logVo);
		throw e;
	}
	
	
	// 문자열 처리 함수
	public String handleString(String str) throws Exception {

		str = str.replaceAll("\n", "");
		str = str.replaceAll(",", "");
		str = str.replaceAll("_", "");
		str = str.replaceAll("\r", "");
		str = str.replaceAll("\r\n", "");
		str = str.replaceAll("<br>", "");
		
		return str;
	}
}
