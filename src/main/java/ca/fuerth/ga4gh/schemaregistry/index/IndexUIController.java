package ca.fuerth.ga4gh.schemaregistry.index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@Controller
@RequestMapping("/ui")
public class IndexUIController {

    @Autowired
    private IndexController indexController;

    @GetMapping("/index")
    public String showIndexForm(Model model) {
        model.addAttribute("indexingResult", null);
        return "indexForm";
    }

    @PostMapping("/index")
    public String submitIndexRequest(@RequestParam("registryUri") URI registryUri,
                                     @RequestParam(value = "includeNamespaces", required = false) String includeNamespaces,
                                     Model model) {
        
        IndexingResult result = indexController.addRegistryToIndex(registryUri, includeNamespaces);
        model.addAttribute("indexingResult", result);
        
        return "indexForm";
    }
}