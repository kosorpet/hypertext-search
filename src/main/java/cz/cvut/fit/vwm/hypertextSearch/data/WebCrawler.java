package cz.cvut.fit.vwm.hypertextSearch.data;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements Runnable {
    private static final int MAX_DEPTH = 3;
    private static final int MAX_COUNT = 1000;
    private static final AtomicInteger siteCount = new AtomicInteger(0);
    private final ArrayList<Thread> threads = new ArrayList<>();
    private final String firstLink;
    private final HashSet<String> visitedLinks = new HashSet<>();
    private final HashSet<WebSite> savedSites = new HashSet<>();
    private final HashMap<String,WebSite> linkedSites = new HashMap<>();
    private final HashMap<Integer,WebSite> idSites = new HashMap<>();


    public WebCrawler( String link, Integer threadNum) {
        firstLink = link;
        System.out.println("Web crawler created. [starting link: " + firstLink + "]");

        //Add first site
        Document doc = request(firstLink);
        if(doc == null){
            System.err.println("Invalid starting link.");
            return;
        }
        WebSite startSite = new WebSite(doc, getNextUniqueIndex());

        savedSites.add(startSite);
        visitedLinks.add(firstLink);
        linkedSites.put(firstLink, startSite);
        idSites.put(startSite.getID(), startSite);

        for (int i = 0; i < threadNum; i++) {
            Thread thread = new Thread(this);
            this.threads.add(thread);
            thread.start();
        }
        try{
            for(Thread thread: threads){
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        crawl(1, firstLink);
    }

    private void crawl(int depth, String url) {
        if (depth <= MAX_DEPTH && siteCount.get() < MAX_COUNT) {
            Document doc = request(url);

            if (doc != null) {
                //Create new website
                if(!Objects.equals(url, firstLink)) {
                    WebSite newSite = new WebSite(doc, 0);
                    if(!savedSites.contains(newSite)){
                        newSite.setID(getNextUniqueIndex());
                        savedSites.add(newSite);
                        linkedSites.put(newSite.getUrl(), newSite);
                        idSites.put(newSite.getID(), newSite);
                    }
                }
                //Delete top, bottom and side menu
                cleanPage(doc);

                //Loop through all the link tags
                for (Element link : doc.select("a[href]")) {
                    //Get link from tag
                    String nextLink = link.absUrl("href");
                    if (!visitedLinks.contains(nextLink)) {
                        crawl(depth + 1, nextLink);
                    }
                }
            }
        }
    }

    private Document request(String url) {
        visitedLinks.add(url);
        try {
            Connection con = Jsoup.connect(url);
            //Get HTML document
            Document doc = con.get();

            //Check if connection was successful
            if (con.response().statusCode() == 200) {
                System.out.println("**Crawler| Recieved page at: " + url);
                //System.out.println(doc.title());
                return doc;
            }
            return null;
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    private void cleanPage(Document doc) {
        Element sideMenu = doc.getElementById("mw-panel");
        if(sideMenu != null){
            sideMenu.remove();
        }
        Element topMenu = doc.getElementById("mw-head");
        if(topMenu != null){
            topMenu.remove();
        }

        Element footer = doc.getElementById("footer");
        if(footer != null){
            footer.remove();
        }
        Element catlinks = doc.getElementById("catlinks");
        if(catlinks != null){
            catlinks.remove();
        }
        doc.getElementsByClass("reflist").remove();
        doc.getElementsByClass("mw-indicators").remove();
        doc.getElementsByAttributeValue("title", "Wikipedia:Citation needed").remove();
        doc.getElementsByAttributeValue("title", "Wikipedia:Protection policy#semi").remove();
        doc.getElementsByAttributeValue("title", "ISBN (identifier)").remove();
        doc.getElementsByAttributeValue("title", "Wikipedia:Verifiability").remove();
        doc.getElementsByAttributeValue("title", "Creative Commons").remove();
        doc.getElementsByAttributeValue("href", "https://commons.wikimedia.org/wiki/Main_Page").remove();
        doc.getElementsByAttributeValue("href", "https://en.wikipedia.org/wiki/ISBN_(identifier)").remove();
        doc.select("a[href~=cite_note]").remove(); //citation notes
        doc.getElementsByClass("mbox-text").remove();

    }

    public ArrayList<Thread> getThreads() {
        return threads;
    }

    public HashSet<WebSite> getSavedSites() {
        return savedSites;
    }

    public HashMap<String, WebSite> getLinkedSites() {
        return linkedSites;
    }

    private int getNextUniqueIndex() {
        return siteCount.getAndIncrement();
    }

    public HashMap<Integer, WebSite> getIdSites() {
        return idSites;
    }
}

