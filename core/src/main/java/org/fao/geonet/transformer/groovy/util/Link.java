package org.fao.geonet.transformer.groovy.util;

/**
 * Represent a link and the text for the link.
 *
 * @author Jesse on 11/18/2014.
 */
public class Link {
    private final String href;
    private final String text;
    private final String tip;
    private final String cls;

    public Link(String href, String text) {
        this(href, text, null);

    }

    public Link(String href, String text, String cls) {
        this.href = href;
        this.tip = text;
        if (text != null && text.length() > 60) {
            text = text.substring(0, 57) + "...";
        }
        this.text = text;
        this.cls = cls;
    }

    public String getHref() {
        return href == null ? null : href;
    }

    public String getText() {
        return text == null ? null : text;
    }

    public String getTip() {
        return tip;
    }

    public String getCls() {
        return cls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (href != null ? !href.equals(link.href) : link.href != null) return false;
        if (text != null ? !text.equals(link.text) : link.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = href != null ? href.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}
