package com.github._1c_syntax.mdclasses.context;

import com.github._1c_syntax.bsl.context.BSLEngine;
import com.github._1c_syntax.bsl.context.component.Method;
import com.github._1c_syntax.mdclasses.metadata.Configuration;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class MDEngine implements BSLEngine {

  Configuration configuration;
  Map<String, Method> globalMethods;

  @Override
  public Map<String, Method> getGlobalMethods() {
    return configuration.getGlobalMethodsPlatform();
  }

}
