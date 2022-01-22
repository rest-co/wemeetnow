package com.mycompany.myapp.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mycompany.myapp.dao.MapDAO;
import com.mycompany.myapp.json.JsonParsing;
import com.mycompany.myapp.model.Place;
import com.mycompany.myapp.model.RouteM;
import com.mycompany.myapp.model.RouteS;
import com.mycompany.myapp.naver.DrivingAPI;

@Service
public class MapServiceImpl implements MapService {

	@Autowired
	private MapDAO md;
	@Autowired
	private JsonParsing par;
	@Autowired
	private PublicDataService pds;
	@Autowired
	private DrivingAPI driving;

	private final String URL_HOME = "https://dapi.kakao.com";
	private final String URL_CATEGORY = "/v2/local/search/category.json";
	private final String URL_KEYWORD = "/v2/local/search/keyword.json";
	private final String URL_ADRESS = "/v2/local/search/address.json";

	@Override
	public List<Place> categorySearch(String categoryCode) {
		String url = URL_HOME + URL_CATEGORY + "?";
		String options = "category_group_code/" + categoryCode;
		return getStationCoord(url, options);
	}

	@Override
	public List<Place> categorySearch(String categoryCode, String option) {
		String url = URL_HOME + URL_CATEGORY + "?";
		String options = "category_group_code/" + categoryCode + "/" + option;
		return getStationCoord(url, options);
	}

