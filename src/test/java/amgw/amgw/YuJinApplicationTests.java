package amgw.amgw;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import amgw.amgw.dto.NoticeDto;
import amgw.amgw.mapper.NoticeMapper;

@SpringBootTest
public class YuJinApplicationTests {

	@Autowired 
	private NoticeMapper noticeMapper;
	
	@Test 
    void testinsertNotice() { 
    	
    	NoticeDto not = new NoticeDto(); 
    	not.setFile_id(0);
    	not.setNotice_count(0);
    	not.setNotice_title("테스트 공지사항");
    	not.setNotice_detail("테스트 공지사항 내용");
    	not.setUser_id("gwapp");
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
}
