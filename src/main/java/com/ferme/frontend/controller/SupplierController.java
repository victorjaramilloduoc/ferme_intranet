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
import com.portafolio.util.entities.HeadingEntity;
import com.portafolio.util.entities.LocationEntity;
import com.portafolio.util.entities.RegionEntity;
import com.portafolio.util.entities.SupplierEntity;
import com.portafolio.util.entities.UserEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope(value = "session")
@Component(value = "supplierController")
@ELBeanName(value = "supplierController")
@Join(path = "/suppliers/new-supplier", to = "/supplier/supplier-form.jsf")
@Data
public class SupplierController {

	private static final Logger LOG = LoggerFactory.getLogger(SupplierController.class);

	private SupplierEntity supp = new SupplierEntity();

	private String formatedRut;

	private List<RegionEntity> regions = new ArrayList<>();

	private List<CityEntity> cities = new ArrayList<>();
	
	private List<HeadingEntity> heads = new ArrayList<>();

	private List<UserEntity> users = new ArrayList<>();

	private List<LocationEntity> locations = new ArrayList<>();

	private List<SelectItem> regionsItems = new ArrayList<>(), citiesItems = new ArrayList<>(),
			locationsItems = new ArrayList<>(), headsItems = new ArrayList<>(), usersItems = new ArrayList<>();

	private Long locationId, cityId, regionId, userId, headsId;

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

	@Value("${service.url.ferme.supplier}")
	private String suppliersUrl;
	
	@Value("${service.url.ferme.heading}")
	private String headingUrl;

	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {
		resetValues();

		JSONArray regionsResponse = RestClientUtil.getJsonArrayFromWs(regionsUrl, null, null, null,
				buildPropertiesMap());
		Gson gson = new Gson();
		regions = gson.fromJson(regionsResponse.toString(), new TypeToken<List<RegionEntity>>() {
		}.getType());

		regions.stream().forEach(data -> {
			regionsItems.add(new SelectItem(data.getId(), data.getRegionName()));
		});

		JSONArray usersResponse = RestClientUtil.getJsonArrayFromWs(usersUrl, null, null, null, buildPropertiesMap());
		gson = new Gson();
		users = gson.fromJson(usersResponse.toString(), new TypeToken<List<UserEntity>>() {
		}.getType());

		users.stream().forEach(data -> {
			if(data.isEnable()) {
				usersItems.add(new SelectItem(data.getId(), data.getName()));
			}
		});
		
		JSONArray headsResponse = RestClientUtil.getJsonArrayFromWs(headingUrl, null, null, null, buildPropertiesMap());
		gson = new Gson();
		heads = gson.fromJson(headsResponse.toString(), new TypeToken<List<HeadingEntity>>() {
		}.getType());

		heads.stream().forEach(data -> {
			headsItems.add(new SelectItem(data.getId(), data.getHeadingType()));
		});

	}

	public void save() {

		supp.setEnable(true);
		supp.getLocation()
				.setId(locations.stream().filter(data -> data.getId().equals(locationId)).findAny().get().getId());
		supp.getUser().setId(users.stream().filter(data -> data.getId().equals(userId)).findAny().get().getId());
		supp.getHeading().setId(heads.stream().filter(data -> data.getId().equals(headsId)).findAny().get().getId());
		try {

			String[] arrayRut = formatedRut.replace(".", "").split("-");

			supp.setRut(new Long(arrayRut[0]));
			supp.setDv(arrayRut[1].charAt(0));

			Object response = RestClientUtil.postPutPatchDeleteToWs(suppliersUrl, null, null, supp, null,
					HttpMethod.POST);
			if (null != response) {
				FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Inforcación",
						"Supervisor " + supp.getName() + " creado con exito");
				loadData();
				FacesUtil.openPopUp("dlgRedirect");
			}

		} catch (Exception e) {
			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se ha podido guardar el Supervisor");
			LOG.error("Error al guardar el Supervisor, causa: {}", e.getMessage(), e);
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
	
	private void resetValues() {
		supp = new SupplierEntity();
		formatedRut = "";
		locationId = null;
		cityId = null;
		regionId = null;
		userId = null;
		headsId = null;
		usersItems.clear();
		headsItems.clear();
		regionsItems.clear();
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
