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
public class Note implements Serializable {
    
    static final String VERSION = "1";
    public static final String TAG_DELIMITER = ",";
    
    private final String version;
    private String title;
    private String message;
    private Set<String> tags;
    private final Date createTime;
    private Date updateTime;
    
    public Note(String title, String message) {
        this.version = VERSION;
        this.title = title;
        this.message = message;
        this.tags = new HashSet<>();
        this.createTime = new Date();
        this.updateTime = new Date();
    }
    
    public Date getCreateTime() {
        return (Date) this.createTime.clone();
    }
    
    public Date getUpdateTime() {
        return (Date) this.updateTime.clone();
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public void setTitle(String title) {
        updateTime();
        this.title = title;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setMessage(String message) {
        updateTime();
        this.message = message;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    private void updateTime() {
        this.updateTime = new Date();
    }
    
    public Set<String> getTags() {
        return this.tags;
    }
    
    public String getTagString() {
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
    
    public void tagWithTagString(String tags) {
        updateTime();
        this.tags.clear();
        for (String tag : tags.split(TAG_DELIMITER)) {
            this.tags.add(tag);
        }
    }
    
    public void tag(String aTag) {
        updateTime();
        this.tags.add(aTag);
    }
    
    public boolean unTag(String aTag) {
        if (this.tags.remove(aTag)) {
            updateTime();
            return true;
        }
        return false;
    }
    
    public String fullText() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.title);
        sb.append(" ");
        sb.append(this.getTagString());
        sb.append(" ");
        sb.append(this.message);
        return sb.toString();
    }
    
}
