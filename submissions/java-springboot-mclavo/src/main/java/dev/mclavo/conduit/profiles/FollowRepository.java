package dev.mclavo.conduit.profiles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    @Modifying
    @Query("delete from Follow f where f.id = :id")
    int remove(@Param("id") FollowId id);
}
