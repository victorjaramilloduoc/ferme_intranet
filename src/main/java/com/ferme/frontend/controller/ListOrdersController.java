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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.portafolio.util.entities.PurchaseOrderEntity;
import com.portafolio.util.entities.StatusPurchaseOrderEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope(value = "session")
@Component(value = "listOrders")
@ELBeanName(value = "listOrders")
@Join(path = "/orders", to = "/compra/list-compra.jsf")
@Data
public class ListOrdersController {
	
	private List<StatusPurchaseOrderEntity> listOrders = new ArrayList<>();

	@Value("${local.connection.timeout}")
	private Integer connectionTimeout;

	@Value("${local.connection.request.timeout}")
	private Integer requestTimeout;

	@Value("${local.read.timeout}")
	private Integer readTimeout;

	@Value("${local.retry.endpoint}")
	private Integer retryEndPoint;
	
	@Value("${service.url.ferme.orders}")
	private String ordersUrl;

	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {

		JSONArray json = RestClientUtil.getJsonArrayFromWs(ordersUrl, null, null, null, buildPropertiesMap());
		Gson gson = new Gson();

		listOrders = gson.fromJson(json.toString(), new TypeToken<List<StatusPurchaseOrderEntity>>() {
		}.getType());
		System.out.println(listOrders);
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
