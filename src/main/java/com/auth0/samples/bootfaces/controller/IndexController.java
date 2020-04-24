package com.auth0.samples.bootfaces.controller;

import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope(value = "session")
@Component(value = "indexController")
@ELBeanName(value = "indexController")
@Join(path = "/index", to = "/index/index.jsf")
public class IndexController {

}
