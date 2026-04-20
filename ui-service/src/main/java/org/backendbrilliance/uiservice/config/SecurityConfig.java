package org.backendbrilliance.uiservice.config;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.backendbrilliance.uiservice.views.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        super.configure(http);
//        setLoginView(http, LoginForm.class);
//    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /**
         * Delegating the responsibility of general configuration
         * of HTTP security to the VaadinSecurityConfigurer.
         *
         * It's configuring the following:
         * - Vaadin's CSRF protection by ignoring internal framework requests,
         * - default request cache,
         * - ignoring public views annotated with @AnonymousAllowed,
         * - restricting access to other views/endpoints, and
         * - enabling ViewAccessChecker authorization.
         */

        // You can add any possible extra configurations of your own
        // here - the following is just an example:
        http.rememberMe(customizer -> customizer.alwaysRemember(false));

        // Configure your static resources with public access before calling
        // VaadinSecurityConfigurer.vaadin() as it adds final anyRequest matcher
//        http.authorizeHttpRequests(auth -> {
//            auth.requestMatchers("/admin-only/**").hasAnyRole("admin")
//                    .requestMatchers("/public/**").permitAll();
//        });

        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            // This is important to register your login view to the
            // view access checker mechanism:
            configurer.loginView(LoginView.class);
        });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails userDetails = User.builder()
                .username("dev@bb.com")
                .password(passwordEncoder.encode("hookspy123"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(userDetails);
    }
}
