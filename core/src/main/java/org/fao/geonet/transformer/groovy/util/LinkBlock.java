package org.fao.geonet.transformer.groovy.util;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author Jesse on 11/20/2014.
 */
public class LinkBlock {
    public static String CSS_CLASS_PREFIX = "summary-links-";

    public final Multimap<LinkType, Link> linkMap = LinkedHashMultimap.create();
    private String html;

    /**
     * The translation key for the name of this section.
     */
    public final String name;
    /**
     * the classes to put on the link block &lt;i> element. (may be null)
     */
    public final String iconClasses;
    public LinkBlock(String name, String iconClasses) {
        this.name = name;
        this.iconClasses = iconClasses;
    }

    public String getName() throws JDOMException, IOException {
        return name;
    }

    public String getIconClasses() {
        return iconClasses;
    }

    public void put(LinkType type, Link link) {
        final Collection<Link> links = this.linkMap.get(type);
        Link other = null;
        for (Link l : links) {
            if (l.getHref().equalsIgnoreCase(link.getHref())) {
                other = l;
                break;
            }
        }
        if (other != null) {
            if (other.getText().trim().isEmpty()) {
                this.linkMap.remove(type, other);
            } else {
                return;
            }
        }
        this.linkMap.put(type, link);
    }

    public Collection<LinkBlockEntry> getLinks() {
        return Collections2.transform(linkMap.asMap().entrySet(), new Function<Map.Entry<LinkType, Collection<Link>>, LinkBlockEntry>() {
            @Nullable
            @Override
            public LinkBlockEntry apply(Map.Entry<LinkType, Collection<Link>> input) {
                return new LinkBlockEntry(input.getKey(), input.getValue());
            }
        });
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public static final class LinkBlockEntry {
        private final LinkType type;
        private final Collection<Link> links;

        public LinkBlockEntry(LinkType type, Collection<Link> links) {
            this.type = type;
            this.links = links;
        }

        public LinkType getType() {
            return this.type;
        }

        public Collection<Link> getLinks() {
            return this.links;
        }
    }
}
