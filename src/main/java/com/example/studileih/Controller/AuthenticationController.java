package com.example.studileih.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiResponses;
//import io.swagger.annotations.ApiResponse;

import com.example.studileih.Security.AuthRequest;
import com.example.studileih.Security.JwtUtil;

@RestController
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
    public String generateToken(@RequestBody AuthRequest authRequest) throws Exception {
    	System.out.println("Username: " + authRequest.getUserName() + ", Password: " + authRequest.getPassword());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
            );
        } catch (Exception ex) {
            throw new Exception("inavalid username/password");
        }
        return jwtUtil.generateToken(authRequest.getUserName());
    }
}