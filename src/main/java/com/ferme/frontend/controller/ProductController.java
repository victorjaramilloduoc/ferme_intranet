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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ferme.frontend.util.FacesUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.portafolio.util.entities.CityEntity;
import com.portafolio.util.entities.HeadingEntity;
import com.portafolio.util.entities.LocationEntity;
import com.portafolio.util.entities.ProductEntity;
import com.portafolio.util.entities.ProductFamilyEntity;
import com.portafolio.util.entities.ProductSubFamilyEntity;
import com.portafolio.util.entities.RegionEntity;
import com.portafolio.util.entities.SupplierEntity;
import com.portafolio.util.entities.UserEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope(value = "session")
@Component(value = "productController")
@ELBeanName(value = "productController")
@Join(path = "/new-product", to = "/product/product-form.jsf")
@Data

public class ProductController {

	private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

	private ProductEntity product = new ProductEntity();

	private List<SupplierEntity> suppliers = new ArrayList<>();

//	private SupplierEntity supplier = new SupplierEntity();

	private List<HeadingEntity> heads = new ArrayList<>();

//	private HeadingEntity head = new HeadingEntity();

	private List<UserEntity> users = new ArrayList<>();

	private List<ProductSubFamilyEntity> subFamily = new ArrayList<>();

	private List<ProductFamilyEntity> prodFamily = new ArrayList<>();
	
	private List<RegionEntity> regions = new ArrayList<>();
	
	private List<CityEntity> cities = new ArrayList<>();
	
	private List<LocationEntity> locations = new ArrayList<>();

	private List<SelectItem> productSubItems = new ArrayList<>(), suppliersItems = new ArrayList<>(),
			usersItems = new ArrayList<>(), headsItems = new ArrayList<>(), prodFamilyItems = new ArrayList<>(),
			regionsItems = new ArrayList<>(), citiesItems = new ArrayList<>(), locationsItems = new ArrayList<>();

	private Long prodFamilyId, headsId, suppliersId, subFamilyId, userId, locationId, cityId, regionId;

	@Value("${local.connection.timeout}")
	private Integer connectionTimeout;

	@Value("${local.connection.request.timeout}")
	private Integer requestTimeout;

	@Value("${local.read.timeout}")
	private Integer readTimeout;

	@Value("${local.retry.endpoint}")
	private Integer retryEndPoint;

	@Value("${service.url.ferme.product.family}")
	private String prodFamilyUrl;

	@Value("${service.url.ferme.product.sub-family}")
	private String subFamilyUrl;

	@Value("${service.url.ferme.product}")
	private String productUrl;
	
	@Value("${service.url.ferme.users}")
	private String usersUrl;
	
	@Value("${service.url.ferme.supplier}")
	private String supplierUrl;
	
	@Value("${service.url.ferme.product.heading}")
	private String headingUrl;
	
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
		
		

		JSONArray prodFamilyResponse = RestClientUtil.getJsonArrayFromWs(prodFamilyUrl, null, null, null,
				buildPropertiesMap());
		Gson gson = new Gson();
		prodFamily = gson.fromJson(prodFamilyResponse.toString(), new TypeToken<List<ProductFamilyEntity>>() {
		}.getType());

		prodFamily.stream().forEach(data -> {
			prodFamilyItems.add(new SelectItem(data.getId(), data.getProductFamily()));
		});

		JSONArray subFamilyResponse = RestClientUtil.getJsonArrayFromWs(subFamilyUrl, null, null, null,
				buildPropertiesMap());
		gson = new Gson();
		subFamily = gson.fromJson(subFamilyResponse.toString(), new TypeToken<List<ProductSubFamilyEntity>>() {
		}.getType());

		subFamily.stream().forEach(data -> {
			productSubItems.add(new SelectItem(data.getId(), data.getSubFamilyProductName()));
		});

		JSONArray headsResponse = RestClientUtil.getJsonArrayFromWs(headingUrl, null, null, null, buildPropertiesMap());
		gson = new Gson();
		heads = gson.fromJson(headsResponse.toString(), new TypeToken<List<HeadingEntity>>() {
		}.getType());

		heads.stream().forEach(data -> {
			headsItems.add(new SelectItem(data.getId(), data.getHeadingType()));
		});
		
		JSONArray suppliersResponse = RestClientUtil.getJsonArrayFromWs(supplierUrl, null, null, null,
				buildPropertiesMap());
		gson = new Gson();
		suppliers = gson.fromJson(suppliersResponse.toString(), new TypeToken<List<SupplierEntity>>() {
		}.getType());

		suppliers.stream().forEach(data -> {
			suppliersItems.add(new SelectItem(data.getId(), data.getName()));
		});

		JSONArray usersResponse = RestClientUtil.getJsonArrayFromWs(usersUrl, null, null, null,
				buildPropertiesMap());
		gson = new Gson();
		users = gson.fromJson(usersResponse.toString(), new TypeToken<List<UserEntity>>() {
		}.getType());

		users.stream().forEach(data -> {
			usersItems.add(new SelectItem(data.getId(), data.getName()));
		});
		
		JSONArray regionsResponse = RestClientUtil.getJsonArrayFromWs(regionsUrl, null, null, null, buildPropertiesMap());
		gson = new Gson();
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

		product.setEnable(true);
//		System.err.println(subFamily.stream().filter(data -> data.getId().equals(subFamilyId)).findAny().get().getId());
//		product.getSubFamilyProduct().setId(subFamily.stream().filter(data -> data.getId().equals(subFamilyId)).findAny().get().getId());
//		product.getSupplier().setId(suppliers.stream().filter(data -> data.getId().equals(suppliersId)).findAny().get().getId());


		try {
			Object response = RestClientUtil.postPutPatchDeleteToWs(productUrl, null, null, product, null,
					HttpMethod.POST);

			if (null != response) {
				FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Inforcación",
						"Producto " + product.getName() + " creado con exito");
				System.out.println(product);
				loadData();
				FacesUtil.openPopUp("dlgRedirect");
			}

		} catch (Exception e) {
			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se ha podido guardar el producto");
			LOG.error("Error al guardar el producto, causa: {}", e.getMessage(), e);
		}
	}

	public String redirect(Boolean redirect) {
		if (redirect) {
			return "/new-product.xhtml?faces-redirect=true";
		} else {
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

}
