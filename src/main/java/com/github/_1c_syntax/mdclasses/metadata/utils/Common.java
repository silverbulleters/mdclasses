package com.github._1c_syntax.mdclasses.metadata.utils;

import com.github._1c_syntax.mdclasses.metadata.SupportDataConfiguration;
import com.github._1c_syntax.mdclasses.metadata.additional.ModuleType;
import com.github._1c_syntax.mdclasses.metadata.additional.SupportVariant;
import com.github._1c_syntax.mdclasses.metadata.configurations.AbstractConfiguration;
import com.github._1c_syntax.mdclasses.metadata.configurations.EDTConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {

  private static final Logger LOGGER = LoggerFactory.getLogger(Common.class.getSimpleName());

  public static final String EXTENSION_BSL = "bsl";
  public static final String FILE_SEPARATOR = Pattern.quote(System.getProperty("file.separator"));

  public static final String REGEX_GUID = "uuid=\"(.*)\"";
  public static final Pattern PATTERN_REGEX_GUID = Pattern.compile(REGEX_GUID, Pattern.MULTILINE);
  public static final String EXTENSION_XML = "xml";
  public static final String EXTENSION_MDO = "mdo";

  public static ModuleType changeModuleTypeByFileName(String fileName, String secondFileName) {

    ModuleType moduleType = null;

    if (fileName.equalsIgnoreCase("CommandModule")) {
      moduleType = ModuleType.CommandModule;
    } else if (fileName.equalsIgnoreCase("ObjectModule")) {
      moduleType = ModuleType.ObjectModule;
    } else if (fileName.equalsIgnoreCase("ManagerModule")) {
      moduleType = ModuleType.ManagerModule;
    } else if (fileName.equalsIgnoreCase("ManagedApplicationModule")) {
      moduleType = ModuleType.ManagedApplicationModule;
    } else if (fileName.equalsIgnoreCase("OrdinaryApplicationModule")) {
      moduleType = ModuleType.OrdinaryApplicationModule;
    } else if (fileName.equalsIgnoreCase("SessionModule")) {
      moduleType = ModuleType.SessionModule;
    } else if (fileName.equalsIgnoreCase("RecordSetModule")) {
      moduleType = ModuleType.RecordSetModule;
    } else if (fileName.equalsIgnoreCase("ExternalConnectionModule")) {
      moduleType = ModuleType.ExternalConnectionModule;
    } else if (fileName.equalsIgnoreCase("ApplicationModule")) {
      moduleType = ModuleType.ApplicationModule;
    } else if (fileName.equalsIgnoreCase("ValueManagerModule")) {
      moduleType = ModuleType.ValueManagerModule;
    } else if (fileName.equalsIgnoreCase("Module")) {
      if (secondFileName.equalsIgnoreCase("Form")) {
        moduleType = ModuleType.FormModule;
      } else {
        moduleType = ModuleType.CommonModule;
      }
    } else {
      LOGGER.error("Module type not find: " + fileName);
    }

    return moduleType;

  }

  public static Map<URI, ModuleType> getModuleTypesByPath(Path rootPath) {

    Map<URI, ModuleType> modulesByType = new HashMap<>();
    String rootPathString = rootPath.toString() + System.getProperty("file.separator");
    Collection<File> files = FileUtils.listFiles(rootPath.toFile(), new String[]{EXTENSION_BSL}, true);
    files.parallelStream().forEach(file -> {
      String[] elementsPath =
          file.toPath().toString().replace(rootPathString, "").split(FILE_SEPARATOR);
      String secondFileName = elementsPath[elementsPath.length - 2];
      String fileName = FilenameUtils.getBaseName(elementsPath[elementsPath.length - 1]);
      ModuleType moduleType = Common.changeModuleTypeByFileName(fileName, secondFileName);
      modulesByType.put(file.toPath().toAbsolutePath().toUri(), moduleType);
    });

    return modulesByType;

  }

  public static Map<URI, SupportVariant> getModuleSupportByPath(AbstractConfiguration configuration, Map<URI, ModuleType> modulesByType) {

    final Path rootPath;
    boolean isEDT = configuration instanceof EDTConfiguration;
    Map<URI, SupportVariant> modulesBySupport = new HashMap<>();

    File fileParentConfiguration;
    if (isEDT) {
      rootPath = Paths.get(configuration.getRootPath().toString(), "src");
      fileParentConfiguration = new File(rootPath.toString(), "Configuration/ParentConfigurations.bin");
    }
    else {
      rootPath = configuration.getRootPath();
      fileParentConfiguration = new File(rootPath.toString(), "Ext/ParentConfigurations.bin");
    }

    if (fileParentConfiguration.exists()) {
      SupportDataConfiguration supportDataConfiguration = new SupportDataConfiguration(fileParentConfiguration.toPath());
      final Map<String, SupportVariant> supportMap = supportDataConfiguration.getSupportMap();

      String rootPathString = rootPath.toString() + System.getProperty("file.separator");
      Collection<File> files = FileUtils.listFiles(rootPath.toFile(), new String[]{EXTENSION_BSL}, true);

      files.parallelStream().forEach(file -> {
        URI uri = file.toPath().toAbsolutePath().toUri();
        String[] elementsPath =
            file.toPath().toString().replace(rootPathString, "").split(FILE_SEPARATOR);

        SupportVariant moduleSupport = null;
        ModuleType moduleType = modulesByType.get(uri);
        String objectGuid = "";
        if (isEDT) {
          objectGuid = getObjectGuidEDT(rootPath, elementsPath, moduleType);
        } else {
          objectGuid = getObjectGuidOriginal(rootPath, elementsPath, moduleType);
        }

        if (objectGuid.isEmpty()) {
          LOGGER.info("Не удалось найти идентфикатор по объекту " + uri.toString());
        }
        else {
          moduleSupport = supportMap.get(objectGuid);
        }

        if (moduleSupport == null) {
          moduleSupport = SupportVariant.NONE;
        }
        modulesBySupport.put(uri, moduleSupport);

      });
    }
    return modulesBySupport;
  }

  private static String getObjectGuidEDT(Path rootPath, String[] elementsPath, ModuleType moduleType) {
    String guid = "";
    Path path = null;
    if (moduleType == ModuleType.ApplicationModule
        || moduleType == ModuleType.ExternalConnectionModule
        || moduleType == ModuleType.ManagedApplicationModule
        || moduleType == ModuleType.OrdinaryApplicationModule
        || moduleType == ModuleType.SessionModule) {

      path = new File(rootPath.toString(), "Configuration/Configuration.mdo").toPath();

    } else {
      String second = elementsPath[elementsPath.length - 3];
      if (second.equalsIgnoreCase("Commands") || (second.equalsIgnoreCase("Forms"))) {
        path = getSimplePath(rootPath, elementsPath, 4, EXTENSION_MDO);
      }
      else {
        path = getSimplePath(rootPath, elementsPath, 2, EXTENSION_MDO);
      }
    }
    return getGuidFromFile(path);
  }

  private static String getObjectGuidOriginal(Path rootPath, String[] elementsPath, ModuleType moduleType) {
    String guid = "";
    Path path;
    if (moduleType == ModuleType.ApplicationModule
        || moduleType == ModuleType.ExternalConnectionModule
        || moduleType == ModuleType.ManagedApplicationModule
        || moduleType == ModuleType.OrdinaryApplicationModule
        || moduleType == ModuleType.SessionModule) {
      path = new File(rootPath.toString(), "Configuration.xml").toPath();
    } else {
      String currentElement = elementsPath[elementsPath.length - 2];
      if (currentElement.equalsIgnoreCase("Ext")) {
        String second = elementsPath[elementsPath.length - 4];
        if (second.equalsIgnoreCase("Commands")) {
          path = getSimplePath(rootPath, elementsPath, 5, EXTENSION_XML);
        } else {
          path = getSimplePath(rootPath, elementsPath, 3, EXTENSION_XML);
        }
      } else if (currentElement.equalsIgnoreCase("Form")) {
        path = getSimplePath(rootPath, elementsPath, 4, EXTENSION_XML);
      } else {
        return guid;
      }
    }
    return getGuidFromFile(path);
  }

  public static String getGuidFromFile(Path path) {
    File xml = path.toFile();
    String guid = "";
    if (xml.exists()) {
      guid = findGuidIntoFileXML(xml);
    }
    return guid;
  }

  public static Path getSimplePath(Path rootPath, String[] elementsPath, int startPosition, String extension) {
    String path = "";
    String lastElement = "";
    int position = 0;
    for (String el : elementsPath) {
      if (position == elementsPath.length - startPosition + 1) {
        break;
      }
      path += el + System.getProperty("file.separator");
      lastElement = el;
      position++;
    }
    if (extension.equals(EXTENSION_XML)) {
      path = path.substring(0, path.length() - 1) + "." + extension;
    } else {
      path = path + lastElement + "." + extension;
    }
    return Paths.get(rootPath.toString(), path).toAbsolutePath();
  }

  public static String findGuidIntoFileXML(File file) {
    String result = "";
    String content = getContentFile(file);
    Matcher matcher = PATTERN_REGEX_GUID.matcher(content);
    if (matcher.find()) {
      result = matcher.group(1);
    }
    return result;
  }

  public static String getContentFile(File file) {
    String content = "";
    try {
      content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    } catch (IOException e) {
      LOGGER.error("Don't read bin file", e);
    }
    return content;
  }

}
