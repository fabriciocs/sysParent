package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Ride;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Ride entity.
 */
@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    default Optional<Ride> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Ride> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Ride> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select ride from Ride ride left join fetch ride.child left join fetch ride.driver",
        countQuery = "select count(ride) from Ride ride"
    )
    Page<Ride> findAllWithToOneRelationships(Pageable pageable);

    @Query("select ride from Ride ride left join fetch ride.child left join fetch ride.driver")
    List<Ride> findAllWithToOneRelationships();

    @Query("select ride from Ride ride left join fetch ride.child left join fetch ride.driver where ride.id =:id")
    Optional<Ride> findOneWithToOneRelationships(@Param("id") Long id);
}
