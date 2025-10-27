package amgw.amgw.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import amgw.amgw.config.CustomUserDetails;
import amgw.amgw.dto.NoticeDto;
import amgw.amgw.dto.Upload_fileDto;
import amgw.amgw.mapper.NoticeMapper;
import amgw.amgw.service.NoticeService;

@Controller
public class NoticeController {
	
	@Autowired
	private NoticeService noticeService;
	
	@Autowired
	private NoticeMapper noticeMapper;
	
	@GetMapping("/Notice_L")
	public String noticeList(@RequestParam(name = "page", defaultValue = "1") int page,
							 @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
							 @RequestParam(name = "searchType", required = false) String searchType,
							 @RequestParam(name = "keyword", required = false) String keyword,
							 Model model) {
		
		int totalCount; 
		List<NoticeDto> notices;
		
		if (keyword != null && !keyword.trim().isEmpty()) {
			//검색이 있는 경우
			totalCount = noticeMapper.selectNoticeCountBySearch(searchType, keyword);
			int offset = (page - 1) * pageSize;
			notices = noticeMapper.searchNotices(searchType, keyword, offset, pageSize);
		} else {
			//검색이 없는 경우
			totalCount = noticeService.selectNoticeCount();
			notices = noticeService.selectAllNotices(page, pageSize);
		}
		
		int totalPages = (int) Math.ceil((double) totalCount / pageSize);
		if (totalPages == 0) totalPages = 1;
		
		model.addAttribute("notices", notices);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("keyword", keyword);
		model.addAttribute("searchType", searchType);
		
		return "Notice_L";

	}
	
	@GetMapping("/Notice_D/{notice_id}")
	public String noticeDetail(@PathVariable(name = "notice_id") int notice_id, Model model) {
		noticeService.incrementNoticeCount(notice_id);
		NoticeDto notice = noticeService.selectNotice(notice_id);
		List<Upload_fileDto> files = noticeMapper.selectFilesByNotice(notice_id);
		notice.setFiles(files);
		model.addAttribute("notice", notice);
		return "Notice_D";
	}
	
	@GetMapping("/Notice_W")
	public String noticeWriteForm(@RequestParam(name = "notice_id", required = false) Integer notice_id,
								  Model model) {
		if (notice_id != null) {
			NoticeDto notice = noticeService.selectNotice(notice_id);
			model.addAttribute("notice", notice);
		} else {
			model.addAttribute("notice", new NoticeDto());
		}
		
		return "Notice_W";
	}
	
	@PostMapping("/Notice_W")
	public String noticeWrite(
	    @ModelAttribute NoticeDto notice,
	    @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles
	) {
	    // 로그인 사용자 정보 설정
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
	    notice.setUser_id(userDetails.getUserId());

	    if (notice.getNotice_id() != null) {
	        // 🔹 수정 로직
	        noticeService.updateNotice(notice);

	        // (선택) 기존 파일 삭제 후 새 파일 등록 로직
	        if (uploadFiles != null && !uploadFiles.isEmpty()) {
	            noticeService.insertNoticeWithFiles(notice, uploadFiles);
	        }

	    } else {
	        // 🔹 새 글 등록 로직
	        notice.setNotice_count(0);
	        notice.setRegistration_time(new Timestamp(System.currentTimeMillis()));

	        if (uploadFiles != null && !uploadFiles.isEmpty()) {
	            noticeService.insertNoticeWithFiles(notice, uploadFiles);
	        } else {
	            noticeService.insertNotice(notice);
	        }
	    }

	    return "redirect:/Notice_L";
	}

	
	@PostMapping("/Notice_Delete")
	public String deleteNotices(@RequestParam("noticeIds") List<Integer> noticeIds) {
		noticeService.deleteNotices(noticeIds);			
		return "redirect:/Notice_L";
	}
	
	@GetMapping("/download/{fileId}")
	public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") int fileId) throws IOException {
		Upload_fileDto file = noticeService.selectFileById(fileId);
		
		if(file == null) {
			return ResponseEntity.notFound().build();
		}
		
		Path path = Paths.get(file.getRel_path());
		Resource resource = new UrlResource(path.toUri());
		
		String contentDisposition = "attachment; filename=\"" + URLEncoder.encode(file.getOrig_name(), "UTF-8") + "\"";
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
				.contentType(MediaType.parseMediaType(file.getContext_type()))
				.body(resource);
	}
}
