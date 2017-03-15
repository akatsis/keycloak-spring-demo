package org.keycloak.example.spring.api;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Created by angelos on 10/3/2017.
 */
@RestController
@RequestMapping("/multi-tenant/spring-demo/realms")
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
//        createRealmFromTemplate("spring-demo",gameName+"_realm");
        createRealmFromScratch(gameName+"_realm");
//        createClientForRealmFromTemplate("spring-demo", "customer-portal", gameName+"_realm", MOBILE_GAME_CLIENT);
        createClientForRealmFromScratch(gameName+"_realm", MOBILE_GAME_CLIENT);
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

    private void createRealmFromScratch(String newRealmName){
        RealmRepresentation realm = new RealmRepresentation();
        realm.setId(newRealmName);
        realm.setRealm(newRealmName);
        realm.setDisplayName(newRealmName);
        realm.setEnabled(true);
        realm.setSslRequired("external");
        realm.setRegistrationAllowed(true);
        realm.setDefaultRoles(asList("ROLE_USER"));
        keycloak.realms().create(realm);
    }


    //NOTE: to find what attributes and with what values you need to fill in, create all these elements MANUALLY on
    //the admin console, the used the Java Admin client to fetch these info and investigate attributes returned and their
    //respecive values
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{gameRealm}/identityProviders")
    public ResponseEntity<String> addIdentityProviderToRealm(
            @PathVariable("gameRealm") String realmName,
            @RequestBody Map<String, String> config){
        String providerId = config.get("providerId");
        String clientId = config.get("clientId");
        String clientSecret = config.get("clientSecret");
        Assert.notNull(providerId,"null providerId");
        Assert.notNull(clientId,"null clientId"); //TODO: this should be provided by author
        Assert.notNull(clientSecret,"null clientSecred");//TODO: this should be provided by author

        IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
        idp.setAlias(providerId);
        idp.setProviderId(providerId);
        idp.setEnabled(true);
        idp.setStoreToken(true);
        idp.setTrustEmail(false);
        idp.setAddReadTokenRoleOnCreate(true);
        idp.setFirstBrokerLoginFlowAlias("first broker login");
        idp.setConfig(new HashMap<String, String>(){{
            put("clientId", clientId);
            put("clientSecret", clientSecret);
            put("useJwksUrl", "true");
        }});

        keycloak.realms().realm(realmName).identityProviders().create(idp);//create the idp

        addIdPMapperToIdp(realmName, providerId,"facebook_id_idpMapper","id","facebook_id");
        addIdPMapperToIdp(realmName, providerId,"facebook_firstName_idpMapper","first_name","facebook_firstName");
        addIdPMapperToIdp(realmName, providerId,"facebook_lastName_idpMapper","last_name","facebook_lastName");
        addFacebookMappersToMobileGameClient(realmName);


        return new ResponseEntity<>(format("identity provider %s created for realm %s",providerId,realmName), HttpStatus.CREATED);
    }

    private void addIdPMapperToIdp(String realmName, String providerId, String mapperName, String providerProfilePath, String userProfilePath){
        IdentityProviderMapperRepresentation idpm = new IdentityProviderMapperRepresentation();
        idpm.setName(mapperName);
        idpm.setIdentityProviderAlias(providerId);
        idpm.setIdentityProviderMapper(providerId + "-user-attribute-mapper");
        idpm.setConfig(new HashMap<String, String>(){{
            put("jsonField", providerProfilePath);
            put("userAttribute", userProfilePath);
        }});
        keycloak.realms().realm(realmName).identityProviders().get(providerId).addMapper(idpm);
    }

    private void addFacebookMappersToMobileGameClient(String realmName){
        String appId =  keycloak.realm(realmName).clients().findByClientId(MOBILE_GAME_CLIENT).get(0).getId();
        ClientResource app =  keycloak.realm(realmName).clients().get(appId);
        app.getProtocolMappers().createMapper(getMapperConfig("facebook_id_realmMapper", "facebook_id", "facebook.id"));
        app.getProtocolMappers().createMapper(getMapperConfig("facebook_firstName_realmMapper","facebook_firstName", "facebook.firstName"));
        app.getProtocolMappers().createMapper(getMapperConfig("facebook_lastName_realmMapper","facebook_lastName", "facebook.lastName"));
    }

    private ProtocolMapperRepresentation getMapperConfig(String mapperName, String userAttributeName, String jwtAttributeName){
        ProtocolMapperRepresentation mapper = new ProtocolMapperRepresentation();
        mapper.setName(mapperName);
        mapper.setProtocol("openid-connect");
        mapper.setProtocolMapper("oidc-usermodel-attribute-mapper");
        mapper.setConsentRequired(false);
        mapper.setConfig(new HashMap<String, String>(){{
            put("userinfo.token.claim" , "true");
            put("user.attribute" , userAttributeName);
            put("id.token.claim" , "true");
            put("access.token.claim" , "true");
            put("claim.name" , jwtAttributeName);
            put("jsonType.label" , "String");
        }});
        return mapper;
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



        templateClientRepresentation.setProtocolMappers(null);

//        //TODO, COPY MAPPERS TOO, TO HAVE USER DETAILS IN PRINIPAL!!!!
//        + dokimi js adapter
//                + fb login
//                + mapper gia fb token


        //TODO: create mappers gia fb KAI ston CLIENT
        //TODO: mi vasizetai se templates gia client kai realm creation, des ti epistrefei to api kai gemise ta explicitly!!!!!!!




        keycloak.realms().realm(targetRealm).clients().create(templateClientRepresentation);


        List<RoleRepresentation> templateRoles = keycloak.realms().realm(templateRealm).roles().list();
        templateRoles.stream().filter((role -> role.getName().startsWith("ROLE_ADMIN"))).forEach((role-> {
            role.setId(null);
            role.setContainerId(targetRealm);
            keycloak.realms().realm(targetRealm).roles().create(role);
        }));

    }

    private void createClientForRealmFromScratch(String targetRealm, String newClient){
        //create client app within realm
        ClientRepresentation client= new ClientRepresentation();
        client.setClientId(newClient);
        client.setName(newClient);
        client.setPublicClient(false); //confidential
        client.setEnabled(true);
        client.setProtocol("openid-connect");
        client.setClientAuthenticatorType("client-secret");
        client.setRedirectUris(singletonList("http://localhost:9092/customer-portal/*"));
        client.setBaseUrl("http://localhost:9092/customer-portal/");
        client.setWebOrigins(singletonList("http://localhost:9092"));
        client.setStandardFlowEnabled(true); //disable other flows
        keycloak.realms().realm(targetRealm).clients().create(client);


        //create realm roles
        RoleRepresentation role = new RoleRepresentation();
        role.setName("ROLE_ADMIN");
        role.setDescription("administratoras");
        role.setClientRole(false);
        role.setContainerId(targetRealm);
        keycloak.realms().realm(targetRealm).roles().create(role);
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
