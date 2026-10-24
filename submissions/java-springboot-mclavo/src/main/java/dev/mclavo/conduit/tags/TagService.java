package dev.mclavo.conduit.tags;

import dev.mclavo.conduit.tags.dto.TagsResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tags;
    public TagService(TagRepository tags) { this.tags = tags; }
    public TagsResponse list() { return new TagsResponse(tags.findAllNames()); }

    @Transactional(propagation = Propagation.MANDATORY)
    public Set<Tag> resolve(List<String> names) {
        Set<Tag> result = new LinkedHashSet<>();
        if (names != null) {
            for (String name : new TreeSet<>(names)) {
                result.add(tags.findByName(name).orElseGet(() -> tags.saveAndFlush(new Tag(name))));
            }
        }
        return result;
    }
}
