//
//package com.example.expo.Controller;
//
////import com.example.expo.Client.OpenRouterClient;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/v1/ai")
//@RequiredArgsConstructor
//@ConditionalOnProperty(
//        name = "app.ai.enabled",
//        havingValue = "true"
//)
//public class AiTestController {
//
////    private final OpenRouterClient openRouterClient;
//
//    @GetMapping("/test")
//    public String testAi() {
//
//        return openRouterClient.sendPrompt(
//                "You are a test assistant. Follow the user's request exactly.",
//                "Reply with exactly this text: AI connection successful"
//        );
//    }
//}
