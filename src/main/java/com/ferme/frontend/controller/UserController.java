package com.ferme.frontend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.json.JSONArray;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.portafolio.util.entities.CityEntity;
import com.portafolio.util.entities.LocationEntity;
import com.portafolio.util.entities.RegionEntity;
import com.portafolio.util.entities.UserEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope(value = "session")
@Component(value = "userController")
@ELBeanName(value = "userController")
@Join(path = "/users/new-user", to = "/user/user-form.jsf")
@Data
public class UserController {
	
	private UserEntity user = new UserEntity();
	
	private List<RegionEntity> regions = new ArrayList<>();
	
	private List<CityEntity> cities = new ArrayList<>();
	
	private List<LocationEntity> locations = new ArrayList<>();
	
	private List<SelectItem> regionsItems = new ArrayList<>(), citiesItems = new ArrayList<>(),
			locationsItems = new ArrayList<>();
	
	private Long locationId, cityId, regionId;
	
	@Value("${local.connection.timeout}")
	private Integer connectionTimeout;

	@Value("${local.connection.request.timeout}")
	private Integer requestTimeout;

	@Value("${local.read.timeout}")
	private Integer readTimeout;

	@Value("${local.retry.endpoint}")
	private Integer retryEndPoint;
	
	@Value("${service.url.ferme.regions}")
	private String regionsUrl;
	
	@Value("${service.url.ferme.cities}")
	private String citiesUrl;
	
	@Value("${service.url.ferme.locations}")
	private String locationsUrl;
	
	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {
		JSONArray regionsResponse = RestClientUtil.getJsonArrayFromWs(regionsUrl, null, null, null, buildPropertiesMap());
		Gson gson = new Gson();
		regions = gson.fromJson(regionsResponse.toString(), new TypeToken<List<RegionEntity>>() {}.getType());
		
		regions.stream().forEach(data -> {
			regionsItems.add(new SelectItem(data.getId(), data.getRegionName()));
		});
		
		JSONArray cityResponse = RestClientUtil.getJsonArrayFromWs(citiesUrl, null, null, null, buildPropertiesMap());
		gson = new Gson();
		cities = gson.fromJson(cityResponse.toString(), new TypeToken<List<CityEntity>>() {}.getType());
		
		cities.stream().forEach(data -> {
			citiesItems.add(new SelectItem(data.getId(), data.getCityName()));
		});
		
		JSONArray locationsResponse = RestClientUtil.getJsonArrayFromWs(locationsUrl, null, null, null, buildPropertiesMap());
		gson = new Gson();
		locations = gson.fromJson(locationsResponse.toString(), new TypeToken<List<LocationEntity>>() {}.getType());
		
		locations.stream().forEach(data -> {
			locationsItems.add(new SelectItem(data.getId(), data.getLocatioName()));
		});
		
	}
	
	public void save() {
		user.setEnable(true);
		user.setLocation(locations.stream().filter(data -> data.getId().equals(locationId)).findAny().get());
		System.out.println(user);
	}
	
	private Map<String, Integer> buildPropertiesMap() {
		Map<String, Integer> response = new LinkedHashMap<>();
		response.put("connectionTimeout", connectionTimeout);
		response.put("requestTimeout", requestTimeout);
		response.put("readTimeout", readTimeout);
		response.put("retryEndPoint", retryEndPoint);
		return response;
	}

}
