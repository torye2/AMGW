package amgw.amgw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import amgw.amgw.dto.ComplimentDto;
import amgw.amgw.dto.NoticeDto;
import amgw.amgw.mapper.ComplimentMapper;
import amgw.amgw.mapper.NoticeMapper;

@SpringBootTest
public class YuJinApplicationTests {

	@Autowired 
	private NoticeMapper noticeMapper;
	
	@Autowired
	private ComplimentMapper complimentMapper;
	
	@Test 
    void testinsertNotice() { 
    	
    	NoticeDto not = new NoticeDto(); 
    	not.setFile_id((long) 0);
    	not.setNotice_count(0);
    	not.setNotice_title("테스트 공지사항");
    	not.setNotice_detail("테스트 공지사항 내용");
    	not.setUsername("gwapp");
    	not.setRegistration_time(Timestamp.valueOf(LocalDateTime.now()));
    	not.setImportant(true);
    	
    	int result = noticeMapper.insertNotice(not); 
    	assertThat(result).isEqualTo(1); 
	} 
	
	@Test
	void testSelectAllNotice() {
		List<NoticeDto> notices = noticeMapper.selectAllNotices(0, 10);
		assertThat(notices).isNotNull();
		System.out.println("전체 공지사항 목록:");
			for (NoticeDto n : notices) {
				System.out.println(n.getDisplayNumber() + "/" + n.getNotice_title());
			}
		
	}
	
	@Test
	void testMakeAsImportant() {
		int noticeId = 1;
		int updated = noticeMapper.makeAsImportant(noticeId);
		assertThat(updated).isEqualTo(1);
		System.out.println("공지사항" + noticeId + "번이 중요로 설정되었습니다.");
	}
	
	@Test
    void testMapperNotNull() {
        // Mapper가 DI 되었는지 확인
        assertNotNull(complimentMapper, "ComplimentMapper가 DI되지 않았습니다.");
    }

    @Test
    void testInsertAndSelect() {
        // 테스트용 게시글 생성
        ComplimentDto dto = new ComplimentDto();
        dto.setUser_id(1L); // users 테이블에 있는 user_id
        dto.setPassword("1234");
        dto.setCompliment_count(0);
        dto.setCompliment_title("테스트 제목");
        dto.setCompliment_detail("테스트 내용");

        // INSERT
        int insertResult = complimentMapper.insertCompliment(dto);
        assert(insertResult > 0);

        // SELECT (방금 넣은 게시글 조회)
        ComplimentDto selected = complimentMapper.selectCompliment(dto.getCompliment_id().intValue());
        assertNotNull(selected, "게시글 조회 실패");
        System.out.println("조회된 게시글: " + selected);
    }

    @Test
    void testSelectAll() {
        // 전체 조회
        var list = complimentMapper.selectAllCompliments(0, 10);
        assertNotNull(list, "전체 조회 실패");
        System.out.println("전체 게시글 수: " + list.size());
    }

    @Test
    void testIncrementCount() {
        // 조회수 증가 테스트 (1번 게시글 기준)
        int result = complimentMapper.incrementComplimentCount(1);
        assert(result > 0);
        System.out.println("조회수 증가 완료");
    }

	
}
