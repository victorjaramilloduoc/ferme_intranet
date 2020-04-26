package com.auth0.samples.bootfaces.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.json.JSONArray;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.portafolio.util.entities.UserEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope (value = "session")
@Component (value = "listUsers")
@ELBeanName(value = "listUsers")
@Join(path = "/users", to = "/user/user-list.jsf")
@Data
public class ListUsersController {
	
	private Logger LOG = LoggerFactory.getLogger(ListUsersController.class);
	
	private List<UserEntity> listUsers = new ArrayList<>();
	
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
		JSONArray json = RestClientUtil.getJsonArrayFromWs(usersUrl, null, null, null, buildPropertiesMap());

		Gson gson = new Gson();
		listUsers = gson.fromJson(json.toString(), new TypeToken<List<UserEntity>>(){}.getType());
		LOG.info("{}",listUsers);
	}
	
	public void onRowEdit(RowEditEvent event) {
		System.out.println("Row edited");
		System.out.println((UserEntity)event.getObject());
		
		Object response = RestClientUtil.putToWs(usersUrl, null, null, (UserEntity)event.getObject(), null);
		if(response != null) {
			System.out.println("Respuesta: "+response);
			
			showPopUpMessage(FacesMessage.SEVERITY_INFO, "Inforcación", "Fila editada");
		}else {
			loadData();
			showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Inforcación", "Edición fallida, error en servicio");
		}
    }
     
    public void onRowCancel(RowEditEvent event) {
    	System.out.println("Edit cancel");
        System.out.println((UserEntity)event.getObject());
        
        showPopUpMessage(FacesMessage.SEVERITY_INFO, "Error", "Edición cancelada");
    }
	
	private Map<String, Integer> buildPropertiesMap(){
		Map<String,Integer> response = new LinkedHashMap<>();
		response.put("connectionTimeout", connectionTimeout);
		response.put("requestTimeout", requestTimeout);
		response.put("readTimeout", readTimeout);
		response.put("retryEndPoint", retryEndPoint);
		return response;
	}
	
	public void showMessage(String titleMsg, String bodyMsg) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, titleMsg, bodyMsg);
         
        PrimeFaces.current().dialog().showMessageDynamic(message);
    }
	
	private void showPopUpMessage(Severity messageType, String title, String body) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType,
				title, body));
	}

}
