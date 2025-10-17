package amgw.amgw.controller;

import amgw.amgw.entity.User;
import amgw.amgw.entity.UserStatus;
import amgw.amgw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public String pendingUsers(Model model) {
        List<User> pending = userRepository.findAll()
                .stream()
                .filter(u -> u.getStatus_code() == UserStatus.PENDING)
                .toList();
        model.addAttribute("pendingUsers", pending);
        return "admin_users";
    }

    @PostMapping("/users/{id}/approve")
    @ResponseBody
    public String approveUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setStatus_code(UserStatus.ACTIVE);
        userRepository.save(user);
        return "OK";
    }

    @PostMapping("/users/{id}/reject")
    @ResponseBody
    public String rejectUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setStatus_code(UserStatus.SUSPENDED);
        userRepository.save(user);
        return "OK";
    }
}
