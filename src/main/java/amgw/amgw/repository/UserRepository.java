package amgw.amgw.repository;

import amgw.amgw.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
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

    List<User> findAllByDepartment(String department);

    Page<User> findAllBy(Pageable pageable);

    public interface NcUserView {
        Long getId();
        String getName();
        String getEmail();
        String getDepartment();
        String getUsername(); // Nextcloud userid로 쓸 값
    }
    @Query("""
        select u.id as id,
               u.name as name,
               u.email as email,
               u.department as department,
               u.username as username
        from User u
        where u.status_code='ACTIVE' and u.email_verify_status='VERIFIED'
    """)
    List<NcUserView> findAllForNextcloud();

}
