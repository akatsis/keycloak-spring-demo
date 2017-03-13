package org.keycloak.example.spring.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.keycloak.example.spring.api.KeycloakAdminApiController.MOBILE_GAME_CLIENT;

/**
 * Application security configuration.
 *
 * @author Scott Rossillo
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity(debug = true)
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter
{
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Bean
    public FilterRegistrationBean keycloakAuthenticationProcessingFilterRegistrationBean(
            KeycloakAuthenticationProcessingFilter filter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean keycloakPreAuthActionsFilterRegistrationBean(
            KeycloakPreAuthActionsFilter filter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }


    @Value("${keycloak.configurationFile:WEB-INF/keycloak.json}")
    private Resource mainRealmConfigFileResource;
    private final Map<String, KeycloakDeployment> cache = new ConcurrentHashMap<String, KeycloakDeployment>();

    @Bean
    public KeycloakConfigResolver keycloakConfigResolver(Keycloak keycloak){

        final String mainRealm = "spring-demo";

        return facade -> {
            String path = facade.getURI();
            int multitenantIndex = path.indexOf("multi-tenant/");
            if (multitenantIndex == -1) {
                KeycloakDeployment deployment = cache.get(mainRealm);
                if (null == deployment) {
                    return loadRealmFromConfigFile(mainRealm, this.mainRealmConfigFileResource);
                }
                return deployment;
            }

            String realm = path.substring(path.indexOf("multi-tenant/")).split("/")[1];
//            if (realm.contains("?")) {
//                realm = realm.split("\\?")[0];
//            }

            KeycloakDeployment deployment = cache.get(realm);
            if (null == deployment) {
                // not found on the simple cache, try to load it from the file system
                RealmRepresentation realmRepresentation =  keycloak.realm(realm).toRepresentation();
                deployment = loadRealmFromRepresentation(realm, realmRepresentation);
            }

            return deployment;
        };
    }

    private KeycloakDeployment loadRealmFromConfigFile(String realmName, Resource realmConfigFileResource) {
        InputStream is = null;
        try {
            is = realmConfigFileResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        KeycloakDeployment deployment = KeycloakDeploymentBuilder.build(is);
        this.cache.put(realmName, deployment);
        return deployment;
    }

    private KeycloakDeployment loadRealmFromRepresentation(String realmName, RealmRepresentation realmRepresentation){
        AdapterConfig realmConfig = new AdapterConfig();
        realmConfig.setRealm(realmName);
        realmConfig.setRealmKey(realmRepresentation.getPublicKey());
        realmConfig.setAuthServerUrl("http://localhost:8095/auth");
        realmConfig.setBearerOnly(true);
        realmConfig.setSslRequired("external");
        realmConfig.setResource(MOBILE_GAME_CLIENT);
        KeycloakDeployment deployment = KeycloakDeploymentBuilder.build(realmConfig);
        this.cache.put(realmName, deployment);
        return deployment;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                .and()
                .addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class)
                .addFilterBefore(keycloakAuthenticationProcessingFilter(), X509AuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers("/**").authenticated()
                .anyRequest().permitAll();
    }

}
