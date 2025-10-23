package amgw.amgw.repository;

import amgw.amgw.entity.Documents;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentsRepository extends JpaRepository<Documents, Long> {
    Page<Documents> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId, Pageable pageable);

}
