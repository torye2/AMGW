package amgw.amgw.repository;

import amgw.amgw.entity.DocumentShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentShareRepository extends JpaRepository<DocumentShare, Long> {
    List<DocumentShare> findByDocId(Long docId);
    Optional<DocumentShare> findByLinkToken(String token);
}

