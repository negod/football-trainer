package se.backede.coachhub.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()
                        // TODO(#20): remove once Coach Accounts & Authentication exists.
                        // Every endpoint below derives its owner from
                        // SingleTenantCurrentCoachResolver, not the request, so
                        // permitting them here doesn't widen what a caller can read
                        // or write — it just lets the single-tenant app be used at
                        // all before real login exists. Lock these back down to
                        // `.authenticated()` when #20 lands.
                        .requestMatchers("/api/teams/**").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}
