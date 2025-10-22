package amgw.amgw.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplimentDto {
	
	private Long compliment_id;
	private Long user_id;
	private String username;
	private String password;
	private int compliment_count;
	private String compliment_title;
	private String compliment_detail;
	private Timestamp registration_time;
	private Timestamp fix_time;
}
