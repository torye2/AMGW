package amgw.amgw.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Upload_fileDto {
	private int file_id;
	private int attach_idx;
	private String orig_name;
	private String stored_name;
	private String rel_path;
	private String context_type;
	private int file_size;
	private String user_id;
	private Timestamp registration_time;
}
