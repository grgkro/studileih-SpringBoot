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


    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }
//    @SuppressWarnings("deprecation")
//	@Bean
//    public PasswordEncoder passwordEncoder(){
//        return NoOpPasswordEncoder.getInstance();
//    }


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
                        "/products",
                        "/dorms",
                        "/images/loadProductPicByFilename"

                ).permitAll().anyRequest().authenticated().and().exceptionHandling().and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        http.csrf().disable().authorizeRequests().antMatchers("/authenticate",
//        		"/",
//        		"/images/loadProductPicByFilename",
//        		"/postImage",
//        		"/images/archivePicByFilename",
//        		"/images/restorePicByFilename",
//        		"/images/deleteArchive",
//        		"/images/deleteImageFolder",
//        		"/images/deleteProductPicByFilename",
//        		"/loadProfilePicByUserId",
//        		"/emails/sendEmail",
//        		"/messages/sendMessage",
//        		"/messages/updateMessage",
//        		"/messages/sendReply",
//        		"/messages/sendEmailReply",
//        		"/messages/messages",
//        		"/chats/chats",
//        		"/chats/chatsByUser/{id}",
//        		"/products",
//        		"/products/{id}",
//        		"/productsdto/{id}",
//        		"/products",
//        		"/products/delete/{id}",
//        		"/products/{id}",
//        		"/dorms",
//        		"/users",
//        		"/users/{id}",
//        		"/usersdto/*",
//        		"/saveUser",
//        		"/users",
//        		"/users/{id}",
//        		"/users/*",
//        		"/swagger-ui.html") //for the start, we free up everything, so the application won't break
//        //this part gives permission without authentication
//                .permitAll()
//                //from here on all the rest must be authenticated
//                .anyRequest().authenticated()
//                .and().exceptionHandling().and().sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);;
    }
}
