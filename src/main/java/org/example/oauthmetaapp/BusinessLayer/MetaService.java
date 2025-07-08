package org.example.oauthmetaapp.BusinessLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.oauthmetaapp.Entities.FacebookUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public String buildAccessTokenURI(String authCode){
        return UriComponentsBuilder.fromUriString(tokenUri)
                .queryParam("client_id",clientId)
                .queryParam("redirect_uri",redirectUri)
                .queryParam("client_secret",clientSecret)
                .queryParam("code",authCode)
                .build().toUriString();
    }

    public void fetchUserData(HttpServletRequest request,String accessToken) {

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
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        HttpSession session = request.getSession();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );


    }

    public FacebookUser getUser() {
        return (FacebookUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    }

    public String checkAccessToken(HttpServletRequest request) {

        List<Cookie> cookies = List.of(request.getCookies());
        if (cookies.isEmpty()) { return null;}
        Optional<Cookie> desiredCookie = cookies.stream().filter(cookie -> cookie.getName().equals("meta_access_token")).findAny();
        return desiredCookie.map(Cookie::getValue).orElse(null);

    }

}
