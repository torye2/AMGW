package amgw.amgw.repository;

import amgw.amgw.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    Optional<DocumentVersion> findTopByDocIdOrderByVersionDesc(Long docId);
    List<DocumentVersion> findByDocIdOrderByVersionDesc(Long docId);
}

