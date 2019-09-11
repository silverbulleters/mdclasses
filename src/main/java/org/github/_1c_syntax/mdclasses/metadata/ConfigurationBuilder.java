package org.github._1c_syntax.mdclasses.metadata;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.github._1c_syntax.mdclasses.CommonTools;
import org.github._1c_syntax.mdclasses.jabx.original.MetaDataObject;
import org.github._1c_syntax.mdclasses.jabx.original.ObjectFactory;
import org.github._1c_syntax.mdclasses.metadata.additional.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigurationBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationBuilder.class.getSimpleName());

    public static final String EXTENSION_BSL = "bsl";
    public static final String FILE_SEPARATOR = Pattern.quote(System.getProperty("file.separator"));

    private ConfigurationSource configurationSource;
    private Path pathToRoot;
    private Path pathToConfig;

    private Configuration configurationMetadata;

    public ConfigurationBuilder(ConfigurationSource configurationSource, Path pathToRoot) {
        this.configurationSource = configurationSource;
        this.pathToRoot = pathToRoot;

        pathToConfig = Paths.get(pathToRoot.toAbsolutePath().toString(), "Configuration.xml");
    }

    public Configuration build() {

        configurationMetadata = new Configuration(configurationSource);

        if (configurationSource == ConfigurationSource.DESIGNER) {

            MetaDataObject mdObject;
            File xml = pathToConfig.toFile();
            try {
                JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
                Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();
                mdObject = (MetaDataObject) ((JAXBElement) jaxbUnmarshaller.unmarshal(xml)).getValue();
            } catch (JAXBException e) {
                LOGGER.error(e.getMessage(), e);
                return null; // TODO: пока так, переделать
            }

            org.github._1c_syntax.mdclasses.jabx.original.Configuration configurationXML = mdObject.getConfiguration();
            fillPropertiesDesigner(configurationXML);
            processConfigurationFilesDesigner();

        } else {

            // в разработке EDT

        }

        return configurationMetadata;
    }

    private void fillPropertiesDesigner(org.github._1c_syntax.mdclasses.jabx.original.Configuration configurationXML) {

        // режим совместимости
        setCompatibilityMode(configurationXML);

        // режим встроенного языка
        setScriptVariant(configurationXML);

    }

    private void processConfigurationFilesDesigner() {

        //HashMap<String, URI> guidsModule = new HashMap<>();

        File fileParentConfiguration = new File(pathToRoot.toString(), "Ext/ParentConfigurations.bin");
        boolean parentConfigurationBinIsFound = fileParentConfiguration.exists();
        Map<String, SupportVariant> supportMap = new HashMap<>();
        if (parentConfigurationBinIsFound) {
            SupportDataConfiguration supportDataConfiguration = new SupportDataConfiguration(fileParentConfiguration.toPath());
            supportMap = supportDataConfiguration.getSupportMap();
        }
        Map<String, SupportVariant> finalSupportMap = supportMap;

        Map<URI, ModuleType> modulesByType = new HashMap<>();
        Map<URI, SupportVariant> modulesBySupport = new HashMap<>();
        String rootPathString = pathToRoot.toString() + System.getProperty("file.separator");
        Collection<File> files = FileUtils.listFiles(pathToRoot.toFile(), new String[]{EXTENSION_BSL}, true);

        Map<String, SupportVariant> finalSupportMap1 = supportMap;
        files.parallelStream().forEach(file -> {
            URI uri = file.toURI();

            // Тип модуля
//            String[] elementsPath =
//                    file.toPath().toString().replace(rootPathString, "").split(FILE_SEPARATOR);
            String[] elementsPath = file.toPath().toString().split(FILE_SEPARATOR);
            String secondFileName = elementsPath[elementsPath.length - 2];
            String fileName = FilenameUtils.getBaseName(elementsPath[elementsPath.length - 1]);
            ModuleType moduleType = changeModuleTypeByFileName(fileName, secondFileName);
            modulesByType.put(uri, moduleType);

            // Поддержка модуля
            SupportVariant moduleSupport = null;
            if (parentConfigurationBinIsFound) {
                String objectGuid = getObjectGuid(uri, elementsPath, moduleType);
                if (!objectGuid.equals("")) {
                    moduleSupport = finalSupportMap1.get(objectGuid);
                }
            }
            if (moduleSupport == null) {
                moduleSupport = SupportVariant.NONE;
            }
            modulesBySupport.put(uri, moduleSupport);

        });

        configurationMetadata.setModulesByType(modulesByType);
        configurationMetadata.setModulesBySupport(modulesBySupport);

    }

    private String getObjectGuid(URI uri, String[] elementsPath, ModuleType moduleType) {
        String guid = "";
        String path = "";
        if (moduleType == ModuleType.ApplicationModule
                || moduleType == ModuleType.ExternalConnectionModule
                || moduleType == ModuleType.ManagedApplicationModule
                || moduleType == ModuleType.OrdinaryApplicationModule
                || moduleType == ModuleType.SessionModule) {
            path = new File(pathToRoot.toString(), "Configuration.xml").toPath().toString();
            guid = CommonTools.getGuidByFile(path);
        } else {
            String currentElement = elementsPath[elementsPath.length - 2];
            if (currentElement.equalsIgnoreCase("Ext")) {
                String second = elementsPath[elementsPath.length - 4];
                if (second.equalsIgnoreCase("Commands")) {
                    path = CommonTools.getSimplePath(elementsPath, 5);
                } else {
                    path = CommonTools.getSimplePath(elementsPath, 3);
                }
                guid = CommonTools.getGuidByFile(path);
            } else if (currentElement.equalsIgnoreCase("Form")) {
                path = CommonTools.getSimplePath(elementsPath, 4);
                guid = CommonTools.getGuidByFile(path);
            } else {
                LOGGER.info("Не найден идентификатор файла: " + uri.toString());
            }
        }
        return guid;
    }

//    private HashMap<String, URI> getGuidModule() {
//
//    }

    private void setCompatibilityMode(org.github._1c_syntax.mdclasses.jabx.original.Configuration configurationXML) {
        CompatibilityMode compatibilityMode;
        org.github._1c_syntax.mdclasses.jabx.original.CompatibilityMode configurationExtensionCompatibilityMode =
                configurationXML.getProperties().getConfigurationExtensionCompatibilityMode();
        if (configurationExtensionCompatibilityMode == null) {
            compatibilityMode = new CompatibilityMode(0, 0);
        } else {
            compatibilityMode = new CompatibilityMode(configurationExtensionCompatibilityMode.name());
        }
        configurationMetadata.setCompatibilityMode(compatibilityMode);
    }

    private void setScriptVariant(org.github._1c_syntax.mdclasses.jabx.original.Configuration configurationXML) {
        String scriptVariantString = configurationXML.getProperties().getScriptVariant().name().toUpperCase();
        configurationMetadata.setScriptVariant(ScriptVariant.valueOf(scriptVariantString));

    }

    private ModuleType changeModuleTypeByFileName(String fileName, String secondFileName) {
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

}
