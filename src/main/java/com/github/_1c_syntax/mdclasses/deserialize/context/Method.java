package com.github._1c_syntax.mdclasses.deserialize.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class Method {
  LangString name;
  LangString description;
  LangString example;
  List<String> availability = Collections.emptyList();
  List<String> returnedValues = Collections.emptyList();
}
