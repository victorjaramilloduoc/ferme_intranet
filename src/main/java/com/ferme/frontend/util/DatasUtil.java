package com.ferme.frontend.util;

import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.json.JSONArray;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.portafolio.util.entities.RoleEntity;
import com.portafolio.util.rest.client.RestClientUtil;

public class DatasUtil {
	
	/**
	 * 
	 * @param rolesUrl
	 * @param roles
	 * @param buildPropertiesMap
	 * @param rolesItems
	 */
	public static void getUserRoles(String rolesUrl, List<RoleEntity> roles, Map<String, Integer> buildPropertiesMap,
			List<SelectItem> rolesItems) {
		Gson gson;
		JSONArray rolesResponse = RestClientUtil.getJsonArrayFromWs(rolesUrl, null, null, null, buildPropertiesMap);
		gson = new Gson();
		roles = gson.fromJson(rolesResponse.toString(), new TypeToken<List<RoleEntity>>() {}.getType());
		
		roles.stream().forEach(data -> {
			rolesItems.add(new SelectItem(data.getId(), data.getRoleType()));
		});
	}

}
