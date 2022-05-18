package cz.cvut.fit.vwm.hypertextSearch.data;

import java.util.Objects;

public class Result implements Comparable<Result> {
    private final WebSite site;
    private final Double PageRank;
    private final Float Score;
    public Double FinalScore;

    public Result(WebSite site, Double pageRank, Float score) {
        this.site = site;
        this.PageRank = pageRank;
        this.Score = score;
        FinalScore = (pageRank + score) / 2;
    }

    public WebSite getSite() {
        return site;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;
        Result result = (Result) o;
        return getSite().equals(result.getSite());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSite());
    }

    @Override
    public int compareTo(Result o) {
        return this.FinalScore.compareTo(o.FinalScore);
    }

    public Double getFinalScore() {
        return FinalScore;
    }

    public Double getPageRank() {
        return PageRank;
    }

    public Float getScore() {
        return Score;
    }

    @Override
    public String toString() {
        return "Result{" +
                "site=" + site +
                ", PageRank=" + PageRank +
                ", Score=" + Score +
                ", FinalScore=" + FinalScore +
                '}';
    }
}
