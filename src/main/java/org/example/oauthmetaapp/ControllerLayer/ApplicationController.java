package org.example.oauthmetaapp.ControllerLayer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.oauthmetaapp.BusinessLayer.MetaService;
import org.example.oauthmetaapp.Entities.FacebookUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
@RequestMapping(value = "myapp")
public class ApplicationController {

    @Autowired
    private MetaService metaService;

    @GetMapping(value = "info")
    public ResponseEntity<?> store(HttpServletRequest request) {

        String token = metaService.getAccessToken(request);

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token missing");
        }

        FacebookUser fbUser = metaService.getUser(token);

        return !fbUser.isEmpty() ? ResponseEntity.ok(fbUser) : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

    }

    @GetMapping("check")
    public Map<String, Object> debug(HttpServletRequest request, Authentication auth) {
        HttpSession session = request.getSession(false);

        return Map.of(
                "sessionExists", session != null,
                "sessionId", session != null ? session.getId() : "none",
                "authenticated", auth != null,
                "principal", auth != null ? auth.getPrincipal() : "none",
                "contextInSession", session != null && session.getAttribute("SPRING_SECURITY_CONTEXT") != null
        );
    }


}
