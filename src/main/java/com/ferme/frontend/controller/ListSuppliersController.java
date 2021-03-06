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
import com.portafolio.util.entities.SupplierEntity;
import com.portafolio.util.rest.client.RestClientUtil;

import lombok.Data;

@Scope(value = "session")
@Component(value = "listSuppliers")
@ELBeanName(value = "listSuppliers")
@Join(path = "/suppliers", to = "/supplier/supplier-list.jsf")
@Data
public class ListSuppliersController {
	
	private Logger LOG = LoggerFactory.getLogger(ListSuppliersController.class);
	
	private List<SupplierEntity> listSuppliers = new ArrayList<>();
	
	@Value("${local.connection.timeout}")
	private Integer connectionTimeout;

	@Value("${local.connection.request.timeout}")
	private Integer requestTimeout;

	@Value("${local.read.timeout}")
	private Integer readTimeout;

	@Value("${local.retry.endpoint}")
	private Integer retryEndPoint;
	
	@Value("${service.url.ferme.supplier}")
	private String suppliersUrl;
	
	@Value("${service.url.ferme.supplier.delete}")
	private String deleteSuppliersUrl;
	
	private Long idSupplierToDisable;
	
	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {
		
		JSONArray json = RestClientUtil.getJsonArrayFromWs(suppliersUrl, null, null, null, buildPropertiesMap());
		Gson gson = new Gson();
		
		listSuppliers = gson.fromJson(json.toString(), new TypeToken<List<SupplierEntity>>(){			
		}.getType());
		listSuppliers = listSuppliers.stream().filter(supp -> supp.isEnable()).collect(Collectors.toList());
		LOG.info("{}", listSuppliers);
	}
	
	
	public void onRowEdit(RowEditEvent event) {
		System.out.println("Row edited");
		System.out.println((SupplierEntity) event.getObject());

		Object response = RestClientUtil.postPutPatchDeleteToWs(suppliersUrl, null, null, (SupplierEntity) event.getObject(), null, HttpMethod.PUT);
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
		System.out.println((SupplierEntity) event.getObject());

		FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Info", "Edición cancelada");
	}
	
	public void disableSupplier() {
		System.out.println(idSupplierToDisable);
		FacesUtil.closePopUp("confirmDialog");
		try {
			Map<String, String> uriParams = new LinkedHashMap<>();
			uriParams.put("id", idSupplierToDisable.toString());
			Object obj = RestClientUtil.postPutPatchDeleteToWs(deleteSuppliersUrl, uriParams, null, null, null,
					HttpMethod.DELETE);
			if (obj != null) {
				loadData();
				FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_INFO, "Info",
						"Supervisor Id: " + idSupplierToDisable + " deshabilitado.");
			} else {
				FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error en los servicios");
			}
		} catch (Exception e) {
			FacesUtil.showPopUpMessage(FacesMessage.SEVERITY_ERROR, "Error",
					"Error al deshabilitar Supervisor Id: " + idSupplierToDisable + ". Causa: " + e.getMessage());

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
