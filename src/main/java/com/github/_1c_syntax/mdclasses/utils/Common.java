/*
 * This file is a part of MDClasses.
 *
 * Copyright © 2019 - 2020
 * Tymko Oleg <olegtymko@yandex.ru>, Maximov Valery <maximovvalery@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * MDClasses is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * MDClasses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MDClasses.
 */
package com.github._1c_syntax.mdclasses.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github._1c_syntax.bsl.context.component.Method;
import com.github._1c_syntax.mdclasses.deserialize.context.ContextJSON;
import com.github._1c_syntax.mdclasses.mdo.CommonModule;
import com.github._1c_syntax.mdclasses.mdo.MDObjectBase;
import com.github._1c_syntax.mdclasses.metadata.Configuration;
import com.github._1c_syntax.mdclasses.metadata.SupportConfiguration;
import com.github._1c_syntax.mdclasses.metadata.additional.*;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.io.IOException;
import java.net.URI;
import java.util.*;

@Slf4j
@UtilityClass
public class Common {

  private static final Map<MDOType, Set<ModuleType>> MODULE_TYPES_FOR_MDO_TYPES = moduleTypesForMDOTypes();
  private static final CompatibilityMode VERSION_8_3_15 = new CompatibilityMode(3, 15);

  public static Map<URI, Map<SupportConfiguration, SupportVariant>> getModuleSupports(Configuration configuration) {
    Map<URI, Map<SupportConfiguration, SupportVariant>> modulesBySupport = new HashMap<>();
    var fileParentConfiguration = MDOPathUtils.getParentConfigurationsPath(
      configuration.getConfigurationSource(), configuration.getRootPath());

    if (fileParentConfiguration == null || !fileParentConfiguration.toFile().exists()) {
      return modulesBySupport;
    }

    ParseSupportData supportData = new ParseSupportData(fileParentConfiguration);
    final Map<String, Map<SupportConfiguration, SupportVariant>> supportMap = supportData.getSupportMap();

    configuration.getChildren().forEach(mdObject -> {
      modulesBySupport.putAll(getMDObjectSupport(supportMap, mdObject));
      if (mdObject.getForms() != null) {
        mdObject.getForms().forEach(form -> modulesBySupport.putAll(getMDObjectSupport(supportMap, form)));
      }
      if (mdObject.getCommands() != null) {
        mdObject.getCommands().forEach(command -> modulesBySupport.putAll(getMDObjectSupport(supportMap, command)));
      }
    });

    return modulesBySupport;
  }

  private static Map<URI, Map<SupportConfiguration, SupportVariant>> getMDObjectSupport(
    Map<String, Map<SupportConfiguration, SupportVariant>> supportMap,
    MDObjectBase mdObject) {
    Map<URI, Map<SupportConfiguration, SupportVariant>> modulesBySupport = new HashMap<>();
    if (mdObject.getUuid() == null || mdObject.getModulesByType() == null) {
      return modulesBySupport;
    }

    Set<URI> uris = new HashSet<>(mdObject.getModulesByType().keySet());

    Map<SupportConfiguration, SupportVariant> moduleSupport =
      supportMap.getOrDefault(mdObject.getUuid(), Collections.emptyMap());

    for (URI uri : uris) {
      modulesBySupport.put(uri, moduleSupport);
    }
    return modulesBySupport;
  }

  public static Map<URI, ModuleType> getModuleTypesByPath(Configuration configuration) {
    Map<URI, ModuleType> modulesByType = new HashMap<>();

    configuration.getChildren().forEach(mdObject -> {
        if (mdObject.getModulesByType() != null) {
          mdObject.getModulesByType().forEach(modulesByType::put);
        }
        if (mdObject.getForms() != null) {
          mdObject.getForms().forEach(form -> {
            if (form.getModulesByType() != null) {
              form.getModulesByType().forEach(modulesByType::put);
            }
          });
        }

        if (mdObject.getCommands() != null) {
          mdObject.getCommands().forEach(command -> {
            if (command.getModulesByType() != null) {
              command.getModulesByType().forEach(modulesByType::put);
            }
          });
        }
      }
    );
    return modulesByType;
  }

  public static Map<URI, MDObjectBase> getModulesByURI(Configuration configuration) {
    Map<URI, MDObjectBase> modulesByType = new HashMap<>();

    configuration.getChildren().forEach(mdObject -> {
        if (mdObject.getModulesByType() != null) {
          mdObject.getModulesByType().forEach((uri, moduleType) -> modulesByType.put(uri, mdObject));
        }

        if (mdObject.getForms() != null) {
          mdObject.getForms().forEach(form -> {
            if (form.getModulesByType() != null) {
              form.getModulesByType().forEach((uri, moduleType) -> modulesByType.put(uri, form));
            }
          });
        }

        if (mdObject.getCommands() != null) {
          mdObject.getCommands().forEach(command -> {
            if (command.getModulesByType() != null) {
              command.getModulesByType().forEach((uri, moduleType) -> modulesByType.put(uri, command));
            }
          });
        }

      }
    );
    return modulesByType;
  }

