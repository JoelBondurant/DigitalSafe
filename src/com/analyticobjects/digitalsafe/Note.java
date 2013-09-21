package com.analyticobjects.digitalsafe;

import java.io.Serializable;

/**
 * Notes are persisted text holders.
 * @author Joel Bondurant
 * @since 2013.08
 */
class Note implements Serializable {
    
    static final String PUT = "PUT";
    static final String GET = "GET";
    static final String FIND = "FIND";
    static final String DELETE = "DELETE";
    static final String VERSION = "1";
    
    private final String version;
    private String name;
    private String message;
    
    Note(String name, String message) {
        this.version = VERSION;
        this.name = name;
        this.message = message;
    }
    
    void setName(String name) {
        this.name = name;
    }
    
    String getName() {
        return this.name;
    }
    
    void setMessage(String message) {
        this.message = message;
    }
    
    String getMessage() {
        return this.message;
    }
    
    String fullText() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.message);
        sb.append(" ");
        sb.append(this.name);
        return sb.toString();
    }
}
