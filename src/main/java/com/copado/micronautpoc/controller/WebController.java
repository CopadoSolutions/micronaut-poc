package com.copado.micronautpoc.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller
public class WebController {

  @Get("/hello")
  public String greet() {
    return "Hello world";
  }

}
