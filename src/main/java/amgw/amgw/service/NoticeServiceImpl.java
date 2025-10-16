package amgw.amgw.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import amgw.amgw.dto.NoticeDto;
import amgw.amgw.dto.Upload_fileDto;
import amgw.amgw.mapper.NoticeMapper;

@Service
public class NoticeServiceImpl implements NoticeService {
	
	@Autowired
	private NoticeMapper noticeMapper;
	
	@Override
	public int insertNotice(NoticeDto notice) {
		return noticeMapper.insertNotice(notice);
	}

	@Override
	public NoticeDto selectNotice(int notice_id) {
		return noticeMapper.selectNotice(notice_id);
	}

	@Override
	public List<NoticeDto> selectAllNotices(int page, int pageSize) {
		int offset = (page - 1) * pageSize;
		return noticeMapper.selectAllNotices(offset, pageSize);
	}

	@Override
	public int selectNoticeCount() {
		return noticeMapper.selectNoticeCount();
	}

	@Override
	public int updateNotice(NoticeDto notice) {
		return noticeMapper.updateNotice(notice);
	}

	@Override
	public int deleteNotice(int notice_id) {
		return noticeMapper.deleteNotice(notice_id);
	}

	@Override
	public int incrementNoticeCount(int notice_id) {
		return noticeMapper.incrementNoticeCount(notice_id);
	}

	@Override
	public List<Upload_fileDto> selectFilesByNotice(int notice_id) {
		return noticeMapper.selectFilesByNotice(notice_id);
	}

	@Override
	public int insertFile(Upload_fileDto file) {
		return noticeMapper.insertFile(file);
	}

	@Override
	public int makeAsImportant(int notice_id) {
		return noticeMapper.makeAsImportant(notice_id);
	}

	@Override
	public int deleteNotices(List<Integer> noticeIds) {
		return noticeMapper.deleteNotices(noticeIds);
	}
	
	@Override
	public Upload_fileDto selectFileById(int fileId) {
		return noticeMapper.selectFileById(fileId);
	}
	
	@Override
	public void insertNoticeWithFiles(NoticeDto notice, List<MultipartFile> uploadFiles) {
		
		//1. 게시글 저장
		noticeMapper.insertNotice(notice);
		
		//2. 파일 저장
		if (uploadFiles != null && !uploadFiles.isEmpty() ) {
			for (MultipartFile file : uploadFiles) {
				if(!file.isEmpty()) {
					try {
						//파일 저장 경로 설정 (폴더 미리 존재해야 함)
						String uploadDir = "C:/upload/";
						java.io.File dir = new java.io.File(uploadDir);
						if (!dir.exists()) dir.mkdirs();
						
						//실제 파일 저장
						String storedName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
						java.io.File dest = new java.io.File(uploadDir + storedName);
						file.transferTo(dest);
						
						//DB에 저장할 정보 세팅
						Upload_fileDto uploadFile = new Upload_fileDto();
						uploadFile.setAttach_idx(notice.getNotice_id());
						uploadFile.setOrig_name(file.getOriginalFilename());
						uploadFile.setStored_name(storedName);
						uploadFile.setRel_path(uploadDir + storedName);
						uploadFile.setContext_type(file.getContentType());
						uploadFile.setFile_size((int) file.getSize());
						uploadFile.setUser_id("gwapp");
						
						noticeMapper.insertFile(uploadFile);
					
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
		}
	}
}
