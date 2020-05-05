package com.github._1c_syntax.mdclasses.deserialize.context;

import lombok.Value;

import java.util.ArrayList;

@Value
public class Global {
  ArrayList<Method> methods;
  ArrayList<Property> properties;
}
