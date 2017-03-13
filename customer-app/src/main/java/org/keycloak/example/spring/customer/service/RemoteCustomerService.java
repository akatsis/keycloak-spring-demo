package org.keycloak.example.spring.customer.service;

import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates making a call to
 */
@Service
public class RemoteCustomerService implements CustomerService {

    private final String DEFAULT_REALM = "spring-demo";
    private final KeycloakRestTemplate template;

    @NotNull
    @Value("${database.service.url}")
    private String endpoint;



    @Autowired
    public RemoteCustomerService(KeycloakRestTemplate template) {
        this.template = template;
    }

    @Override
    public List<String> getCustomers(String realm) {
        String endpoint= this.endpoint+"/customers";
        if(!realm.equals(DEFAULT_REALM))
            endpoint = this.endpoint+"/multi-tenant/"+realm+"/customers";

        ResponseEntity<String[]> response = template.getForEntity(endpoint, String[].class);
        return Arrays.asList(response.getBody());
    }

    @Override
    public String createGame(String gameName) {
        ResponseEntity<String> response = template.postForEntity(endpoint+"/realms", gameName ,String.class);
        return response.getBody();
    }
}
