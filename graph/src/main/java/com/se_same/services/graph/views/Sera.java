package com.se_same.services.graph.views;

import io.vertx.core.json.Json;

import java.util.HashMap;

public class Sera extends HashMap<String, java.io.Serializable> {
  public String toJson() {
    return Json.encode(this);
  }
}
