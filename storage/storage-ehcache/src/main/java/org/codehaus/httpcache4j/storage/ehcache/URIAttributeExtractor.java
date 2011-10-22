package org.codehaus.httpcache4j.storage.ehcache;

import net.sf.ehcache.Element;
import net.sf.ehcache.search.attribute.AttributeExtractor;
import net.sf.ehcache.search.attribute.AttributeExtractorException;
import org.codehaus.httpcache4j.cache.Key;

import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: maedhros
 * Date: 10/22/11
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class URIAttributeExtractor implements AttributeExtractor {
    public String attributeFor(Element element, String attributeName) throws AttributeExtractorException {
         if ("uri".equals(attributeName)) {
            Key key = (Key) element.getKey();
            return key.getURI().normalize().toString();
        }
        return null;
    }
}
