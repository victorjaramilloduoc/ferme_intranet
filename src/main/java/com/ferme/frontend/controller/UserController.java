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

import com.ferme.frontend.util.DatasUtil;
import com.ferme.frontend.util.FacesUtil;
import com.ferme.frontend.util.FormatUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.portafolio.util.entities.CityEntity;
import com.portafolio.util.entities.LocationEntity;
import com.portafolio.util.entities.RegionEntity;
import com.portafolio.util.entities.RoleEntity;
import com.portafolio.util.entities.UserEntity;
import com.portafolio.util.entities.UserRoleEntity;
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
	
	private String formatedRut, secondLastName, additionalAddressInfo;
	 
	private List<RegionEntity> regions = new ArrayList<>();
	
	private List<CityEntity> cities = new ArrayList<>();
	
	private List<LocationEntity> locations = new ArrayList<>();
	
	private List<RoleEntity> roles = new ArrayList<>();
	
	private List<SelectItem> regionsItems = new ArrayList<>(), citiesItems = new ArrayList<>(),
			locationsItems = new ArrayList<>(), rolesItems = new ArrayList<>();
	
	private Long locationId, cityId, regionId, roleId;
	
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
	
	@Value("${service.url.ferme.cities.fitered}")
	private String citiesUrl;
	
	@Value("${service.url.ferme.locations.fitered}")
	private String locationsUrl;
	
	@Value("${service.url.ferme.users}")
	private String usersUrl;
	
	@Value("${service.url.ferme.roles}")
	private String rolesUrl;
	
	@Value("${service.url.ferme.save.user.roles}")
	private String userRoleUrl;
	
	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {
		resetValues();
		
		JSONArray regionsResponse = RestClientUtil.getJsonArrayFromWs(regionsUrl, null, null, null, buildPropertiesMap());
		Gson gson = new Gson();
		regions = gson.fromJson(regionsResponse.toString(), new TypeToken<List<RegionEntity>>() {}.getType());
		
		regions.stream().forEach(data -> {
			regionsItems.add(new SelectItem(data.getId(), data.getRegionName()));
		});
		
		DatasUtil.getUserRoles(rolesUrl, roles, buildPropertiesMap(), rolesItems);
	}

	/**
	 * Guardar usuario desde formulario
	 */
	public void save() {
		user.setEnable(true);
		user.getLocation()
				.setId(locations.stream().filter(data -> data.getId().equals(locationId)).findAny().get().getId());

		try {
			String[] arrayRut = formatedRut.replace(".", "").split("-");
			user.setAddress(additionalAddressInfo != null ? (user.getAddress()+ " "+ additionalAddressInfo) : user.getAddress());
			user.setLastName(secondLastName != null ? (user.getLastName() + " "+secondLastName) : user.getLastName()); 
			user.setRut(new Long(arrayRut[0]));
			user.setDv(arrayRut[1].charAt(0));

			Object response = RestClientUtil.postPutPatchDeleteToWs(usersUrl, null, null, user, null, HttpMethod.POST);
			String[] arr = response.toString().split(",");
			arr = arr[1].replace("{", "").split(":");
			
			UserRoleEntity userRole = new UserRoleEntity();
			userRole.getUser().setId(new Long(arr[2]));
			userRole.getRole().setId(roleId);
			
			Object roleResponse = RestClientUtil.postPutPatchDeleteToWs(userRoleUrl, null, null, userRole, null, HttpMethod.POST);

			if (null != response && roleResponse != null) {
				FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Inforcación",
						"Usuario " + user.getName() + " creado con exito");
				loadData();
				FacesUtil.openPopUp("dlgRedirect");
			}

		} catch (Exception e) {
			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se ha podido guardar el usuario");
			LOG.error("Error al guardar el usuario, causa: {}", e.getMessage(), e);
		}
	}
	
	public void getCitiesByRegion(Long regionId) {
		Map<String, String> uriParams = new LinkedHashMap<>();
		uriParams.put("regionId", regionId.toString());
		
		JSONArray cityResponse = RestClientUtil.getJsonArrayFromWs(citiesUrl, uriParams, null, null,
				buildPropertiesMap());
		Gson gson = new Gson();
		cities = gson.fromJson(cityResponse.toString(), new TypeToken<List<CityEntity>>() {}.getType());
		
		citiesItems.clear();
		locationsItems.clear();
		cities.stream().forEach(data -> {
			citiesItems.add(new SelectItem(data.getId(), data.getCityName()));
		});
	}
	
	public void getLocationsByCity(Long cityId) {
		Map<String, String> uriParams = new LinkedHashMap<>();
		uriParams.put("cityId", cityId.toString());
		
		JSONArray locationsResponse = RestClientUtil.getJsonArrayFromWs(locationsUrl, uriParams, null, null,
				buildPropertiesMap());
		Gson gson = new Gson();
		locations = gson.fromJson(locationsResponse.toString(), new TypeToken<List<LocationEntity>>() {}.getType());
		
		locationsItems.clear();
		locations.stream().forEach(data -> {
			locationsItems.add(new SelectItem(data.getId(), data.getLocatioName()));
		});
	}
	
	/**
	 * Darle formato de rut Chileno y validar rut.
	 * @param rut
	 * @return
	 */
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
		roleId = null;
		regionsItems.clear();
		rolesItems.clear();
	}

}
