package com.evoting.controllers;

import com.evoting.models.User;
import com.evoting.models.Election;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.*;

@Controller
public class MainController {

    @Resource
    private ConcurrentHashMap<String, User> users;  //users that have logged in before

    @Resource
    private ConcurrentHashMap<String, Election> elections;  //created elections

    /**
     * Method for displaying users home page
     * @param model thymeleaf model for displaying info
     * @param auth token for logged in user
     * @return home page
     */
    @GetMapping("/index")
    public String displayHome(Model model, OAuth2AuthenticationToken auth) {

        String email = auth.getPrincipal().getAttributes().get("email").toString(); //current users email
        String name = auth.getPrincipal().getAttributes().get("given_name").toString(); //current users name

        String currentUser= email;  //current user

        CopyOnWriteArrayList<Election> usrOwnedElec;    //users owned elections
        CopyOnWriteArrayList<Election> usrJoinedElec;   //users joined elections
        ConcurrentHashMap<Election, Boolean> ownedElections = new ConcurrentHashMap<>();    //users owned elections and if they have voted
        ConcurrentHashMap<Election, Boolean> joinedElections = new ConcurrentHashMap<>();   //users joined elections and if they have voted

        model.addAttribute("name", name);

        //if the user doesn't already exist, add them to the concurrent hash map of users
        if(!users.containsKey(email)) {
            String id = auth.getPrincipal().getAttributes().get("sub").toString();
            User newUser = new User(name, email, id);
            users.put(email, newUser);
        }

        //get their elections
        usrOwnedElec = users.get(email).getOwnedElections();
        usrJoinedElec = users.get(email).getJoinedElections();

        //for each of their elections, get it and if they have voted
        for(int i=0; i<usrOwnedElec.size(); i++) {
            ownedElections.put(usrOwnedElec.get(i), usrOwnedElec.get(i).hasVoted(users.get(currentUser)));
        }
        for(int j=0; j<usrJoinedElec.size(); j++) {
            joinedElections.put(usrJoinedElec.get(j), usrJoinedElec.get(j).hasVoted(users.get(currentUser)));
        }

        //for each of their owned elections display them or display message saying they dont have any
        if(!usrOwnedElec.isEmpty()) {
            model.addAttribute("owned", ownedElections);

        } else {
            model.addAttribute("ownEmpty", "Not currently running any elections");
        }

        //for each of their joined elections display them or display message saying they dont have any
        if(!usrJoinedElec.isEmpty()) {
            model.addAttribute("joined", joinedElections);

        } else {
            model.addAttribute("joinEmpty", "Not currently in any elections");
        }

        return "index";
    }

    /**
     * Method for creating an election
     * @param request form data from page
     * @param auth token for logged in user
     * @return index page
     */
    @PostMapping("/create")
    public String createElection(HttpServletRequest request, OAuth2AuthenticationToken auth) {

        String currentUser = auth.getPrincipal().getAttributes().get("email").toString();   //get current user

        Map<String, String[]> input = request.getParameterMap();    //get their form data
        String owner = currentUser; //set owner
        String title = input.get("electionTitle")[0];   //set title
        CopyOnWriteArrayList<String> cands = new CopyOnWriteArrayList<>();  //candidates list

        //for each candidate, add to list
        for(int i=0; i<input.get("candidate").length; i++) {
            cands.add(input.get("candidate")[i]);
        }

        //create election, add the user to it
        Election e = new Election(currentUser, title, cands);
        e.joinElection(users.get(currentUser));
        users.get(currentUser).addOwned(e);

        //add election to elections concurrent hash map
        elections.put(e.getEid(), e);

        return "index";
    }

    /**
     * Method for joining an election
     * @param request form data from html
     * @param auth token for logged in user
     * @return home page
     */
    @PostMapping("/join")
    public String joinElection(HttpServletRequest request, OAuth2AuthenticationToken auth) {

        String currentUser = auth.getPrincipal().getAttributes().get("email").toString();   //get current user

        Map<String, String[]> input = request.getParameterMap();    //get their form input
        String inputCode = input.get("joinCode")[0];    //get their code

        //for each election
        for(ConcurrentHashMap.Entry<String, Election> entry : elections.entrySet()) {
            Election entryElec = entry.getValue();

            //if the code entered matches any
            if(entryElec.getCode().equals(inputCode)) {
                //if the user is not already in the election
                if(!entryElec.getParticipants().containsKey(users.get(currentUser))) {
                    //add the user to the election and add the election to the users joined elections
                    users.get(currentUser).addJoined(entryElec);
                    elections.get(entryElec.getEid()).joinElection(users.get(currentUser));
                }
            }
        }

        return "index";
    }

}
