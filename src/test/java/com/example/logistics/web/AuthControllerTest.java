//package com.example.logistics.web;
//
//import com.example.logistics.service.RegistrationService;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentMatchers;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.Mockito.verify;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AuthController.class)
//class AuthControllerTest {
//
//    @Autowired MockMvc mvc;
//
//    @Configuration
//    static class MockConfig {
//        @Bean
//        RegistrationService registrationService() {
//            return Mockito.mock(RegistrationService.class);
//        }
//    }
//
//    @Test
//    void loginPageLoads() throws Exception {
//        mvc.perform(get("/auth/login"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/login"));
//    }
//
//    @Test
//    void registerPageLoads() throws Exception {
//        mvc.perform(get("/auth/register"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/register"))
//                .andExpect(model().attributeExists("form"));
//    }
//
//    @Test
//    void registerPostsAndRedirects() throws Exception {
//        mvc.perform(post("/auth/register")
//                        .param("fullName", "Test User")
//                        .param("email", "test@example.com")
//                        .param("password", "secret")
//                        .param("phone", "0888")
//                        .param("city", "Sofia")
//                        .param("address", "Center 1"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/auth/login?registered"));
//
//        verify(registrationService).registerClient(
//                ArgumentMatchers.eq("Test User"),
//                ArgumentMatchers.eq("test@example.com"),
//                ArgumentMatchers.eq("secret"),
//                ArgumentMatchers.eq("0888"),
//                ArgumentMatchers.eq("Sofia"),
//                ArgumentMatchers.eq("Center 1")
//        );
//    }
//}