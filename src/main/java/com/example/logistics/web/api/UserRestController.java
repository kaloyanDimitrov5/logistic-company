package com.example.logistics.web.api;

import com.example.logistics.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserRestController {

    private final UserRepo userRepo;

//    @GetMapping("/check-email")
//    public boolean checkEmail(@RequestParam String email) {
//        if (email == null || email.isBlank()) return false;
//        return userRepo.existsByEmailIgnoreCase(email);
//    }
}