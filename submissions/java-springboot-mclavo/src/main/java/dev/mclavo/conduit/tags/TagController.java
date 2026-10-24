package dev.mclavo.conduit.tags;

import dev.mclavo.conduit.tags.dto.TagsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tags;
    public TagController(TagService tags) { this.tags = tags; }
    @GetMapping public TagsResponse list() { return tags.list(); }
}
