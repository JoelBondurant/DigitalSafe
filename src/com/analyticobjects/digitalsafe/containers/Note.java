package com.analyticobjects.digitalsafe.containers;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    private String title;
    private String message;
    private Set<String> tags;
    private final Date createTime;
    private Date updateTime;
    
    Note(String title, String message) {
        this.version = VERSION;
        this.title = title;
        this.message = message;
        this.tags = new HashSet<>();
        this.createTime = new Date();
        this.updateTime = new Date();
    }
    
    void setTitle(String title) {
        updateTime();
        this.title = title;
    }
    
    String getTitle() {
        return this.title;
    }
    
    void setMessage(String message) {
        updateTime();
        this.message = message;
    }
    
    String getMessage() {
        return this.message;
    }
    
    void updateTime() {
        this.updateTime = new Date();
    }
    
    Set<String> getTags() {
        return this.tags;
    }
    
    String getTagString() {
        if (this.tags.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String tag : this.tags) {
            sb.append(tag);
            sb.append(", ");
        }
        return sb.substring(0, sb.length() - 2).toString();
    }
    
    void tag(String aTag) {
        updateTime();
        this.tags.add(aTag);
    }
    
    boolean unTag(String aTag) {
        if (this.tags.remove(aTag)) {
            updateTime();
            return true;
        }
        return false;
    }
    
    String fullText() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.title);
        sb.append(" ");
        sb.append(this.getTagString());
        sb.append(" ");
        sb.append(this.message);
        return sb.toString();
    }
    
}
