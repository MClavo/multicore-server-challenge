package dev.mclavo.conduit.tags;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findByName(String name);
    @Query("select t.name from Tag t order by t.name")
    List<String> findAllNames();
}
