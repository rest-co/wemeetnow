package com.mycompany.myapp.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mycompany.myapp.json.JsonParsing;
import com.mycompany.myapp.model.Place;
import com.mycompany.myapp.model.RouteM;
import com.mycompany.myapp.model.RouteS;
import com.mycompany.myapp.naver.DrivingAPI;
import com.mycompany.myapp.service.MapService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeAction {
	
	@Autowired
	private MapService ms;
	@Autowired
	private JsonParsing jsonparser;
	@RequestMapping("test.do")
	public String home_push(HttpSession session, HttpServletRequest request) {
		return "map/home";
	}
//	@RequestMapping("navermap.do")
//	public String test(HttpSession session, Place endplace, Model model) throws JsonProcessingException {
////		//광화문 좌표 
//		//37.575971, 126.976781
//		endplace.setX("126.976781");
//		endplace.setY("37.575971");
//		List<Place> startPlaceList = (List<Place>)session.getAttribute("startPlaceList");
//		List<double[]>[] pathArr= ms.getPathArr(startPlaceList, endplace);
//		
//		JSONObject jsonObject = jsonparser.createGeoJson();
//		JSONArray arr = (JSONArray) jsonObject.get("features");
//		for(List<double[]> list : pathArr) {
//			jsonparser.addFeature(arr, list, "red");
//		}
//		model.addAttribute("centerPath", jsonObject.toString());
//		
//		
//		return "map/navermap";
//	}
	
	@RequestMapping("geoJson.do")
	@ResponseBody
	public String geoJson(HttpSession session, Place endplace, Model model) throws InterruptedException, ExecutionException {
		List<Place> startPlaceList = (List<Place>)session.getAttribute("startPlaceList");
		long stime = System.currentTimeMillis();
		JSONArray[] pathArr= ms.getPathArr(startPlaceList, endplace);
		long etime = System.currentTimeMillis();
		System.out.println("Naver api 소요시간 : "+ (etime -stime));
		
		JSONObject jsonObject = jsonparser.createGeoJson();
		JSONArray arr = (JSONArray) jsonObject.get("features");
		for(JSONArray path : pathArr) {
			jsonparser.addFeature(arr, path, "red");
		}
		return jsonObject.toString();
	}
	
	@RequestMapping("session_del.do")
	public String testtest(HttpServletRequest request, Place place, Model model) {
		if(place.getName()==null)return "map/home";
		
		ArrayList<Place> startPlaceList = new ArrayList<Place>();
		int n = place.getName().split(",").length;
		for(int i=0; i<n; i++) {
			Place p = new Place();
			p.setAddress(place.getAddress().split(",")[i]);
			p.setName(place.getName().split(",")[i]);
			p.setX(place.getX().split(",")[i]);
			p.setY(place.getY().split(",")[i]);
			startPlaceList.add(p);
		}
		request.getSession().setAttribute("startPlaceList", startPlaceList);
		
		return "map/home";
	}
	
	@RequestMapping("sendAddr2.do")
	public String sendAddr2(HttpServletRequest request, @ModelAttribute Place place, Model model) throws Exception {
		
		
		ArrayList<Place> startPlaceList = (ArrayList<Place>) place.getPlaces();
		// 세션
		HttpSession session = request.getSession();
		session.setAttribute("startPlaceList", startPlaceList);		
		
		//---------------------------------중점 좌표 get--------------------------------
		Place center = ms.getCenter(startPlaceList);		
		
		// geoJson 생성 // 경로 그리기
		long stime = System.currentTimeMillis();
		JSONArray[] pathArr= ms.getPathArr(startPlaceList, center);
		long etime = System.currentTimeMillis();
		System.out.println("Naver api 소요시간 : "+ (etime -stime));
		
		JSONObject jsonObject = jsonparser.createGeoJson();
		JSONArray arr = (JSONArray) jsonObject.get("features");
		for(JSONArray list : pathArr) {
			jsonparser.addFeature(arr, list, "red");
		}
		model.addAttribute("centerPath", jsonObject.toString());
		
		
		//--------------------------------가까운 지하철역 5개 get-------------------------------
		// category_group_code:SW8(지하철), page:1, size:15(기본값), radius:2000 으로 제한하여 요청
		String option = "x/" + center.getX() + "/y/" + center.getY() + "/page/1/radius/2000";
		List<Place> endplaceList = ms.categorySearch("SW8", option);
		
		endplaceList.add(0,	center);//중심 좌표 추가.	
		//tmap api 호출을 위해서  출발,도착지 정보를 js에서 사용해야함
		// js에서 사용하기 편하게  json형식으로 변환.
		String jsonEpl = jsonparser.josonParsing(endplaceList); // 추천지역 json 변환 08.29
		String jsonSpl = jsonparser.josonParsing(startPlaceList); // 출발지역 json 변환08.29
		model.addAttribute("jsonEpl", jsonEpl);
		model.addAttribute("jsonSpl", jsonSpl);
		

		
		return "map/foundplace2";
	}
//	@RequestMapping("sendAddr.do")
//	public String sendAddr(HttpServletRequest request, @ModelAttribute Place place, Model model) throws Exception {
//		
//		
//		ArrayList<Place> startPlaceList = (ArrayList<Place>) place.getPlaces();
//		// 세션
//		HttpSession session = request.getSession();
//		session.setAttribute("startPlaceList", startPlaceList);		
//		
//		//---------------------------------중점 좌표 get--------------------------------
//		Place center = ms.getCenter(startPlaceList);		
//		
//		//--------------------------------가까운 지하철역 5개 get-------------------------------
//		// category_group_code:SW8(지하철), page:1, size:15(기본값), radius:2000 으로 제한하여 요청
//		String option = "x/" + center.getX() + "/y/" + center.getY() + "/page/1/radius/2000";
//		List<Place> endplaceList = ms.categorySearch("SW8", option);
//		
//		endplaceList.add(0,	center);//중심 좌표 추가.	
//		//tmap api 호출을 위해서  출발,도착지 정보를 js에서 사용해야함
//		// js에서 사용하기 편하게  json형식으로 변환.
//		String jsonEpl = jsonparser.josonParsing(endplaceList); // 추천지역 json 변환 08.29
//		String jsonSpl = jsonparser.josonParsing(startPlaceList); // 출발지역 json 변환08.29
//		model.addAttribute("jsonEpl", jsonEpl);
//		model.addAttribute("jsonSpl", jsonSpl);
//		
//		// 각 후보지에 대해서 소요시간 보여주기.		
//		// 공공데이터포털에서 경로 찾아오기
//		// 필요한 정보 : 출발지 정보, 도착 후보지 정보
//		List<String> jsonPath = new ArrayList<String>();
//		endplaceList.remove(0);
//		for(int i=0; i<endplaceList.size(); i++) {
//			List<RouteS> list = ms.test(startPlaceList, endplaceList.get(i));
//			jsonPath.add(jsonparser.josonParsing(list));
//		}
//		model.addAttribute("path", jsonPath);
//		
//		return "map/foundplace";
//	}
	@RequestMapping("category.do")
	public String categorySelect(Place place, Model model, HttpSession session) {

		if(session.getAttribute("startPlaceList")==null) {
			return "member/sessionResult";
		}
		model.addAttribute("place", place);

		//-------------------- 카테고리별 추천 장소 5개 ----- 08/05 김가을  --------------------
		String option = "x/" + place.getX() + "/y/" + place.getY() + "/page/1/size/5/radius/2000";
		
		StringBuilder sb = new StringBuilder();
		List<Place> startPlaceList = (List<Place>)session.getAttribute("startPlaceList");
		for(Place p : startPlaceList)
			sb.append(p.getAddress());
		// CT1 문화시설
		List<Place> ct1placeList = ms.categorySearch("CT1", option);
		ms.createId(ct1placeList, sb.toString());
		model.addAttribute("ct1placeList", ct1placeList);
		// FD6 음식점
		List<Place> fd6placeList = ms.categorySearch("FD6", option);
		ms.createId(fd6placeList, sb.toString());
		model.addAttribute("fd6placeList", fd6placeList);
		// CE7 카페
		List<Place> ce7placeList = ms.categorySearch("CE7", option);
		ms.createId(ce7placeList, sb.toString());
		model.addAttribute("ce7placeList", ce7placeList);
		// AT4 관광명소
		List<Place> at4placeList = ms.categorySearch("AT4", option);
		ms.createId(at4placeList, sb.toString());
		model.addAttribute("at4placeList", at4placeList);
		//------------------------------------------------------------
		
		return "map/category";
	}
	@RequestMapping("route.do")
	public String route(String status, HttpSession session, Place place, RouteM rm, Model model) throws JsonProcessingException {
		//지하철,버스,
		//경로 db에 저장된 정보 있는지 체크.
		RouteM r = ms.routeSearch(rm.getId());
		
		//비성장 접근 체크
		if(status==null && r == null) {
			return "map/routeResult";	
		}

		if(r == null ) {
			//session.setAttribute("id", id);
			Place endPlace = place;
			List<Place> startPlaceList =(List<Place>) session.getAttribute("startPlaceList");		
			ms.finalDBSetting(startPlaceList,endPlace,rm);
			r = ms.routeSearch(rm.getId());
			
		}
		List<RouteM> routeList = ms.getRouteList(r);
		model.addAttribute("routelist", routeList);
		model.addAttribute("endPlace",r);
		model.addAttribute("end",jsonparser.josonParsing(r));

		return "map/route";
	}
	
}
// 