  public static Map<String, CommonModule> getCommonModules(Configuration configuration) {
    Map<String, CommonModule> modulesByName = new CaseInsensitiveMap<>();

    configuration.getChildren().forEach(mdObject -> {
      if (mdObject.getType() == MDOType.COMMON_MODULE & mdObject instanceof CommonModule) {
        modulesByName.put(mdObject.getName(), (CommonModule) mdObject);
      }
    });
    return modulesByName;
  }

  public Map<MDOType, Set<ModuleType>> getModuleTypesForMdoTypes() {
    return MODULE_TYPES_FOR_MDO_TYPES;
  }

  private static Map<MDOType, Set<ModuleType>> moduleTypesForMDOTypes() {
    Map<MDOType, Set<ModuleType>> result = new HashMap<>();

    for (MDOType mdoType : MDOType.values()) {
      Set<ModuleType> types = new HashSet<>();
      switch (mdoType) {
        case ACCOUNTING_REGISTER:
        case ACCUMULATION_REGISTER:
        case CALCULATION_REGISTER:
        case INFORMATION_REGISTER:
          types.add(ModuleType.ManagerModule);
          types.add(ModuleType.RecordSetModule);
          break;
        case BUSINESS_PROCESS:
        case CATALOG:
        case CHART_OF_ACCOUNTS:
        case CHART_OF_CALCULATION_TYPES:
        case CHART_OF_CHARACTERISTIC_TYPES:
        case DATA_PROCESSOR:
        case DOCUMENT:
        case EXCHANGE_PLAN:
        case REPORT:
        case TASK:
          types.add(ModuleType.ManagerModule);
          types.add(ModuleType.ObjectModule);
          break;
        case COMMAND_GROUP:
        case COMMON_ATTRIBUTE:
        case COMMON_PICTURE:
        case COMMON_TEMPLATE:
        case DEFINED_TYPE:
        case DOCUMENT_NUMERATOR:
        case EVENT_SUBSCRIPTION:
        case FUNCTIONAL_OPTION:
        case ROLE:
        case SCHEDULED_JOB:
        case SESSION_PARAMETER:
        case STYLE_ITEM:
        case STYLE:
        case SUBSYSTEM:
        case WS_REFERENCE:
        case XDTO_PACKAGE:
          break;
        case COMMON_COMMAND:
          types.add(ModuleType.CommandModule);
          break;
        case COMMON_FORM:
          types.add(ModuleType.FormModule);
          break;
        case COMMON_MODULE:
          types.add(ModuleType.CommonModule);
          break;
        case CONFIGURATION:
          types.add(ModuleType.ApplicationModule);
          types.add(ModuleType.SessionModule);
          types.add(ModuleType.ExternalConnectionModule);
          types.add(ModuleType.ManagedApplicationModule);
          types.add(ModuleType.OrdinaryApplicationModule);
          break;
        case CONSTANT:
          types.add(ModuleType.ValueManagerModule);
          break;
        case DOCUMENT_JOURNAL:
        case ENUM:
        case FILTER_CRITERION:
        case SETTINGS_STORAGE:
          types.add(ModuleType.ManagerModule);
          break;
        case HTTP_SERVICE:
          types.add(ModuleType.HTTPServiceModule);
          break;
        case SEQUENCE:
          types.add(ModuleType.RecordSetModule);
          break;
        case WEB_SERVICE:
          types.add(ModuleType.WEBServiceModule);
          break;
        default:
          // non
      }

      result.put(mdoType, types);
    }

    return result;
  }

  public Map<String, Method> fillGlobalMethodContext(CompatibilityMode compatibilityMode) {
    Map<String, Method> map = new CaseInsensitiveMap<>();

    var url = Common.class.getResource(getPathToContextFile(compatibilityMode));
    if (url == null) {
      url = Common.class.getResource(getPathToContextFile(new CompatibilityMode()));
    }

    ContextJSON context;
    ObjectMapper mapper = new ObjectMapper();
    try {
      context = mapper.readValue(url, ContextJSON.class);
    } catch (IOException e) {
      LOGGER.error("При чтение JSON проблема", e);
      return map;
    }

    context.getGlobal().getMethods().stream()
      .map(methodJSON -> Method.builder()
        .name(methodJSON.getName().getRu())
        .nameEn(methodJSON.getName().getEn())
        .function(!methodJSON.getReturnedValues().isEmpty())
        .build())
      .forEach(method -> addMethodToMap(map, method));

    return map;
  }

  private String getPathToContextFile(CompatibilityMode compatibilityMode) {
    String version;
    CompatibilityMode modeForVersion = compatibilityMode;
    if (CompatibilityMode.compareTo(compatibilityMode, new CompatibilityMode()) == 0) {
      // TODO: а какую версию отдавать без режима совместимости?
      modeForVersion = new CompatibilityMode(3, 8);
    }
    version = String.join(
      ".",
      String.valueOf(modeForVersion.getMajor()),
      String.valueOf(modeForVersion.getMinor()),
      String.valueOf(modeForVersion.getVersion()));
    return String.format("/context/%s.json", version);
  }

  private void addMethodToMap(Map<String, Method> map, Method method) {
    map.put(method.getName(), method);
    map.put(method.getNameEn(), method);
  }

}
