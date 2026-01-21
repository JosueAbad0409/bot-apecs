package ista.M3A.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String check() {
        return "¡El Bot APECS está vivo y coleando! 🤖✅";
    }
}
