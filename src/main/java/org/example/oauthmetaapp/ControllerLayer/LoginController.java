package org.example.oauthmetaapp.ControllerLayer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.oauthmetaapp.BusinessLayer.MetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    private MetaService metaService;

    @Value(value = "${redirect-home-uri}")
    private String redirectHomeUri;

    @GetMapping(value = "meta")
    public void redirectToFacebook(HttpServletResponse response) throws IOException {
        response.sendRedirect(metaService.buildAuthCodeURI());
    }

    @GetMapping(value = "callback")
    public void getUserInfo(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String accessToken = metaService.returnAccessToken(code,request,response);

        metaService.fetchUserData(request,response,accessToken);

        response.sendRedirect(redirectHomeUri);
    }
}
