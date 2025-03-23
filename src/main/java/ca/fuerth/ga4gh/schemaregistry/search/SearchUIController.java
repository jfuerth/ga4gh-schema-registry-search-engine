package ca.fuerth.ga4gh.schemaregistry.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/ui")
public class SearchUIController {

    @Autowired
    private SearchController searchController;

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("searchResult", null);
        model.addAttribute("query", "");
        return "searchForm";
    }

    @PostMapping("/search")
    public String submitSearchRequest(@RequestParam("q") String query, Model model) {
        SearchResult result = searchController.search(query);
        model.addAttribute("searchResult", result);
        model.addAttribute("query", query);
        return "searchForm";
    }
}