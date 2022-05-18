package cz.cvut.fit.vwm.hypertextSearch.data;

import org.apache.lucene.search.ScoreDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Component
public class SearchEngine {

    private final LocalDatabase database;
    private final WebSiteIndex index;

    @Autowired
    public SearchEngine() {
        String link = "https://en.wikipedia.org/wiki/Main_Page/";
        Integer threads = 6;

        WebCrawler bot;
        bot = new WebCrawler(link, threads);

        this.database = new LocalDatabase(bot.getSavedSites(), bot.getLinkedSites(), bot.getIdSites());
        this.index = new WebSiteIndex(this.database);
    }

    private List<Result> aggregate(List<ScoreDoc> resultDocuments, Collection<WebSite> sites, boolean pageRank) {
        List<Result> aggregated = new ArrayList<>();

        //Find max and min query score
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (ScoreDoc doc : resultDocuments) {
            if (doc.score > max) {
                max = doc.score;
            }
            if (doc.score < min) {
                min = doc.score;
            }
        }

        for (ScoreDoc doc : resultDocuments) {
            String url = index.scoreDoctoDoc(doc).getField("url").stringValue();
            WebSite site = database.getSiteByLink(url);
            //Add score normalized to (0,1)
//            Double finalScore = pageRank ? (doc.score - min) / (max - min) : site.getPagerank();
            Float score = (doc.score - min) / (max - min);
            aggregated.add(new Result(site, pageRank ? site.getPagerank() : score, score));
         }
        Collections.sort(aggregated);
        Collections.reverse(aggregated);

        return aggregated;
    }

    public List<Result> search(String pattern, boolean pageRank) {

        List<ScoreDoc> resultDocuments = this.index.query("body", pattern);

        return aggregate(resultDocuments, database.getSites(), pageRank);
    }
}
