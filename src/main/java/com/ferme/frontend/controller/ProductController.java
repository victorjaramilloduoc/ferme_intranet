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
import com.portafolio.util.entities.ProductEntity;
import com.portafolio.util.entities.ProductSubFamilyEntity;
import com.portafolio.util.entities.SupplierEntity;
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

	private List<ProductSubFamilyEntity> subFamily = new ArrayList<>();

	private List<SelectItem> suppliersItems = new ArrayList<>(),
			subFamilyItems = new ArrayList<>();

	private Long suppliersId, subFamilyId;

	@Value("${local.connection.timeout}")
	private Integer connectionTimeout;

	@Value("${local.connection.request.timeout}")
	private Integer requestTimeout;

	@Value("${local.read.timeout}")
	private Integer readTimeout;

	@Value("${local.retry.endpoint}")
	private Integer retryEndPoint;

	@Value("${service.url.ferme.product.sub-family}")
	private String subFamilyUrl;

	@Value("${service.url.ferme.product}")
	private String productUrl;
		
	@Value("${service.url.ferme.supplier}")
	private String supplierUrl;	
	
	@Deferred
	@RequestAction
	@IgnorePostback

	public void loadData() {
		resetValues();
		
		JSONArray subFamilyResponse = RestClientUtil.getJsonArrayFromWs(subFamilyUrl, null, null, null,
				buildPropertiesMap());
		Gson gson = new Gson();
		subFamily = gson.fromJson(subFamilyResponse.toString(), new TypeToken<List<ProductSubFamilyEntity>>() {
		}.getType());

		subFamily.stream().forEach(data -> {
			subFamilyItems.add(new SelectItem(data.getId(), data.getSubFamilyProductName()));
		});
				
		JSONArray suppliersResponse = RestClientUtil.getJsonArrayFromWs(supplierUrl, null, null, null,
				buildPropertiesMap());
		gson = new Gson();
		suppliers = gson.fromJson(suppliersResponse.toString(), new TypeToken<List<SupplierEntity>>() {
		}.getType());

		suppliers.stream().forEach(data -> {
			suppliersItems.add(new SelectItem(data.getId(), data.getName()));
		});

	}

	public void save() {

		product.setEnable(true);
		product.getSubFamilyProduct().setId(subFamily.stream().filter(data -> data.getId().equals(subFamilyId)).findAny().get().getId());
		product.getSupplier().setId(suppliers.stream().filter(data -> data.getId().equals(suppliersId)).findAny().get().getId());

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
			resetValues();
			return "/new-product.xhtml?faces-redirect=true";
		} else {
			return "/index/index.xhtml?faces-redirect=true";
		}
	}
	
	private void resetValues() {
		product = new ProductEntity();
		suppliersId = null;
		subFamilyId = null;
		suppliersItems.clear();
		subFamilyItems.clear();
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
