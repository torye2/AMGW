package amgw.amgw.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import amgw.amgw.config.CustomUserDetails;
import amgw.amgw.dto.ComplimentDto;
import amgw.amgw.mapper.ComplimentMapper;
import amgw.amgw.service.ComplimentService;

@Controller
public class ComplimentController {

    @Autowired
    private ComplimentService complimentService;
    
    @Autowired
    private ComplimentMapper complimentMapper;

    // 글쓰기 폼
    @GetMapping("/Compliment_W")
    public String ComplimentWriteForm(@RequestParam(value = "compliment_id", required = false) Long complimentId,
                                      Model model) {
        ComplimentDto compliment = null;
        
        if (complimentId != null) {
            compliment = complimentMapper.selectCompliment(complimentId.intValue());
        }
        
        // null 방지용
        if (compliment == null) {
            compliment = new ComplimentDto();
        }
        
        model.addAttribute("compliment", compliment);
        return "Compliment_W";
    }
    
    // 목록 (페이징 + 검색)
    @GetMapping("/Compliment_L")
    public String ComplimentListForm(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        List<ComplimentDto> complimentList;
        int totalCount;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색
            complimentList = complimentService.searchCompliments(searchType, keyword, page, size);
            totalCount = complimentService.getSearchCount(searchType, keyword);
        } else {
            // 일반 목록
            complimentList = complimentService.getCompliments(page, size);
            totalCount = complimentService.getTotalComplimentsCount();
        }

        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (totalPages == 0) totalPages = 1;
      

        model.addAttribute("complimentList", complimentList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "Compliment_L";
    }
    
    @GetMapping("/Compliment_D/{compliment_id}")
    public String complimentDetail(
            @PathVariable("compliment_id") int compliment_id,
            Model model,
            @AuthenticationPrincipal CustomUserDetails user) {

        complimentMapper.incrementComplimentCount(compliment_id);
        ComplimentDto compliment = complimentMapper.selectCompliment(compliment_id);

        boolean isAuthor = false;
        if (user != null && compliment.getUser_id().equals(user.getUserId())) {
            isAuthor = true;
        }

        model.addAttribute("compliment", compliment);
        model.addAttribute("loginUser", user);
        model.addAttribute("isAuthor", isAuthor); // ✅ 추가

        return "Compliment_D";
    }

    @PostMapping("/Compliment_W")
    public String saveCompliment(@AuthenticationPrincipal CustomUserDetails user, 
                                 ComplimentDto compliment, Model model) {
        try {
            if (user == null) { // 로그인 안 한 사용자 차단
                model.addAttribute("error", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            // 로그인한 사용자 정보 설정
            compliment.setUser_id(user.getUserId());

            if (compliment.getCompliment_id() != null) {
                // ✅ 수정 로직
                complimentMapper.updateCompliment(compliment);
            } else {
                // ✅ 새 글 등록 로직
                compliment.setCompliment_count(0);
                complimentMapper.insertCompliment(compliment);
            }

            return "redirect:/Compliment_L";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "등록 중 오류가 발생했습니다.");
            return "Compliment_W";
        }
    }

    @PostMapping("/Compliment_Delete")
    @ResponseBody
    public ResponseEntity<?> deleteCompliments(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                complimentMapper.deleteCompliment(id.intValue());
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("삭제 중 오류가 발생하였습니다.");
        }
    }
    
    @PostMapping("/Compliment_Delete_One")
    public String deleteOneCompliment(@RequestParam("compliment_id") int complimentId,
                                      @AuthenticationPrincipal CustomUserDetails user,
                                      Model model) {
        try {
            // 글 정보 가져오기
            ComplimentDto compliment = complimentMapper.selectCompliment(complimentId);
            if (compliment == null) {
                model.addAttribute("error", "존재하지 않는 게시글입니다.");
                return "redirect:/Compliment_L";
            }

            // 로그인 사용자 본인 확인
            if (user == null || !compliment.getUser_id().equals(user.getUserId())) {
                model.addAttribute("error", "본인만 삭제할 수 있습니다.");
                return "redirect:/Compliment_D/" + complimentId;
            }

            // 삭제 수행
            complimentMapper.deleteCompliment(complimentId);

            // 삭제 후 목록으로 이동
            return "redirect:/Compliment_L";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "삭제 중 오류가 발생했습니다.");
            return "redirect:/Compliment_D/" + complimentId;
        }
    }


}
