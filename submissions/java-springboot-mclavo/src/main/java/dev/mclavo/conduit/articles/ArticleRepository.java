package dev.mclavo.conduit.articles;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    String FILTERS = """
            where (:tag is null or exists (select t.id from Article tagged join tagged.tags t
                    where tagged.id = a.id and t.name = :tag))
              and (:author is null or a.author.username = :author)
              and (:favorited is null or exists (select f.id from Favorite f
                    where f.article = a and f.user.username = :favorited))
              and (:feedViewer is null or exists (select fo.id from Follow fo
                    where fo.follower.id = :feedViewer and fo.followee = a.author))
            """;

    @EntityGraph(attributePaths = "author")
    Optional<Article> findBySlug(String slug);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Article a where a.slug = :slug")
    Optional<Article> findBySlugForUpdate(@Param("slug") String slug);

    @EntityGraph(attributePaths = "author")
    @Query("select a from Article a " + FILTERS)
    List<Article> findArticles(@Param("tag") String tag, @Param("author") String author,
            @Param("favorited") String favorited, @Param("feedViewer") Long feedViewer, Pageable pageable);

    // A bounded identifier query gives the specified capped count without counting the whole table.
    @Query("select a.id from Article a " + FILTERS)
    List<Long> findCountCandidates(@Param("tag") String tag, @Param("author") String author,
            @Param("favorited") String favorited, @Param("feedViewer") Long feedViewer, Pageable pageable);

    @Query("select a.id from Article a " + FILTERS + " order by a.id desc")
    Stream<Long> streamArticleIds(@Param("tag") String tag, @Param("author") String author,
            @Param("favorited") String favorited, @Param("feedViewer") Long feedViewer);

    @Query("select t.name from Article a join a.tags t where a.id = :id order by t.name")
    List<String> findTagNames(@Param("id") long id);
}
