package com.auth0.samples.bootfaces.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.portafolio.util.Entities.UserEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope(value = "session")
@Component(value = "loginController")
@ELBeanName(value = "loginController")
@Join(path = "/", to = "/login/login.jsf")
@Data
public class LoginController {
	
	private Logger LOG = LoggerFactory.getLogger(LoginController.class);
	
	private UserEntity user = new UserEntity();
	
	@Value("${local.connection.timeout}")
	private Integer connectionTimeout;

	@Value("${local.connection.request.timeout}")
	private Integer requestTimeout;
	
	@Value("${local.read.timeout}")
	private Integer readTimeout;
	
	@Value("${local.retry.endpoint}")
	private Integer retryEndPoint;
	
	@Value("${service.url.ferme.users}")
	private String usersUrl;
	
	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {
	}
	
	public void save() {
		
		JSONArray json = RestClientUtil.getJsonArrayFromWs(usersUrl, null, null, null, buildPropertiesMap());
		
		List<UserEntity> list = new ArrayList<>();
		
		Gson gson = new Gson();
		list = gson.fromJson(json.toString(), new TypeToken<List<UserEntity>>(){}.getType());
		
		LOG.info("{}",list);
	}
	
	private Map<String, Integer> buildPropertiesMap(){
		Map<String,Integer> response = new LinkedHashMap<>();
		response.put("connectionTimeout", connectionTimeout);
		response.put("requestTimeout", requestTimeout);
		response.put("readTimeout", readTimeout);
		response.put("retryEndPoint", retryEndPoint);
		return response;
	}

}
