package amgw.amgw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NoticeControllor {
	
	@GetMapping("/Notice_W")
	public String Notice_w() {
		return "Notice_W";
	}
	
	@GetMapping("/Notice_L")
	public String Notice_L() {
		return "Notice_L";
	}
	
	@GetMapping("/Notice_D")
	public String Notice_D() {
		return "Notice_D";
	}
}
