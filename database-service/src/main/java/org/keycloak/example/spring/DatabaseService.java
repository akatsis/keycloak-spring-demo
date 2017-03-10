package org.keycloak.example.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import java.io.File;

/**
 * Created by scott on 4/22/15.
 */
@SpringBootApplication
public class DatabaseService extends SpringBootServletInitializer {

    /**
     * Initializes this application when running as a standalone application.
     */
    public static void main(String[] args) throws Exception {
        SpringApplication.run(DatabaseService.class, args);
    }

    /**
     * Initializes this application when running in a servlet container (e.g. Tomcat)
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DatabaseService.class);
    }

    //TODO
//
//    {
//        "realm": "spring-demo",
//            "realm-public-key": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCh65Gqi3BSaVe12JHlqChWm8WscICrj46MVqmRoO9FCmqbxEpCQhE1RLjW+GDyc3YdXW3xqUQ3AZxDkTmN1h6BWkhdxPLzA4EnwgWmGurhyJlUF9Id2tKns0jbC+Z7kIb2LcOiKHKL7mRb3q7EtWubNnrvunv8fx+WeXGaQoGEVQIDAQAB",
//            "auth-server-url": "http://localhost:8095/auth",
//            "bearer-only": true,
//            "ssl-required": "external",
//            "resource": "database-service"
//    }


    @Bean
    public Keycloak getKeycloak(){
        return KeycloakBuilder.builder()
                .serverUrl("http://localhost:8095/auth")
                .realm("master")
                .username("admin")
                .password("admin123")
                .clientId("admin-cli")
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
//                .clientId("spring-demo-client")
//                .clientSecret("eb4906d9-230a-40c1-a8af-8f731049c569")
                .build();
    }


}
