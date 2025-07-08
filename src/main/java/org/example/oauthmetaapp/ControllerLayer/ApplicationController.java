package org.example.oauthmetaapp.ControllerLayer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.oauthmetaapp.BusinessLayer.MetaService;
import org.example.oauthmetaapp.Entities.FacebookUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(value = "myapp")
public class ApplicationController {

    @Value(value = "${redirect-home-uri}")
    private String redirectHomeUri;

    @Autowired
    private MetaService metaService;

    @GetMapping(value = "info")
    public void store(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String access_token = metaService.checkAccessToken(request);
        if (access_token == null) { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid access token"); }

        metaService.fetchUserData(request,access_token);

        response.sendRedirect(redirectHomeUri);

    }

    @GetMapping(value = "home")
    public ResponseEntity<FacebookUser> home() {
        FacebookUser facebookUser = metaService.getUser();

        return facebookUser != null ? ResponseEntity.ok(facebookUser) : ResponseEntity.notFound().build();
    }


}
