package com.ferme.frontend.controller;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.portafolio.util.entities.SaleEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope(value = "session")
@Component(value = "saleController")
@ELBeanName(value = "saleController")
@Join(path = "/sales", to = "/sales/sales-list.jsf")
@Data
public class SaleController {
	
	@Value("${local.connection.timeout}")
	private Integer connectionTimeout;

	@Value("${local.connection.request.timeout}")
	private Integer requestTimeout;

	@Value("${local.read.timeout}")
	private Integer readTimeout;

	@Value("${local.retry.endpoint}")
	private Integer retryEndPoint;
	
	@Value("${service.url.ferme.sales}")
	private String salesUrl;
	
	List<SaleEntity> sales = new ArrayList<>();
	
	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {
		JSONArray salesResponse = RestClientUtil.getJsonArrayFromWs(salesUrl, null, null, null,
				buildPropertiesMap());
		Gson gson = new Gson();
		sales = gson.fromJson(salesResponse.toString(), new TypeToken<List<SaleEntity>>() {
		}.getType());
		System.out.println(sales);
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
