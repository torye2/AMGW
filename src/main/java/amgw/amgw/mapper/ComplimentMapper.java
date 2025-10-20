package amgw.amgw.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import amgw.amgw.dto.ComplimentDto;

@Mapper
public interface ComplimentMapper {
	
	//등록
	int insertCompliment(ComplimentDto compliment);
	
	//수정
	int updateCompliment(ComplimentDto compliment);
	
	//삭제
	int deleteCompliment(@Param("compliment_id") int compliment_id);
	
	//단일 조회
	ComplimentDto selectCompliment(@Param("compliment_id") int compliment_id);
	
	//전체 조회(페이지네이션)
	List<ComplimentDto> selectAllCompliments(
			@Param("offset") int offsert,
			@Param("limit") int limit
	);
	
	//전체 개수 조회(페이지 계산)
	int selectComplimentCount();
	
	//조회수 증가
	int incrementComplimentCount(@Param("compliment_id") int compliment_id);
	
	//검색 + 페이징
	List<ComplimentDto> searchCompliments(
			@Param("searchType") String searchType,
			@Param("keyword") String keyword,
			@Param("offset") int offset,
			@Param("limit") int limit
	);
	
	//검색 결과 개수
	int selectComplimentCountBySearch(
			@Param("searchType") String searchType,
			@Param("keyword") String keyword
	);
}
