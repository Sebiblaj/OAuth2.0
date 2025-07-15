package org.example.oauthmetaapp.BusinessLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.oauthmetaapp.Entities.AccessTokenDTO;
import org.example.oauthmetaapp.Entities.FacebookUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings(value = {"unused","unchecked"})
public class MetaService {

    @Value("${meta.client-id}")
    private String clientId;

    @Value("${meta.response-type}")
    private String responseType;

    @Value("${meta.client-secret}")
    private String clientSecret;

    @Value("${meta.redirect-uri}")
    private String redirectUri;

    @Value("${meta.scope}")
    private String scope;

    @Value("${meta.authorization-uri}")
    private String authorizationUri;

    @Value("${meta.token-uri}")
    private String tokenUri;

    @Value("${meta.user-info-uri}")
    private String userInfoUri;

    @Value("${meta.fields}")
    private String fields;


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    public String buildAuthCodeURI(){
        return UriComponentsBuilder.fromUriString(authorizationUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .queryParam("response_type", responseType)
                .build()
                .toUriString();
    }

    public String returnAccessToken(String authCode, HttpServletRequest request, HttpServletResponse response){

        String accessTokenUri =  buildAccessTokenURI(authCode);
        AccessTokenDTO accessTokenDTO = restTemplate.getForObject(accessTokenUri, AccessTokenDTO.class);

        if(accessTokenDTO == null){
            throw new RuntimeJsonMappingException("The access token could not be found");
        }

        ResponseCookie responseCookie = ResponseCookie.from("meta_access_token",accessTokenDTO.getAccess_token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofSeconds(accessTokenDTO.getExpires_in()))
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", responseCookie.toString());

        return accessTokenDTO.getAccess_token();
    }

    public void fetchUserData(HttpServletRequest request,HttpServletResponse response,String accessToken) {

        String uri = UriComponentsBuilder.fromUriString(userInfoUri)
                .queryParam("fields", fields)
                .queryParam("access_token", accessToken)
                .build().toUriString();

        Map<?, ?> userInfo = restTemplate.getForObject(uri, Map.class);


        assert userInfo != null;
        String firstName = (String) userInfo.get("first_name");
        String lastName = (String) userInfo.get("last_name");
        String email = (String) userInfo.get("email");
        String id = (String) userInfo.get("id");
        String picture = ((Map<String, Object>) ((Map<String, Object>) userInfo.get("picture")).get("data")).get("url").toString();

        FacebookUser user = new FacebookUser(id, firstName,lastName, email, picture);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("USER"))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);

    }

    public FacebookUser getUser(String accessToken){
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null ? new FacebookUser() : (FacebookUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public  String getAccessToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("meta_access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String buildAccessTokenURI(String authCode){
        return UriComponentsBuilder.fromUriString(tokenUri)
                .queryParam("client_id",clientId)
                .queryParam("redirect_uri",redirectUri)
                .queryParam("client_secret",clientSecret)
                .queryParam("code",authCode)
                .build().toUriString();
    }

}
