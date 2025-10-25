package amgw.amgw.controller;

import java.util.List;

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
    public String insertCompliment(@AuthenticationPrincipal CustomUserDetails user, 
                                   ComplimentDto compliment, Model model) {
        try {
            if (user == null) { // 로그인 안 한 사용자 차단
                model.addAttribute("error", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            compliment.setUser_id(user.getUserId());
            complimentMapper.insertCompliment(compliment);
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
    	System.out.println("========================"+ids);
        for (Long id : ids) {
            complimentMapper.deleteCompliment(id.intValue());
        }
        return ResponseEntity.ok().build();
    }

}
