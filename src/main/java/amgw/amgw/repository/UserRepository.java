package amgw.amgw.repository;

import amgw.amgw.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("""
        select u from User u
        where lower(u.name) like lower(:kw)
           or lower(u.username) like lower(:kw)
        order by u.name asc
    """)
    Page<User> searchByNameOrUsername(@Param("kw") String kw, Pageable pageable);

    @Query("select u.username from User u where u.id = :userId")
    String findUsernameById(@Param("userId") Long userId);

    @Query("select u.name from User u where u.id = :userId")
    String findNameById(@Param("userId") Long userId);
}
