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
import com.portafolio.util.entities.ProductEntity;

import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope(value = "session")
@Component(value = "listProducts")
@ELBeanName(value = "listProducts")
@Join(path = "/products", to = "/product/product-list.jsf")
@Data
public class ListProductsController {

	private Logger LOG = LoggerFactory.getLogger(ListProductsController.class);

	private List<ProductEntity> listProducts = new ArrayList<>();

	@Value("${local.connection.timeout}")
	private Integer connectionTimeout;

	@Value("${local.connection.request.timeout}")
	private Integer requestTimeout;

	@Value("${local.read.timeout}")
	private Integer readTimeout;

	@Value("${local.retry.endpoint}")
	private Integer retryEndPoint;

	@Value("${service.url.ferme.product}")
	private String productUrl;

	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {

		JSONArray json = RestClientUtil.getJsonArrayFromWs(productUrl, null, null, null, buildPropertiesMap());

		Gson gson = new Gson();
		listProducts = gson.fromJson(json.toString(), new TypeToken<List<ProductEntity>>() {
		}.getType());
		listProducts = listProducts.stream().collect(Collectors.toList());
		LOG.info("{}", listProducts);

	}
	
	public void onRowEdit(RowEditEvent event) {
		System.out.println("Row edited");
		System.out.println((ProductEntity) event.getObject());

		Object response = RestClientUtil.postPutPatchDeleteToWs(productUrl, null, null, (ProductEntity) event.getObject(), null, HttpMethod.PUT);
		if (response != null) {
			System.out.println("Respuesta: " + response);

			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Inforcación", "Fila editada");
		} else {
			loadData();
			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error", "Edición fallida, error en servicio");
		}
	}
	
	public void onRowCancel(RowEditEvent event) {
		System.out.println("Edit cancel");
		System.out.println((ProductEntity) event.getObject());

		FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Info", "Edición cancelada");
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