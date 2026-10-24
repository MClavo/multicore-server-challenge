package dev.mclavo.conduit.articles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    long countByArticleId(long articleId);
    @Modifying
    @Query("delete from Favorite f where f.id = :id")
    int remove(@Param("id") FavoriteId id);
}
