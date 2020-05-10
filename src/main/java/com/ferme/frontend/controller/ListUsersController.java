package com.ferme.frontend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;

import org.json.JSONArray;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.primefaces.event.RowEditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.ferme.frontend.util.FacesUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.portafolio.util.entities.UserEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope(value = "session")
@Component(value = "listUsers")
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
	
	@Value("${service.url.ferme.users.delete}")
	private String deleteUsersUrl;
	
	private Long idUserToDisable;

	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {
		JSONArray json = RestClientUtil.getJsonArrayFromWs(usersUrl, null, null, null, buildPropertiesMap());

		Gson gson = new Gson();
		listUsers = gson.fromJson(json.toString(), new TypeToken<List<UserEntity>>() {
		}.getType());
		listUsers = listUsers.stream().filter(user -> user.isEnable()).collect(Collectors.toList());
	}
	
	/**
	 * Editar usuarios desde la tabla.
	 * @param event
	 */
	public void onRowEdit(RowEditEvent event) {

		Object response = RestClientUtil.postPutPatchDeleteToWs(usersUrl, null, null, (UserEntity) event.getObject(), null, HttpMethod.PUT);
		if (response != null) {
			System.out.println("Respuesta: " + response);

			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Inforcación", "Fila editada");
		} else {
			loadData();
			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error", "Edición fallida, error en servicio");
		}
	}
	
	/**
	 * Cancelar edición de usuario
	 * @param event
	 */
	public void onRowCancel(RowEditEvent event) {

		FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Info", "Edición cancelada");
	}
	
	/**
	 * Deshabilitar usuarios desde front
	 */
	public void disableUser() {
		FacesUtil.closePopUp("confirmDialog");
		try {
			Map<String, String> uriParams = new LinkedHashMap<>();
			uriParams.put("id", idUserToDisable.toString());
			Object obj = RestClientUtil.postPutPatchDeleteToWs(deleteUsersUrl, uriParams, null, null, null, HttpMethod.DELETE);
			if(obj != null) {
				loadData();
				FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Info", "Usuario Id: "+idUserToDisable+" deshabilitado.");
			}else {
				FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error en los servicios.");
			}
		} catch (Exception e) {
			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error",
					"Error al deshabilitar usuario Id: " + idUserToDisable + ". Causa: "+e.getMessage());
		}
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
