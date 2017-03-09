package org.keycloak.example.spring.customer.config;

import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Created by angelos on 8/3/2017.
 */
//@Configuration
//@EnableWebSecurity(debug = true)
//@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class) //necessary for KeycloakClientRequestFactory to be found
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KeycloakRestTemplate keycloakRestTemplate(KeycloakClientRequestFactory keycloakClientRequestFactory) {
        return new KeycloakRestTemplate(keycloakClientRequestFactory);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        super.configure(http);
        http
                .authorizeRequests()
                .antMatchers("/customers*").hasRole("USER")
                .antMatchers("/admin*").hasRole("ADMIN")
                .anyRequest().permitAll();
    }
}
