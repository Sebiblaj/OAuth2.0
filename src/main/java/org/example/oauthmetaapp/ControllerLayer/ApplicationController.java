package org.example.oauthmetaapp.ControllerLayer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.oauthmetaapp.BusinessLayer.MetaService;
import org.example.oauthmetaapp.Entities.FacebookUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(value = "myapp")
public class ApplicationController {

    @Autowired
    private MetaService metaService;

    @GetMapping(value = "info")
    public ResponseEntity<?> store(HttpServletRequest request){

        String token = metaService.getAccessToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token missing");
        }

        return ResponseEntity.ok(metaService.fetchUserData(request,token));

    }


}
