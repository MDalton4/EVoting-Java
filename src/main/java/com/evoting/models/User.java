package com.evoting.models;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class User {

    private String name, email, id; //user attributes
    private CopyOnWriteArrayList<Election> ownedElections;  //users owned elections
    private CopyOnWriteArrayList<Election> joinedElections; //users joined elections


    public User(){
    }

    public User(String n, String e, String i) {
        name = n;
        email = e;
        id = i;
        ownedElections = new CopyOnWriteArrayList<>();
        joinedElections = new CopyOnWriteArrayList<>();
    }

    public void addOwned(Election e) {
        ownedElections.add(e);
    }

    public void addJoined(Election e) {
        joinedElections.add(e);
    }

    public CopyOnWriteArrayList<Election> getJoinedElections() {
        return joinedElections;
    }

    public CopyOnWriteArrayList<Election> getOwnedElections() {
        return ownedElections;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

}
