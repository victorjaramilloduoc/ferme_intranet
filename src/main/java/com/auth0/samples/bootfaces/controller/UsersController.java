package com.auth0.samples.bootfaces.controller;

import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope(value = "session")
@Component(value = "usersController")
@ELBeanName(value = "userController")
@Join(path = "/users/new-user", to = "/user/user-form.jsf")
public class UsersController {

}
