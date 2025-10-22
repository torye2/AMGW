// src/main/java/amgw/amgw/search/SearchController.java
package amgw.amgw.controller;

import amgw.amgw.dto.SearchResult;
import amgw.amgw.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public List<SearchResult> search(@RequestParam("q") String q) {
        return searchService.search(q);
    }
}
