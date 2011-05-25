package com.temp.afan.data.dao;

public class DAOFactory {

    public StatusDAO getStatusDAO() {
        return new StatusDAO();
    }
    
    public DirectMessageDAO getDirectMessageDAO() {
        return new DirectMessageDAO();
    }
}