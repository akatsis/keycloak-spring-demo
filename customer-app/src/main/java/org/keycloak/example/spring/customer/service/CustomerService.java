package org.keycloak.example.spring.customer.service;

import java.util.List;
import java.util.Map;

/**
 * Created by scott on 4/22/15.
 */
public interface CustomerService {

    /**
     * Returns a list of customers.
     */
    List<String> getCustomers(String realm);

    String createGame(String gameName);

    String addIdentityProviderToRealm(
            String realmName,
            Map<String, String> config);
}
