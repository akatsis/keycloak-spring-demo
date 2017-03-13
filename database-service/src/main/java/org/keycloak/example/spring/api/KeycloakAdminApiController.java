package org.keycloak.example.spring.api;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Created by angelos on 10/3/2017.
 */
@RestController("/realms")
public class KeycloakAdminApiController {
    public static final String MOBILE_GAME_CLIENT = "mobile_game_app";

    private final Keycloak keycloak;

    @Autowired
    public KeycloakAdminApiController(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<String> createGame(@RequestBody String gameName) {
        createRealmFromTemplate("spring-demo",gameName+"_realm");
        createClientForRealmFromTemplate("spring-demo", "customer-portal", gameName+"_realm", MOBILE_GAME_CLIENT);
//        createRealmUser(gameName+"_realm");
        return new ResponseEntity<>("game realm and client created!", HttpStatus.CREATED);
    }

    private void createRealmFromTemplate(String templateRealm, String newRealm){
        RealmRepresentation templateRealmRepresentation = keycloak.realms().realm(templateRealm).toRepresentation();
        templateRealmRepresentation.setUsers(new ArrayList<>());
        templateRealmRepresentation.setClients(new ArrayList<>());
        templateRealmRepresentation.setId(newRealm);
        templateRealmRepresentation.setRealm(newRealm);
        templateRealmRepresentation.setDisplayName(newRealm);
        templateRealmRepresentation.setEnabled(true);
        templateRealmRepresentation.setSslRequired("external");
        templateRealmRepresentation.setRegistrationAllowed(true);
        templateRealmRepresentation.setDefaultRoles(asList("ROLE_USER"));
        keycloak.realms().create(templateRealmRepresentation);
    }

    private void createClientForRealmFromTemplate(String templateRealm, String templateCLient, String targetRealm, String newClient){
        ClientRepresentation templateClientRepresentation=
                keycloak.realms().realm(templateRealm).clients().findByClientId(templateCLient).get(0);
        templateClientRepresentation.setClientId(newClient);
        templateClientRepresentation.setName(newClient);
        templateClientRepresentation.setId(null);
        templateClientRepresentation.setPublicClient(false); //confidential
        templateClientRepresentation.setRedirectUris(singletonList("http://localhost:9092/customer-portal/*"));
        templateClientRepresentation.setBaseUrl("http://localhost:9092/customer-portal/");
        templateClientRepresentation.setWebOrigins(singletonList("http://localhost:9092"));
        templateClientRepresentation.setStandardFlowEnabled(true); //disable other flows
        templateClientRepresentation.setProtocolMappers(null);//TODO, COPY MAPPERS TOO, TO HAVE USER DETAILS IN PRINIPAL!!!!
        keycloak.realms().realm(targetRealm).clients().create(templateClientRepresentation);


        List<RoleRepresentation> templateRoles = keycloak.realms().realm(templateRealm).roles().list();
        templateRoles.stream().filter((role -> role.getName().startsWith("ROLE_ADMIN"))).forEach((role-> {
            role.setId(null);
            role.setContainerId(targetRealm);
            keycloak.realms().realm(targetRealm).roles().create(role);
        }));

    }


//    private void createRealmUser(String realm){
//        CredentialRepresentation credential = new CredentialRepresentation();
//        credential.setType(CredentialRepresentation.PASSWORD);
//        credential.setValue("test123");
//        credential.setTemporary(false);
//
//        UserRepresentation user = new UserRepresentation();
//        user.setUsername("testUser");
//        user.setFirstName("Test");
//        user.setLastName("User");
//        user.setCredentials(singletonList(credential));
//        user.setEnabled(true);
//        user.setRealmRoles(asList("ROLE_ADMIN","ROLE_USER"));
//
//        keycloak.realms().realm(realm).users().create(user);
//
//
//
//    }

}
