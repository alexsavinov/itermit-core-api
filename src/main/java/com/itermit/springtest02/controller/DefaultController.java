package com.itermit.springtest02.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class DefaultController {

    private final ObjectMapper mapper;

    @GetMapping()
    public Map get(Principal principal) {
//        throw new UserNotFoundException(); //
//        Chapter chapter = chapterService.findById(1L);
//        model.addAttribute("message", chapter);

//        return principal.getName();
//        Map<String, Object> attributes = ((DefaultOidcUser) ((OAuth2AuthenticationToken) principal).getPrincipal()).getAttributes();
//        principal.getPrincipal()
//        ((DefaultOidcUser) ((OAuth2AuthenticationToken) principal).getPrincipal()).getAttributes();
//        ((OAuth2AuthenticationToken) principal).getPrincipal()).getAttributes()

//        return principal.toString();
        Map response = new HashMap();

        if (principal instanceof OAuth2AuthenticationToken) {
            Map<String, Object> attributes = ((OAuth2AuthenticationToken) principal).getPrincipal().getAttributes();


            response.put("email", attributes.get("email").toString());
            response.put("name", attributes.get("name").toString());
            response.put("getName", principal.getName());
//            return "email: " + attributes.get("email").toString()
//                    + "<br>name: " + attributes.get("name").toString()
//                    + "<br>getName: " + principal.getName()
//                    + "<br>getDetails: " + ((OAuth2AuthenticationToken) principal).getDetails()
//                    + "<br>getCredentials: " + ((OAuth2AuthenticationToken) principal).getCredentials();
        } else {
//            return principal.getName();
            response.put("name", principal.getName());
        }

        return response;

        /*
         * OAuth2AuthenticationToken
         * [Principal=Name: [113057080214550241998],
         * Granted Authorities: [
         *  [OIDC_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]],
         * User Attributes: [{at_hash=CzTUQLgWWXr4QQRaL661AQ, sub=113057080214550241998, email_verified=true,
         * iss=https://accounts.google.com, given_name=Oleksii, locale=ru, nonce=_WvEHT6WlxfzlTYHlDhQdtE4iCLex0RYzdS09LLSuVI,
         * picture=https://lh3.googleusercontent.com/a/ACg8ocKM42lHUiviYsfYdNJwxEPIJmLxJ2gbgSmUHPEYSh09=s96-c,
         * aud=[673473660667-6hdnvb8rnif8fifm12gh3ib327q3ehb6.apps.googleusercontent.com],
         * azp=673473660667-6hdnvb8rnif8fifm12gh3ib327q3ehb6.apps.googleusercontent.com, name=Oleksii Savinov,
         * exp=2023-10-17T15:51:15Z, family_name=Savinov, iat=2023-10-17T14:51:15Z, email=spell477@gmail.com}],
         * Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1,
         * SessionId=6EF0F701702613C2F267C5E39E799CB9], Granted Authorities=[OIDC_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email,
         * SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]]
         *
         * */
    }
}
