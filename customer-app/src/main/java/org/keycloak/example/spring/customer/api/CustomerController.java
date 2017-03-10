package org.keycloak.example.spring.customer.api;

//import net.rossillo.spring.web.mvc.CacheControl;
//import net.rossillo.spring.web.mvc.CachePolicy;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.example.spring.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;

/**
 * Customer portal controller.
 */
@Controller
//@CacheControl(policy = CachePolicy.NO_CACHE)
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @NotNull
    @Value("${product.service.url}")
    private String productServiceUrl;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String handleHomePageRequest(Model model) {
        return "home";
    }

    @RequestMapping(value = "/customers", method = RequestMethod.GET)
    public String handleCustomersRequest(Principal principal, Model model) {
        model.addAttribute("customers", customerService.getCustomers());
        model.addAttribute("principal",  principal);
        return "customers";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/prepost/principal", method = RequestMethod.GET)
    @ResponseBody
    public String getPrincipal(KeycloakAuthenticationToken token) {
        return token.getAccount().getKeycloakSecurityContext().getTokenString();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/prepost/createRealm")
    public @ResponseBody String createRealm(@RequestBody String realmName) {
        return customerService.createRealm(realmName);
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String handleAdminRequest(Principal principal, Model model) {
        model.addAttribute("principal",  principal);
        return "admin";
    }

    @ModelAttribute("productServiceUrl")
    public String populateProductServiceUrl() {
        return productServiceUrl;
    }

    @ModelAttribute("serviceName")
    public String populateServiceName() {
        return "Customer Portal";
    }
}
