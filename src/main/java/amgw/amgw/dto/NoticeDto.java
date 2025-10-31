package amgw.amgw.dto;

import java.sql.Timestamp;
import java.util.List;

import amgw.amgw.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDto {
	private Long notice_id;
	private Long file_id;
	private Integer notice_count;
	private String notice_title;
	private String notice_detail;
	private String username;
	private Long user_id;
	private Timestamp registration_time;
	private Timestamp fix_time;
	private boolean important;
	private String displayNumber;
	
	private List<Upload_fileDto> files;

	public static NoticeDto fromEntity(Notice notice) {
		if(notice == null) return null;

		return NoticeDto.builder()
				.notice_id(notice.getNoticeId())
				.file_id(notice.getFileId())
				.notice_count(notice.getNoticeCount())
				.notice_title(notice.getNoticeTitle())
				.notice_detail(notice.getNoticeDetail())
				.username(null)
				.user_id(notice.getUserId())
				.registration_time(notice.getRegistrationTime())
				.fix_time(notice.getFixTime())
				.important(false)
				.displayNumber(null)
				.build();
	}
}
