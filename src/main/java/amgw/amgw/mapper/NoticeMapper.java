package amgw.amgw.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import amgw.amgw.dto.NoticeDto;
import amgw.amgw.dto.Upload_fileDto;

@Mapper
public interface NoticeMapper {
	
	int insertNotice(NoticeDto notice);
	NoticeDto selectNotice(@Param("notice_id") int notice_id);
	List<NoticeDto> selectAllNotices(
			@Param("offset") int offset, 
			@Param("limit") int limit
	);
	int selectNoticeCount(); //공지사항 전체 개수 조회 (페이징 계산용)
	int updateNotice(NoticeDto notice);
	int deleteNotice(@Param("notice_id") int notice_id);
	int incrementNoticeCount(@Param("notice_id") int notice_id);
	List<Upload_fileDto> selectFilesByNotice(int notice_id);
	Upload_fileDto selectFileById(@Param("fileId") int fileId);
	int insertFile(Upload_fileDto file);
	int makeAsImportant(@Param("notice_id") int notice_id);
	int deleteNotices(@Param("noticeIds") List<Integer> noticeIds);
}
