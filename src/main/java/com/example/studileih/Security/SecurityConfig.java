package com.example.studileih.Security;

//the security configuration itself

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.studileih.Security.CustomUserDetailsService;
import com.example.studileih.Security.JwtFilter;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // Roles for users
    private static final String ROLE_1 = "ADMIN";
    private static final String ROLE_2 = "USER";

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Bean
	public PasswordEncoder passwordEncoder() {
    	return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Override
    //enable Swagger access
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs",
                                   "/configuration/ui",
                                   "/swagger-resources/**",
                                   "/configuration/security",
                                   "/swagger-ui.html",
                                   "/webjars/**");
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //http.csrf().disable().authorizeRequests().antMatchers("/authenticate") //original suggestion, to protect everything, except for these values

		// took me half a day to find this line, which fixes the CORS preflight error: https://www.baeldung.com/spring-security-cors-preflight
		http.cors();

        http.csrf().disable().authorizeRequests()
                .antMatchers("/authenticate",
                        "/users/register",   //without this you would need to be logged in for being able to register...
                        "/products",
                        "/products/{id}",
                        "/dorms",
                        "/images/loadProductPicByFilename",
                "/productsByDorm/{id}",
                "/productsWithouthDormProducts/{id}",
                        "/noAuthNeeded",
                        "/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**"  //needed for swagger
                ).permitAll()
                .antMatchers("/admin").hasRole(ROLE_1)
                .antMatchers("/user").hasAnyRole(ROLE_2, ROLE_1)
                .anyRequest().authenticated().and().exceptionHandling().and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);;
    }
}
