package com.auth0.samples.bootfaces.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
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
import org.springframework.web.client.HttpClientErrorException;

import com.portafolio.util.entities.UserEntity;
import com.portafolio.util.login.LoginUtil;
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
	
	@Value("${service.url.ferme.users.login}")
	private String loginUrl;
	
	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {
	}
	
	public String save() {
		String response = ""; 
		try {
			String basicAuth = LoginUtil.createBasicAuth(user.getEmail(), user.getPassword());
			
			Object obj = RestClientUtil.postToWs(loginUrl, null, null, null, null, basicAuth);
			if(obj != null) {
				LOG.info("{}",obj);
				return "/index/index.xhtml?faces-redirect=true";
			}
		} catch (HttpClientErrorException e) {
			if(e.getStatusCode().value() == 400) {
				LOG.warn("Error en las credenciales, response:{}",e.getMessage());
			}else {
				LOG.error("Error al llamar al login");
			}
		}
		LOG.info(response);
		return response;
		
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
