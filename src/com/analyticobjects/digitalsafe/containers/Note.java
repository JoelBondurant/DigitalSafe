package com.analyticobjects.digitalsafe.containers;

import java.io.Serializable;
import java.util.Collections;
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
    
    private int id;
    private final String version;
    private String title;
    private String message;
    private final Set<String> tags;
    private final Date createTime;
    private Date updateTime;
    
    public Note(String title, String message) {
        this.id = -1;
        this.version = VERSION;
        this.title = title;
        this.message = message;
        this.tags = new HashSet<>();
        this.createTime = new Date();
        this.updateTime = new Date();
    }
    
    public boolean needsId() {
        return (this.id == -1);
    }
    
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        updateTime();
        this.id = id;
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
    
    void updateTime() {
        this.updateTime = new Date();
    }
    
    public Set<String> getTags() {
        return Collections.unmodifiableSet(this.tags);
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
            this.tags.add(tag.trim());
        }
    }
    
    public void tag(String aTag) {
        updateTime();
        this.tags.add(aTag.trim());
    }
    
    public boolean unTag(String aTag) {
        if (this.tags.remove(aTag.trim())) {
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
        sb.append(" ");
        sb.append(this.createTime.toString());
        sb.append(" ");
        sb.append(this.updateTime.toString());
        return sb.toString();
    }
    

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Note)) {
            return false;
        }
        Note other = ((Note) obj);
        return ((this.id == other.id) && (this.getClass().equals(other.getClass())));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.id;
        return hash;
    }
}
