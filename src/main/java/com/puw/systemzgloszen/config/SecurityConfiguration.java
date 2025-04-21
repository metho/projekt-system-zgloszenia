package com.puw.systemzgloszen.config;

import com.puw.systemzgloszen.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .userDetailsService(userDetailsService())
                .authorizeHttpRequests(auth -> auth.requestMatchers(new AntPathRequestMatcher("/public/**"))
                        .permitAll());

        super.configure(http);

        // This is important to register your login view to the
        // navigation access control mechanism:
        setLoginView(http, LoginView.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // Customize your WebSecurity configuration.
        super.configure(web);
    }


    @Bean
    public UserDetailsManager userDetailsService() {
        UserDetails user =
                User.withUsername("user")
                        .password("{noop}userpassword")
                        .roles("USER")
                        .build();
        UserDetails admin =
                User.withUsername("admin")
                        .password("{noop}adminpassword")
                        .roles("ADMIN")
                        .build();
        return new InMemoryUserDetailsManager(user, admin);
    }
}