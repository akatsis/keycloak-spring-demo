package org.keycloak.example.spring.api;

//import net.rossillo.spring.web.mvc.CacheControl;
//import net.rossillo.spring.web.mvc.CachePolicy;
import org.keycloak.admin.client.Keycloak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * Customer API controller.
 */
@RestController
@RequestMapping("/multi-tenant/{realm}/customers")
//@CacheControl(policy = CachePolicy.NO_CACHE)
public class CustomerApiController {

    @Value("${keycloak.server.url}")
    private String keycloakServerUrl;

    private static final Logger log = LoggerFactory.getLogger(CustomerApiController.class);

    private final Keycloak keycloak;

    @Autowired
    public CustomerApiController(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getCustomers(@PathVariable(value = "realm", required = false) String realm, Principal principal) {

        log.info("Returning customer list.");



        return Arrays.asList(
                "logged in as: "+ principal.getName(),
                "logged in in realm: "+ realm,
                "fb access token: "+ "TODO",
                "Scott Rossillo",
                "Kyung Lee",
                "Keith Leggins",
                "Ben Loy"
        );
    }


    /**
     * this is needed by the author in order to register his game with faceboook
     * After he does that , he will use our endpoint to create a facebook Identity Providers for his app
     * by providing us with the client id and client secret from the fb admin console
     */
    @GetMapping(params = {"realm","provider"})
    public String getKeycloakRedirectUriForIdp(String realm, String provider){
        return format("%s/realms/%s/broker/%s/endpoint", this.keycloakServerUrl,realm,provider);
    }
}
