package amgw.amgw.dto;

import java.sql.Timestamp;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDto {
	private int notice_id;
	private int file_id;
	private int notice_count;
	private String notice_title;
	private String notice_detail;
	private String user_id;
	private Timestamp registration_time;
	private Timestamp fix_time;
	private boolean important;
	private String displayNumber;
	
	private List<Upload_fileDto> files;
}
