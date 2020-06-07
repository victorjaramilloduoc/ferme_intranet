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

import com.ferme.frontend.util.FacesUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.portafolio.util.entities.ProductEntity;
import com.portafolio.util.entities.StatusPurchaseOrderEntity;
import com.portafolio.util.entities.SupplierEntity;
import com.portafolio.util.entities.UserEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Data;

@Scope(value = "session")
@Component(value = "orderController")
@ELBeanName(value = "orderController")
@Join(path = "/orders/new-order", to = "/order/orden-form.jsf")
@Data
public class OrderController {

	private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);

	private StatusPurchaseOrderEntity order = new StatusPurchaseOrderEntity();

	private List<SupplierEntity> supp = new ArrayList<>();

	private List<ProductEntity> products = new ArrayList<>();

	private List<UserEntity> users = new ArrayList<>();

	private List<SelectItem> usersItems = new ArrayList<>(), productsItems = new ArrayList<>(),
			suppItems = new ArrayList<>();

	private Long userId, suppId, productId;

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

	@Value("${service.url.ferme.orders}")
	private String ordersUrl;

	@Deferred
	@RequestAction
	@IgnorePostback

	public void loadData() {

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
			productsItems.add(new SelectItem(data.getId(), data.getName()));
		});

	}

	public void save() {

		try {
			order.setStatusPurchaseOrder("Pendiente");

			order.getPurchaseOrder().getUser()
					.setId(users.stream().filter(data -> data.getId().equals(userId)).findAny().get().getId());

			order.getPurchaseOrder().getReception().setObsevations("Pendiente");

			order.getPurchaseOrder().getReception().getReceptionUser()
					.setId(users.stream().filter(data -> data.getId().equals(userId)).findAny().get().getId());

			order.getPurchaseOrder().getProduct()
					.setId(products.stream().filter(data -> data.getId().equals(productId)).findAny().get().getId());
		} catch (Exception e) {
			System.out.println(e);
		}

		try {

			Object response = RestClientUtil.postPutPatchDeleteToWs(ordersUrl, null, null, order, null,
					HttpMethod.POST);
			if (null != response) {
				FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Inforcación",
						"Orden " + order.getId() + " creada con exito");
				loadData();
				FacesUtil.openPopUp("dlgRedirect");
			}

		} catch (Exception e) {
			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se ha podido guardar la Orden");
			LOG.error("Error al guardar la Orden, causa: {}", e.getMessage(), e);
		}

	}

	public void getSuppliersByProductId(Long productId) {
		Map<String, String> uriParams = new LinkedHashMap<>();
		uriParams.put("product_id", productId.toString());

		JSONArray suppResponse = RestClientUtil.getJsonArrayFromWs(suppliersUrl, null, uriParams, null,
				buildPropertiesMap());
		Gson gson = new Gson();
		supp = gson.fromJson(suppResponse.toString(), new TypeToken<List<SupplierEntity>>() {
		}.getType());

		suppItems.clear();
		supp.stream().forEach(data -> {
			suppItems.add(new SelectItem(data.getId(), data.getName()));
		});
	}

	public String redirect(Boolean redirect) {
		if (redirect) {
			resetValues();
			return "/order/orden-form.xhtml?faces-redirect=true";
		} else {
			return "/index/index.xhtml?faces-redirect=true";
		}
	}

	private void resetValues() {
		order = new StatusPurchaseOrderEntity();
		userId = null;
		suppId = null;
		productId = null;
		usersItems.clear();
		suppItems.clear();
		productsItems.clear();
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
