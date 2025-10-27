package amgw.amgw.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import amgw.amgw.dto.ComplimentDto;
import amgw.amgw.mapper.ComplimentMapper;

@Service
public class ComplimentService {

    @Autowired
    private ComplimentMapper complimentMapper;

    // 전체 게시글 조회 (페이징)
    public List<ComplimentDto> getCompliments(int page, int size) {
        int offset = (page - 1) * size;
        return complimentMapper.selectAllCompliments(offset, size);
    }

    // 전체 게시글 수
    public int getTotalComplimentsCount() {
        return complimentMapper.selectComplimentCount();
    }

    // 검색 결과 조회
    public List<ComplimentDto> searchCompliments(String searchType, String keyword, int page, int size) {
        int offset = (page - 1) * size;
        return complimentMapper.searchCompliments(searchType, keyword, offset, size);
    }

    // 검색 결과 수
    public int getSearchCount(String searchType, String keyword) {
        return complimentMapper.selectComplimentCountBySearch(searchType, keyword);
    }
}
