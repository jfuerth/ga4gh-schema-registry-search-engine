package ca.fuerth.ga4gh.schemaregistry.index;

import ca.fuerth.ga4gh.schemaregistry.crawler.Crawler;
import ca.fuerth.ga4gh.schemaregistry.crawler.CrawledSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class IndexController {

    @Autowired
    Crawler crawler;

    @Autowired
    LangChainIndexer indexer;

    @PostMapping("/index")
    public IndexingResult addRegistryToIndex(@RequestParam URI registryUri,
                                             @RequestParam(required = false) String includeNamespaces) {
        Predicate<String> namespaceFilter;
        if (includeNamespaces != null && !includeNamespaces.isBlank()) {
            namespaceFilter = Pattern.compile(includeNamespaces).asMatchPredicate();
        } else {
            namespaceFilter = s -> true;
        }
        Stream<CrawledSchema> crawledSchemas = crawler.crawl(registryUri, namespaceFilter);
        return indexer.addToIndex(crawledSchemas);
    }
}
