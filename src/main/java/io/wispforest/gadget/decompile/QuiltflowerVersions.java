package io.wispforest.gadget.decompile;

import io.wispforest.gadget.Gadget;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public final class QuiltflowerVersions {
    private static final String MAVEN_METADATA_URL
        = "https://maven.quiltmc.org/repository/release/org/quiltmc/quiltflower/maven-metadata.xml";
    private static List<String> VERSIONS = null;
    private static String LATEST_VERSION = null;

    private QuiltflowerVersions() {

    }

    private static Element child(Element element, String name) {
        NodeList children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            var node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            if (node.getNodeName().equals(name))
                return (Element) node;
        }

        return null;
    }

    public static List<String> versions() {
        if (VERSIONS == null) {
            try {
                var conn = new URL(MAVEN_METADATA_URL).openConnection();
                try (var is = new BufferedInputStream(conn.getInputStream())) {
                    var root = DocumentBuilderFactory.newDefaultInstance()
                        .newDocumentBuilder()
                        .parse(is)
                        .getDocumentElement();

                    var versioning = child(root, "versioning");
                    var versionsEl = child(versioning, "versions");
                    var children = versionsEl.getChildNodes();
                    var versions = new ArrayList<String>();

                    for (int i = 0; i < children.getLength(); i++) {
                        var child = children.item(i);

                        versions.add(child.getTextContent());
                    }

                    VERSIONS = Collections.unmodifiableList(versions);

                    var latestEl = child(versioning, "latest");
                    LATEST_VERSION = latestEl.getTextContent();
                }
            } catch (IOException | ParserConfigurationException | SAXException e) {
                throw new RuntimeException(e);
            }
        }

        return VERSIONS;
    }

    public static String effectiveVersion() {
        String v = Gadget.CONFIG.quiltflowerVersion();

        versions();

        if (v.equals("LATEST")) {
            return LATEST_VERSION;
        } else {
            return v;
        }
    }
}
