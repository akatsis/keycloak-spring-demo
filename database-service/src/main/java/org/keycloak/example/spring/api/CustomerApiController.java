package org.keycloak.example.spring.api;

//import net.rossillo.spring.web.mvc.CacheControl;
//import net.rossillo.spring.web.mvc.CachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

/**
 * Customer API controller.
 */
@RestController
@RequestMapping("/multi-tenant/{realm}/customers")
//@CacheControl(policy = CachePolicy.NO_CACHE)
public class CustomerApiController {

    private static final Logger log = LoggerFactory.getLogger(CustomerApiController.class);

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
}
