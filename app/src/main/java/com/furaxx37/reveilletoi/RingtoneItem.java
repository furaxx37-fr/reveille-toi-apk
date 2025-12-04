package com.furaxx37.reveilletoi;

import java.io.Serializable;

public class RingtoneItem implements Serializable {
    private String displayTitle;
    private String uri;
    private boolean isDefault;

    public RingtoneItem() {
        this.displayTitle = "";
        this.uri = "";
        this.isDefault = false;
    }

    public RingtoneItem(String displayTitle, String uri, boolean isDefault) {
        this.displayTitle = displayTitle;
        this.uri = uri;
        this.isDefault = isDefault;
    }

    // Getters
    public String getDisplayTitle() {
        return displayTitle;
    }

    public String getUri() {
        return uri;
    }

    public boolean isDefault() {
        return isDefault;
    }

    // Setters
    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return "RingtoneItem{" +
                "displayTitle='" + displayTitle + '\'' +
                ", uri='" + uri + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RingtoneItem that = (RingtoneItem) obj;
        return uri != null ? uri.equals(that.uri) : that.uri == null;
    }

    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : 0;
    }
}
