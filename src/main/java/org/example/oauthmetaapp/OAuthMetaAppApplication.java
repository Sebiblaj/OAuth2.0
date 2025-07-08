package org.example.oauthmetaapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class OAuthMetaAppApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();

        System.setProperty("meta.client-id", Objects.requireNonNull(dotenv.get("META_CLIENT_ID")));
        System.setProperty("meta.client-secret", Objects.requireNonNull(dotenv.get("META_SECRET_KEY")));

        SpringApplication.run(OAuthMetaAppApplication.class, args);
    }

}
