package com.ferme.frontend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

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
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.ferme.frontend.util.FacesUtil;
import com.ferme.frontend.util.FormatUtil;
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
	
	
	private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

	
	private UserEntity user = new UserEntity();
	
	private String formatedRut;
	
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
	
	@Value("${service.url.ferme.users}")
	private String usersUrl;
	
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
		System.err.println(locations.stream().filter(data -> data.getId().equals(locationId)).findAny().get().getId());
		user.getLocation().setId(locations.stream().filter(data -> data.getId().equals(locationId)).findAny().get().getId());
		
		try {
			String[] arrayRut = formatedRut.replace(".", "").split("-");
			
			user.setRut(new Long(arrayRut[0]));
			user.setDv(arrayRut[1].charAt(0));
			
			Object response = RestClientUtil.postPutPatchDeleteToWs(usersUrl, null, null, user, null, HttpMethod.POST);
			
			if(null != response) {
				FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Inforcación", "Usuario "+user.getName()+" creado con exito");
				System.out.println(user);
				loadData();
				FacesUtil.openPopUp("dlgRedirect");
			}
			
			

		} catch (Exception e) {
			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se ha podido guardar el usuario");
			LOG.error("Error al guardar el usuario, causa: {}", e.getMessage(), e);
		}
	}
	
	public String formatRut(String rut) {
		if(FormatUtil.validarRut(rut)) {
			formatedRut = FormatUtil.formatRUT(rut);
		}else {
			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error","Rut no es valido");
			formatedRut = "";
		}
		return formatedRut;
	}
	
	public String redirect(Boolean redirect) {
		if(redirect) {
			resetValues();
			return "/user/user-form.xhtml?faces-redirect=true";
		}else {
			return "/index/index.xhtml?faces-redirect=true";
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
	
	private void resetValues() {
		user = new UserEntity();
		formatedRut = "";
		locationId = null;
		cityId = null;
		regionId = null;
	}

}
