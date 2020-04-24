package com.auth0.samples.bootfaces.controller;

import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope (value = "session")
@Component (value = "listUsers")
@ELBeanName(value = "listUsers")
@Join(path = "/users", to = "/user/user-list.jsf")
public class ListUsersController {

	@Deferred
	@RequestAction
	@IgnorePostback
	public void loadData() {
	}

}
