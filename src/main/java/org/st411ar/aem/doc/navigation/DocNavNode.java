package org.st411ar.aem.doc.navigation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DocNavNode {
    private static final String EXTENSION = ".html";
    private static final String URL_BASE = "https://docs.adobe.com";
    private static final String ROOT_CONTENT = URL_BASE
            + "/content/docs/jcr:content/sidebar-for-navigation.html?sec="
            + "/content";

    public static final String AEM = "/docs/en/aem/6-2";
    private static final String AEM_HTL = AEM + "/develop/sightly";
    private static final String HTL = "/docs/en/htl";

    private String path;
    private String title;
    private int volume;
    private List<DocNavNode> children = new ArrayList<DocNavNode>();


    DocNavNode(String nodePath, String nodeTitle) {
        setPath(nodePath);
        title = nodeTitle;
        try {
            if (AEM.equals(path) || HTL.equals(path)) {
                updateChildren();
            } else {
                Document document = getDocument(URL_BASE + path + EXTENSION);
                initVolume(document);
                updateChildren(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getPath() {
        return path;
    }

    public String getUrl() {
        return URL_BASE + path + EXTENSION;
    }

    public String getTitle() {
        return title;
    }

    public int getVolume() {
        return volume;
    }

    public List<DocNavNode> getChildren() {
        return children;
    }


    public static DocNavNode buildNavigation() {
        return new DocNavNode(AEM, "AEM Documentation Navigation");
    }


    public int size() {
        int size = 1;
        for (DocNavNode node : children) {
            size += node.size();
        }
        return size;
    }

    public int fullVolume() {
        int fullVolume = volume;
        for (DocNavNode node : children) {
            fullVolume += node.fullVolume();
        }
        return fullVolume;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(buildMargin());
        sb.append(path);
        sb.append(" : ");
        sb.append(title);
        sb.append(" has ");
        sb.append(volume);
        sb.append(" symbols and contains ");
        sb.append(size() - 1);
        sb.append(" subnodes with ");
        sb.append(fullVolume() - volume);
        sb.append(" symbols\n");
        for (DocNavNode node : children) {
            sb.append(node);
        }
        return sb.toString();
    }


    private void setPath(String nodePath) {
        path = AEM_HTL.equals(nodePath) ? HTL : nodePath;
    }

    private Document getDocument(String path) throws IOException {
//        System.out.println("getDocument " + path);
        return Jsoup.connect(path).validateTLSCertificates(false).get();
    }

    private void initVolume(Document doc) throws IOException {
        for (Element contentBody : doc.getElementsByClass("content-body")) {
            volume += contentBody.text().trim().replaceAll("\\s", "").length();
        }
    }

    private void updateChildren() throws IOException {
        List<DocNavNode> children = new ArrayList<DocNavNode>();
        for (Element child : fetchChildrenElements()) {
            DocNavNode childNode = buildNode(child);
            children.add(childNode);
        }
        this.children = children;
    }

    private Elements fetchChildrenElements() throws IOException {
        String contentPath = ROOT_CONTENT + path;
        Element element = getDocument(contentPath).getElementById("accordion");
        if (element != null) {
            Elements children = element.children();
            if (children.size() == 2) {
                return children.last().children();
            }
        }
        return new Elements();
    }

    private void updateChildren(Document document) throws IOException {
        List<DocNavNode> children = new ArrayList<DocNavNode>();
        for (Element child : getChildrenElements(document)) {
            DocNavNode childNode = buildNode(child);
            children.add(childNode);
        }
        this.children = children;
    }

    private Elements getChildrenElements(Document doc) {
        String path = this.path + EXTENSION;
        Elements links = doc.getElementsByAttributeValue("href", path);
        if (links.size() == 1) {
            Elements description = links.first().parent().parent().children();
            if (description.size() > 1) {
                return description.last().children();
            }
        }
        return new Elements();
    }

    private DocNavNode buildNode(Element element) throws IOException {
        Element link = element.children().first().getElementsByTag("a").first();
        String href = link.attr("href");
        String path = href.substring(0, href.length() - EXTENSION.length());
        String title = link.text();
        return new DocNavNode(path, title);
    }

    private StringBuilder buildMargin() {
        StringBuilder sb = new StringBuilder();
        String[] path = this.path.split("/");
        int baseMargin = 0;
        int rootPathDepth = 5;
        if ("htl".equals(path[3])) {
            baseMargin += 2;
            rootPathDepth--;
        }
        for (int i = rootPathDepth; i < baseMargin + path.length; i++) {
            sb.append("\t");
        }
        return sb;
    }
}