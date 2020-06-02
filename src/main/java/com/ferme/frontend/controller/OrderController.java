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
import com.portafolio.util.entities.ProductEntity;
import com.portafolio.util.entities.PurchaseOrderEntity;
import com.portafolio.util.entities.StatusPurchaseOrderEntity;
import com.portafolio.util.entities.SupplierEntity;
import com.portafolio.util.entities.UserEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope(value = "session")
@Component(value = "orderController")
@ELBeanName(value = "orderController")
@Join(path = "/orders/new-orden", to = "/compra/orden-form.jsf")
@Data
public class OrderController {

	private StatusPurchaseOrderEntity order = new StatusPurchaseOrderEntity();
	
	private ProductEntity product = new ProductEntity();
	
	private SupplierEntity supps = new SupplierEntity();
	
	private List<SupplierEntity> supp = new ArrayList<>();
	
	private List<ProductEntity> products = new ArrayList<>();

	private List<UserEntity> users = new ArrayList<>();
	
	private List<SelectItem> usersItems = new ArrayList<>(), productsItems = new ArrayList<>(), suppItems = new ArrayList<>();
	
	private Long userId, suppId;
	
	private String product_name;

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
	
	@Value("${service.url.ferme.supplier.filtered}")
	private String suppliersUrl;
	
	@Value("${service.url.ferme.product}")
	private String productUrl;
	
	
	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData(){
		
		JSONArray usersResponse = RestClientUtil.getJsonArrayFromWs(usersUrl, null, null, null, buildPropertiesMap());
		Gson gson = new Gson();
		users = gson.fromJson(usersResponse.toString(), new TypeToken<List<UserEntity>>() {
		}.getType());

		users.stream().forEach(data -> {
			usersItems.add(new SelectItem(data.getId(), data.getName()));
		});			
				
		JSONArray productsResponse = RestClientUtil.getJsonArrayFromWs(productUrl, null, null, null,
				buildPropertiesMap());
		gson = new Gson();
		products = gson.fromJson(productsResponse.toString(), new TypeToken<List<ProductEntity>>() {
		}.getType());

		products.stream().forEach(data -> {
			productsItems.add(new SelectItem(data.getName()));
		});
		
	}
	
	public void Save() {
		

		System.err.println(supp.stream().filter(data -> data.getId().equals(suppId)).findAny().get().getId());
//		try {
//			supps.getLocation().setId(locations.stream().filter(data -> data.getId().equals(locationId)).findAny().get().getId());
//		}catch (Exception e)
//		{
//			System.out.println(e);
//		}
//		supp.getUser().setId(users.stream().filter(data -> data.getId().equals(userId)).findAny().get().getId());
//		supp.getHeading().setId(heads.stream().filter(data -> data.getId().equals(headsId)).findAny().get().getId());
		
	}
	
	public void getSuppliersByProduct(String product_name) {
		Map<String, String> uriParams = new LinkedHashMap<>();
		uriParams.put("product_name", product_name);
		
		JSONArray suppResponse = RestClientUtil.getJsonArrayFromWs(suppliersUrl, null, uriParams, null,
				buildPropertiesMap());
		Gson gson = new Gson();
		supp = gson.fromJson(suppResponse.toString(), new TypeToken<List<SupplierEntity>>() {}.getType());
		
		suppItems.clear();
		supp.stream().forEach(data -> {
			suppItems.add(new SelectItem(data.getId(), data.getName()));
		});
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
