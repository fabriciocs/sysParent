package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Child;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Child entity.
 */
@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {
    default Optional<Child> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Child> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Child> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select child from Child child left join fetch child.parent", countQuery = "select count(child) from Child child")
    Page<Child> findAllWithToOneRelationships(Pageable pageable);

    @Query("select child from Child child left join fetch child.parent")
    List<Child> findAllWithToOneRelationships();

    @Query("select child from Child child left join fetch child.parent where child.id =:id")
    Optional<Child> findOneWithToOneRelationships(@Param("id") Long id);
}
