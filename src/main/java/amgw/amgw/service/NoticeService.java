package amgw.amgw.service;

import java.util.List;

import amgw.amgw.entity.Notice;
import org.springframework.web.multipart.MultipartFile;

import amgw.amgw.dto.NoticeDto;
import amgw.amgw.dto.Upload_fileDto;

public interface NoticeService {
	
	int insertNotice(NoticeDto not);
	NoticeDto selectNotice(int notice_id);
	List<NoticeDto> selectAllNotices(int page, int pageSize);
	int selectNoticeCount();
	int updateNotice(NoticeDto notice);
	int deleteNotice(int notice_id);
	int incrementNoticeCount(int notice_id);
	List<Upload_fileDto> selectFilesByNotice(int notice_id);
	int insertFile(Upload_fileDto file);
	int makeAsImportant(int notice_id);
	int deleteNotices(List<Integer> noticeIds);
	
	void insertNoticeWithFiles(NoticeDto notice, List<MultipartFile> uploadFiles);
	Upload_fileDto selectFileById(int fileId);

	List<Notice> getRecentNotices();
}
