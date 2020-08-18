package com.example.studileih.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiResponses;
//import io.swagger.annotations.ApiResponse;

import com.example.studileih.Security.AuthRequest;
import com.example.studileih.Security.JwtUtil;

@RestController
@CrossOrigin
@Api(tags = "Authentication API - controller methods for authenticating Users")
public class AuthenticationController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/")
    @ApiOperation(value = "Dummy welcome message")
    public String welcome() {
        return "Welcome to StudiLeih !!!";
    }

    @PostMapping("/authenticate")
    @ApiOperation(value = "Authenticate user by username and password, return the JWT Token")
    public String generateToken(String username, String password) throws Exception {
    	System.out.println("Username: " + username + ", Password: " + password);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (Exception ex) {
            throw new Exception("inavalid username/password");
        }
        String token = jwtUtil.generateToken(username);
        System.out.println(token);
        return token;
    }
}