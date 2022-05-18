package cz.cvut.fit.vwm.hypertextSearch.data;


import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.util.HashSet;
import java.util.Objects;

public class WebSite {
    private final String url;
    private final Document html;
    private final String plaintext;
    private final HashSet<WebSite> neighbors = new HashSet<>();

    private int ID;

    private double pagerank;

    public String getView(Integer length){
        return StringUtils.substring(plaintext,0,length);
    }

    public String getName(){
        return html.title();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebSite webSite = (WebSite) o;
        return url.equals(webSite.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    public WebSite(Document html, int id) {
        this.url = html.location();
        this.html = html;
        this.plaintext = html.text();
        this.ID = id;
    }

    public void addNeighbor(WebSite site){
        neighbors.add(site);
    }

    public String getUrl() {
        return url;
    }


    public String getPlaintext() {
        return plaintext;
    }

    public int getID() {
        return ID;
    }

    public HashSet<WebSite> getNeighbors() {
        return neighbors;
    }

    public Document getHtml() {
        return html;
    }

    public int getOutlinksCount(){
        return neighbors.size();
    }

    public void setPagerank(double pagerank) {
        this.pagerank = pagerank;
    }

    public double getPagerank() {
        return pagerank;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
