package amgw.amgw;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.internal.build.AllowSysOut;
import org.junit.jupiter.api.Tags;
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
	void testInsertCompliment() {
		ComplimentDto dto = new ComplimentDto();
		dto.setUser_id("gwapp");
		dto.setPassword("GwApp!2025");
		dto.setCompliment_count(0);
		dto.setCompliment_title("칭찬게시글 테스트");
		dto.setCompliment_detail("이건 mapper 테스트 중입니다.");
		
		int result = complimentMapper.insertCompliment(dto);
		System.out.println("insert result: " + result);
		System.out.println("생선된 ID : " + dto.getCompliment_id());
		
		assertThat(result).isEqualTo(1);
		assertThat(dto.getCompliment_id()).isGreaterThan(0);
	}
	
	@Test
	void testSelectCompliment() {
		ComplimentDto compliment = complimentMapper.selectCompliment(1);
		if (compliment != null) {
			System.out.println("조회 성공 : " + compliment);
		} else {
			System.out.println("조회 실패 - 해당 ID 없음");
		}
	}
	
	@Test
	void testUpdateCompliment() {
		ComplimentDto dto = new ComplimentDto();
		dto.setCompliment_id(1);
		dto.setUser_id("gwapp");
		dto.setPassword("1234");
		dto.setCompliment_title("수정된 제목");
		dto.setCompliment_detail("수정된 내용입니다.");
		
		int result = complimentMapper.updateCompliment(dto);
		System.out.println("update result : " + result);
		assertThat(result).isGreaterThanOrEqualTo(0);
	}
	
	@Test
	void testIncrementCount() {
		int result = complimentMapper.incrementComplimentCount(1);
		System.out.println("조회수 증가 결과 : " + result);
		assertThat(result).isGreaterThanOrEqualTo(0);
	}
	
	@Test
	void testSelectAllCompliments() {
		List<ComplimentDto> list = complimentMapper.selectAllCompliments(0, 10);
		System.out.println("전체 목록 개수 : " + list.size());
		list.forEach(System.out::println);
		assertThat(list).isNotNull();
	}
	
	@Test
	void testSearchCompliments() {
		List<ComplimentDto> searchList = complimentMapper.searchCompliments("title", "테스트", 0, 10);
		System.out.println("검색 결과 개수 : " + searchList.size());
		searchList.forEach(System.out::println);
	}
	
	@Test
	void testDeleteCompliment() {
		int result = complimentMapper.deleteCompliment(1);
		System.out.println("삭제 결과 : " + result);
		assertThat(result).isGreaterThanOrEqualTo(0);
	}
}