	@Override
	public List<Place> keywordSearch(String query) {
		String url = URL_HOME + URL_KEYWORD + "?";
		String options = null;
		try {
			options = "query/" + URLEncoder.encode(query, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return getStationCoord(url, options);
	}

	@Override
	public List<Place> keywordSearch(String query, String option) {
		String url = URL_HOME + URL_KEYWORD + "?";
		String options = null;
		try {
			options = "query/" + URLEncoder.encode(query, "utf-8") + "/" + option;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return getStationCoord(url, options);
	}

	@Override
	public List<Place> addressSearch(String address) {
		String url = URL_HOME + URL_ADRESS + "?";
		String options = null;
		try {
			options = "query/" + URLEncoder.encode(address, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return getStationCoord(url, options);
	}

	@Override
	public List<Place> addressSearch(String address, String option) {
		String url = URL_HOME + URL_ADRESS + "?";
		String options = null;
		try {
			options = "query/" + URLEncoder.encode(address, "utf-8") + "/" + option;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return getStationCoord(url, options);
	}

	// REST API에 요청해서 json 형식 데이터 받아올 부분
	@Override
	public List<Place> getStationCoord(String url_, String options) {
		HttpURLConnection conn = null;
		StringBuilder sb = null;
		try {
			sb = new StringBuilder();
			sb.append(url_);
			StringTokenizer st = new StringTokenizer(options, "/");
			String Authorization = "KakaoAK " + "cdca325d6efe88cfb6c48440908a80c2";
			while (st.hasMoreTokens()) {
				sb.append(st.nextToken()).append("=").append(st.nextToken()).append("&");
			}

			// 주소 확인용 디버깅 코드
			String final_request_url = sb.toString();
			URL url = new URL(final_request_url);

			conn = (HttpURLConnection) url.openConnection();
			// Request 형식 설정
			conn.setRequestMethod("GET");
			// 키 입력
			conn.setRequestProperty("Authorization", Authorization);

			// 보내고 결과값 받기
			// 통신 상태 확인 코드.
			int responseCode = conn.getResponseCode();
			if (responseCode == 400) {
				System.out.println("kakao connection :: 400 error");
			} else if (responseCode == 401) {
				System.out.println("401:: Wrong X-Auth-Token Header");
			} else if (responseCode == 500) {
				System.out.println("500:: kakao server error");
			} else { // 성공 후 응답 JSON 데이터받기
				sb = new StringBuilder();
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

				String line = "";
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		par = new JsonParsing();
		return par.getPlaceInfo(sb.toString());
	}

	// 중심 좌표 구하기.
	// 출발지 좌표 값의 평균.
	@Override
	public Place getCenter(List<Place> placeList) {
		Place p = new Place();
		float n = placeList.size();
		float sumX = 0;
		float sumY = 0;
		for (int i = 0; i < n; i++) {
			sumX += Float.parseFloat(placeList.get(i).getX());
			sumY += Float.parseFloat(placeList.get(i).getY());
		}
		p.setX(Float.toString(sumX / n));
		p.setY(Float.toString(sumY / n));
		p.setName("중심");
		return p;
	}

	
	//공공데이터포털에서 경로와 시간 구해오는 메소드
	//리턴 형식은 '/'와 '#' 를 구분자로하는 문자열 타입
	//각 경로는 '/'로 구분
	//하나의 경로에서 시간 거리 출발지 도착지 등 정보는 '#'로 구분

	public void getFinalPath(RouteS rs,Place startPlace, Place endPlace, String transport) {
		StringBuilder sb = new StringBuilder();
		//PublicDataService pds = new PublicDataService();				
		pds.getPath(startPlace, endPlace, transport, rs);
		
	}
	
	@Override
	public List<RouteS> test(List<Place> startList, Place end) {
		List<RouteS> list = new ArrayList<RouteS>();
		for(Place p : startList) {
			RouteS rs = new RouteS();
			rs.setDeparture(p.getAddress());
			pds.getPath(p, end, "Bus", rs);
			pds.getPath(p, end, "BusNSub", rs);
			list.add(rs);
		}
		return list;
	}
	
	//마지막 페이지에 필요한 정보(출발지, 경로1, 경로2, 시간1, 시간2)
	@Override
	public int finalDBSetting(List<Place> startPlaceList, Place endPlace, RouteM rm){		
		
		rm.setNum(startPlaceList.size());
		md.insertRouteM(rm);
		List<RouteS> list = test(startPlaceList, endPlace);
		for(RouteS rs : list) {
			rs.setId(rm.getId());
			md.insertRouteS(rs);
		}
		return 0;
	}
	@Override
	public RouteM routeSearch(String id) {
		
		RouteM rm = md.routeSearch(id);
		
		return rm;
	}
	public void createId(List<Place> list, String spl) {		
		Collections.sort(list, new Comparator<Place>() {
			public int compare(Place o1, Place o2) {
				return o1.getAddress().compareTo(o2.getAddress());
			};
		});
		for(Place p : list) {
			StringBuilder sb = new StringBuilder();
			sb.append(spl);
			sb.append(p.getPlace_url());
			int id = sb.toString().hashCode();
			p.setId(id+"");
			
		}
	}
	@Override
	public List<RouteM> getRouteList(RouteM r) {
		return md.getRouteList(r);
	}
	
	@Override
	public JSONArray[] getPathArr(List<Place> startPlaceList, Place endplace) throws InterruptedException, ExecutionException {
//		String start ="126.865572139231,37.5507280806214";
//		String start2 = "126.986400086711,37.5609634671841";
//		String goal = "126.996969239236,37.6107638961532";
//		String path = driving.getPath(start, goal);
//		String path2 = driving.getPath(start2, goal);
//		List<double[]> list = jsonparser.getPath(path);
//		List<double[]> list2 = jsonparser.getPath(path2);
		
		JSONArray[] arr = new JSONArray[startPlaceList.size()];
		String goal = new StringBuilder()
				.append(endplace.getX())
				.append(",")
				.append(endplace.getY())
				.toString();
		int idx=0;
		ExecutorService executor = Executors.newFixedThreadPool(5);
		List<Future<String>> futures = new ArrayList<Future<String>>();
		for( Place p : startPlaceList) {
			String start = new StringBuilder()
					.append(p.getX())
					.append(",")
					.append(p.getY())
					.toString();
			futures.add(executor.submit(()->{
				return driving.getPath(start, goal);
			}));
			
//			arr[idx++]= par.getPath(driving.getPath(start, goal));
//			System.out.println(arr[idx].size());
		}
		executor.shutdown();
		System.out.println(executor.awaitTermination(3000,TimeUnit.MILLISECONDS));
		for(int i=0; i<futures.size(); i++) {
			arr[i]=par.getPath(futures.get(i).get());
		}
		return arr;
	}
}

