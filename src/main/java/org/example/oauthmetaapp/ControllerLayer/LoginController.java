package org.example.oauthmetaapp.ControllerLayer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.oauthmetaapp.BusinessLayer.MetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    private MetaService metaService;

    @GetMapping(value = "meta")
    public void redirectToFacebook(HttpServletResponse response) throws IOException {
        response.sendRedirect(metaService.buildAuthCodeURI());
    }

    @GetMapping(value = "callback")
    public void getUserInfo(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) throws IOException {

        metaService.returnClientInfo(code,request,response);

        response.sendRedirect("http://localhost:3000/home");
    }
}
