package cz.cvut.fit.vwm.hypertextSearch.web;

import cz.cvut.fit.vwm.hypertextSearch.data.SearchEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    SearchEngine searchEngine;

    @Autowired
    public SearchController(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    @RequestMapping("/")
    public String home(Model model){
        model.addAttribute("name", "name");
        return "index";
    }

    @RequestMapping("/search")
    public String search(Model model, @RequestParam String sourceText){
        model.addAttribute("sourceText",sourceText);
        model.addAttribute("resultSet", searchEngine.search(sourceText,true));
        model.addAttribute("resultSetWithoutPageRank", searchEngine.search(sourceText,false));
        return "results";
    }


}
