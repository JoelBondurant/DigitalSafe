package com.analyticobjects.digitalsafe.containers;

import com.analyticobjects.digitalsafe.database.IndexedTableEntry;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Notes are persisted text holders.
 *
 * @author Joel Bondurant
 * @since 2013.08
 */
public class Note implements IndexedTableEntry {

	private static final long serialVersionUID = 1L;
	public static final String TAG_DELIMITER = ",";
	private long id;
	private String title;
	private String message;
	private final Set<String> tags;
	private final Date createTime;
	private Date updateTime;

	public Note(String title, String message) {
		this.title = title;
		this.message = message;
		this.tags = new HashSet<>();
		this.createTime = new Date();
		this.updateTime = new Date();
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
		return Long.toString(serialVersionUID);
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

	/**
	 * @param aTag Add this tag to the note.
	 */
	public void tag(String aTag) {
		updateTime();
		this.tags.add(aTag.trim());
	}

	/**
	 * Remove a tag from the note.
	 *
	 * @param aTag A tag to remove.
	 * @return True if the tag was removed, false if the tag wasn't found.
	 */
	public boolean unTag(String aTag) {
		if (this.tags.remove(aTag.trim())) {
			updateTime();
			return true;
		}
		return false;
	}

	/**
	 * @return Full text of fields concatenated, useful for searching notes.
	 */
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
		return new Long(this.id).hashCode();
	}

	/**
	 * A lightweight implementation of Note to XML snippit.
	 *
	 * @return An XML snippit representation of the note.
	 */
	public String toXML() {
		StringBuilder xmlSnip = new StringBuilder();
		xmlSnip.append("\t<Note>\n");
		xmlSnip.append("\t\t<Title>").append(this.title).append("</Title>\n");
		xmlSnip.append("\t\t<Message>").append(this.message).append("</Message>\n");
		xmlSnip.append("\t\t<Tags>").append(this.getTagString()).append("</Tags>\n");
		xmlSnip.append("\t</Note>\n");
		return xmlSnip.toString();
	}

	@Override
	public String getIndexId() {
		return this.title.toLowerCase();
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
}
