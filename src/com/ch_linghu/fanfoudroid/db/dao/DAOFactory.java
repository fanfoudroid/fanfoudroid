package com.ch_linghu.fanfoudroid.db.dao;

public class DAOFactory {

    public StatusDAO getStatusDAO() {
        return new StatusDAO();
    }
    
    public DirectMessageDAO getDirectMessageDAO() {
        return new DirectMessageDAO();
    }
}