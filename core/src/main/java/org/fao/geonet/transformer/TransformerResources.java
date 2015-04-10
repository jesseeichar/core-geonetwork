package org.fao.geonet.transformer;

import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.WeakHashMap;

import static com.google.common.io.Files.getNameWithoutExtension;

/**
 * @author Jesse on 4/10/2015.
 */
public class TransformerResources {

    @Autowired
    private IsoLanguagesMapper isoLanguagesMapper;

    /**
     * Map (canonical path to formatter dir -> Element containing all xml files in Formatter bundle's loc directory)
     */
    private WeakHashMap<String, Element> pluginLocs = new WeakHashMap<>();
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SystemInfo systemInfo;

    /**
     * Get the localization files from current format plugin.  It will load all xml file in the loc/lang/ directory as children
     * of the returned element.
     */
    public synchronized Element getPluginLocResources(Path formatDir, String lang) throws Exception {
        final Element pluginLocResources = getPluginLocResources(formatDir);
        Element translations = pluginLocResources.getChild(lang);
        if (translations == null) {
            if (pluginLocResources.getChildren().isEmpty()) {
                translations = new Element(lang);
            } else {
                translations = (Element) pluginLocResources.getChildren().get(0);
            }
        }
        return translations;
    }
    public synchronized Element getPluginLocResources(Path formatDir) throws Exception {
        final String formatDirPath = formatDir.toString();
        Element allLangResources = this.pluginLocs.get(formatDirPath);
        if (systemInfo.isDevMode() || allLangResources == null) {
            allLangResources = new Element("loc");
            Path baseLoc = formatDir.resolve("loc");
            if (Files.exists(baseLoc)) {
                final Element finalAllLangResources = allLangResources;
                Files.walkFileTree(baseLoc, new SimpleFileVisitor<Path>(){
                    private void addTranslations(String locDirName, Element fileElements) {
                        if (locDirName != null && !locDirName.isEmpty()) {
                            Element resources = finalAllLangResources.getChild(locDirName);
                            if (resources == null) {
                                resources = new Element(locDirName);
                                finalAllLangResources.addContent(resources);
                            }
                            resources.addContent(fileElements);
                        }
                    }
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().toLowerCase().endsWith(".xml")) {
                            try {
                                final Element fileElements = Xml.loadFile(file);
                                final String fileName = getNameWithoutExtension(file.getFileName().toString());
                                fileElements.setName(fileName);
                                final String locDirName = getNameWithoutExtension(file.getParent().getFileName().toString());
                                addTranslations(locDirName, fileElements);
                            } catch (JDOMException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (file.getFileName().toString().toLowerCase().endsWith(".json")) {
                            try {
                                final String fileName = getNameWithoutExtension(file.getFileName().toString());
                                final String[] nameParts = fileName.split("-", 2);
                                String lang = isoLanguagesMapper.iso639_1_to_iso639_2(nameParts[0].toLowerCase(), nameParts[0]);
                                final JSONObject json = new JSONObject(new String(Files.readAllBytes(file), Constants.CHARSET));
                                Element fileElements = new Element(nameParts[1]);
                                final Iterator keys = json.keys();
                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    fileElements.addContent(new Element(key).setText(json.getString(key)));
                                }
                                addTranslations(lang, fileElements);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return super.visitFile(file, attrs);
                    }
                });
            }

            this.pluginLocs.put(formatDirPath, allLangResources);
        }
        return allLangResources;
    }

    public void setIsoLanguagesMapper(IsoLanguagesMapper isoLanguagesMapper) {
        this.isoLanguagesMapper = isoLanguagesMapper;
    }

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }
}

