package com.example.springatt.config;

import com.example.springatt.services.PersonDetailsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig{

    private final PersonDetailsServices personDetailsServices;

    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests() //
                .requestMatchers("/admin").hasRole("ADMIN")
                //
                .requestMatchers("/authentication", "/error", "/registration", "/resources/**", "/static/**", "/css/**", "/js/**", "/img/**", "/product", "/product/info/{id}", "/product/search").permitAll()
                //
                .anyRequest().hasAnyRole("USER","ADMIN")
                //
                .and() //
                .formLogin().loginPage("/authentication") //
                .loginProcessingUrl("/process_login") //
                .defaultSuccessUrl("/lk", true) //
                .failureUrl("/authentication?error")
                .and()//
                .logout().logoutUrl("/logout").logoutSuccessUrl("/authentication");
    return http.build();
    }
    @Autowired
    public SecurityConfig(PersonDetailsServices personDetailsServices) {
        this.personDetailsServices = personDetailsServices;
    }
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(personDetailsServices)
                .passwordEncoder(getPasswordEncoder());
    }
}
