package ca.fuerth.ga4gh.schemaregistry.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchUIController {

    @Autowired
    private SearchController searchController;

    @GetMapping("/")
    public String submitSearchRequest(@RequestParam(name="q", required = false) String query, Model model) {
        SearchResult result = null;
        if (StringUtils.hasText(query)) {
            result = searchController.search(query);
        }
        model.addAttribute("searchResult", result);
        model.addAttribute("query", query);
        return "searchForm";
    }
}