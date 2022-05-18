package cz.cvut.fit.vwm.hypertextSearch.data;

import org.jsoup.nodes.Element;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class LocalDatabase {
    private final HashSet<WebSite> sites;
    private final HashMap<String, WebSite> linkedSites;
    private final HashMap<Integer, WebSite> idSites;
    //Alpha is a paramater used in matrix calculation
    //https://moodle-vyuka.cvut.cz/pluginfile.php/493967/course/section/76282/BIVWM_lecture04.pdf slide 25
    private static final double ALPHA = 0.85;
    SimpleMatrix linkMatrix;

    public LocalDatabase(HashSet<WebSite> sites, HashMap<String, WebSite> linkSites, HashMap<Integer, WebSite> idSites) {
        this.sites = sites;
        this.linkedSites = linkSites;
        this.idSites = idSites;
        this.linkMatrix = new SimpleMatrix(sites.size(), sites.size());
        this.init();
    }

    private void init(){
        //Find neighbors for every site, save them in neighbour list
        System.out.println("**Database| Updating neighbour lists...");
        for (WebSite site : this.sites) {
            addNeighbors(site);
        }
        System.out.println("**Database| Building link matrix...");
        buildLinkMatrix();
        System.out.println("**Database| Calculating page rank...");
        calculatePageRank();
    }

    private void buildLinkMatrix(){
        //Fill with values
        for (WebSite site : this.sites) {
            //If website has no neighbors (outlinks) set all the values in the row to 1 / (number of all sites)
            if(site.getNeighbors().isEmpty()){
                for(int i = 0; i < this.sites.size(); i++){
                    linkMatrix.set(site.getID(), i, 1 / (double) this.sites.size());
                }
                continue;
            }
            //Fill matrix row with values
            for (WebSite neighbor: site.getNeighbors()){
                linkMatrix.set(site.getID(), neighbor.getID(), 1 / (double) site.getNeighbors().size());
            }
        }
        //Multiply each value in matrix with the parameter ALPHA, add (1 - ALPHA) * (1 / (number of all sites))
        //There will be no zeros in the matrix
        for(int i = 0; i < sites.size(); i++){
            for(int j = 0; j < sites.size(); j++){
                linkMatrix.set(i, j, linkMatrix.get(i, j) * ALPHA + ((1 - ALPHA) * (1 / (double) sites.size())));
            }
        }

    }

    private void addNeighbors(WebSite siteToAdd) {
        //Get link from tag
        for (Element link : siteToAdd.getHtml().select("a[href]")) {
            String linkString = link.absUrl("href");
            //If site is in database, add it as a neighbor
            if(linkedSites.containsKey(linkString)){
                siteToAdd.addNeighbor(linkedSites.get(linkString));
            }
        }
    }

    private void calculatePageRank(){
        //Create initial page rank vector
        SimpleMatrix prVector = new SimpleMatrix(sites.size(), 1);
        for(int i = 0; i < sites.size(); i++){
            prVector.set(i , 1);
        }
        prVector = prVector.transpose();
        //Iterate while previous iteration does not equal current
        SimpleMatrix prevIteration;
        do {
            prevIteration = prVector;
            prVector = prVector.mult(linkMatrix);
        }while(!prevIteration.isIdentical(prVector, 0.0001d));

        //Set page rank for each site, normalize to (0, 1)
        double min = prVector.elementMinAbs();
        double max = prVector.elementMaxAbs();
        for(int i = 0; i < sites.size(); i++){
            //getSiteByID(i).setPagerank(prVector.get(i));
            //getSiteByID(i).setPagerank((prVector.get(i) - min) / (max - min));
            getSiteByID(i).setPagerank(Math.log(1 + prVector.get(i))/Math.log(1+max));
        }

//        for(WebSite site: sites){
//            System.out.println(site.getUrl() + " | Page rank: " + site.getPagerank());
//        }
    }

    public WebSite getSiteByID(int i) {
        return idSites.get(i);
    }

    public WebSite getSiteByLink(String url){
        return linkedSites.get(url);
    }

    public HashSet<WebSite> getSites() {
        return sites;
    }
}
