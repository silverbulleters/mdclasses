package org.github._1c_syntax.mdclasses;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonTools {

    public static final String REGEX_GUID = "uuid=\"(.*)\"";
    public static final Pattern PATTERN_REGEX_GUID = Pattern.compile(REGEX_GUID, Pattern.MULTILINE);
    public static final String EXTENSION_XML = "xml";

    public static String getGuidByFile(String path) {
        File xml = new File(path);
        String guid = "";
        if (xml.exists()) {
            guid = findGuidIntoFileXML(xml);
        }
        return guid;
    }

    public static String getSimplePath(String[] elementsPath, int startPosition) {
        String path = "";
        int position = 0;
        for (String el : elementsPath) {
            if (position == elementsPath.length - startPosition + 1) {
                break;
            }
            path += el + System.getProperty("file.separator");
            position++;
        }
        path = path.substring(0, path.length() - 1) + "." + EXTENSION_XML;
        return path;
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
            content = readFile(file.toPath());
        } catch (Exception e) {

        }
        return content;
    }

    public static String readFile(Path path) {
        String result = "";
        try {
            result = new String(Files.readAllBytes(Paths.get(path.toUri())), StandardCharsets.UTF_8);
        } catch (IOException e) {
            //LOGGER.error("Don't read bin file", e);
        }
        return result;

    }

}
