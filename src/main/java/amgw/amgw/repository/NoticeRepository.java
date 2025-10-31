package amgw.amgw.repository;

import amgw.amgw.dto.NoticeDto;
import amgw.amgw.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice,Long> {
    List<Notice> findTop5ByOrderByRegistrationTimeDesc();
}
