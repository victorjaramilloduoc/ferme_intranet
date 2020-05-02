package com.ferme.frontend.util;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.primefaces.PrimeFaces;

public class FacesUtil {
	
	/**
	 * 
	 * @param wid
	 */
	public static void openPopUp(String wid) {
		PrimeFaces current = PrimeFaces.current();
		current.executeScript("PF('"+wid+"').show();");
	}
	
	/**
	 * 
	 * @param wid
	 */
	public static void closePopUp(String wid) {
		PrimeFaces current = PrimeFaces.current();
		current.executeScript("PF('"+wid+"').hide();");
	}
	
	public static void showMessage(String titleMsg, String bodyMsg) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, titleMsg, bodyMsg);

		PrimeFaces.current().dialog().showMessageDynamic(message);
	}
	
	public static void showPopUpMessage(Severity messageType, String title, String body) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, title, body));
	}

}
