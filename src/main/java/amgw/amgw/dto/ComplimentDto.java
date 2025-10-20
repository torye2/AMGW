package amgw.amgw.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplimentDto {
	
	private int compliment_id;
	private String user_id;
	private String password;
	private int compliment_count;
	private String compliment_title;
	private String compliment_detail;
	private Timestamp registration_time;
	private Timestamp fix_time;
}
