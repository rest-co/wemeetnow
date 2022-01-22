package com.mycompany.myapp.naver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.model.Place;

public class JsonParsing {
	
	public List<Place> getPath(String jsonData) {
		List<Place> list = new ArrayList<Place>();
		
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonOb = (JSONObject) jsonParser.parse(jsonData);
			String result_code = (String) jsonOb.get("code");
			if(!result_code.equals("0")) {
				return null;
			}
			JSONObject route = (JSONObject) jsonOb.get("route");
			JSONArray docArray = (JSONArray)(jsonOb.get("traoptimal"));
			jsonOb = (JSONObject) docArray.get(0);
			JSONArray pathArray = (JSONArray) jsonOb.get("path");
			Place p = null;
			for (int i = 0; i < pathArray.size(); i++) {
				JSONArray arr = (JSONArray) pathArray.get(i);
				p = new Place();
				p.setX(Double.toString((Double) arr.get(0)));
				p.setY(Double.toString((Double) arr.get(1)));
				list.add(p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public JSONObject createGeoJson() {
		JSONObject jsonOb = new JSONObject();
		jsonOb.put("type", "FeatureCollection");
		JSONArray arr = new JSONArray();
		jsonOb.put("features", arr);
		return jsonOb;
	}
	public void addFeature(JSONArray arr, List<Place> list) {
		JSONObject element = new JSONObject();
		element.put("type", "Feature");
		
		JSONObject geometry = new JSONObject();
		geometry.put("type", "LineString");
		JSONArray coordinates = new JSONArray();
		for(Place p : list) {
			coordinates.add(new String[]{p.getX(), p.getY()});
		}
		geometry.put("coordinaates", coordinates);
		
		
		element.put("geometry", geometry);
		
	}
}
