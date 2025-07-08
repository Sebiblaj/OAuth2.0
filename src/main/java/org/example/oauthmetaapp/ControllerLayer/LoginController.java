package org.example.oauthmetaapp.ControllerLayer;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.Session;
import org.example.oauthmetaapp.BusinessLayer.MetaService;
import org.example.oauthmetaapp.Entities.AccessTokenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    private MetaService metaService;

    @Autowired
    private RestTemplate restTemplate;


    @GetMapping(value = "meta")
    public void redirectToFacebook(HttpServletResponse response) throws IOException {
        response.sendRedirect(metaService.buildAuthCodeURI());
    }

    @GetMapping(value = "callback")
    public ResponseEntity<?> getUserInfo(@RequestParam String code,HttpServletResponse response){

        String uri = metaService.buildAccessTokenURI(code);

        AccessTokenDTO accessTokenDTO = restTemplate.getForObject(uri, AccessTokenDTO.class);

        if(accessTokenDTO == null){
            return ResponseEntity.internalServerError().build();
        }

        ResponseCookie responseCookie = ResponseCookie.from("meta_access_token",accessTokenDTO.getAccess_token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofSeconds(accessTokenDTO.getExpires_in()))
                .sameSite("Lax")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return ResponseEntity.ok("Access token stored in the cookie");

    }
}
