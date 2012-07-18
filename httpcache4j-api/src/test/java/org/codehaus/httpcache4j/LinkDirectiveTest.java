package org.codehaus.httpcache4j;

import org.codehaus.httpcache4j.util.DirectivesParser;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Test cases from examples: http://www.w3.org/Protocols/9707-link-header.html
 *
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class LinkDirectiveTest {

    @Test
    public void testFirstExample() {
        String expected = "<http://www.cern.ch/TheBook/chapter2>; rel=\"Previous\"";
        LinkDirective linkDirective = new LinkDirective(DirectivesParser.parse(expected).iterator().next());
        Assert.assertEquals("Previous", linkDirective.getRel());
        Assert.assertEquals(URI.create("http://www.cern.ch/TheBook/chapter2"), linkDirective.getURI());
        Assert.assertEquals(expected, linkDirective.toString());
        Assert.assertNull(linkDirective.getRev());
        Assert.assertNull(linkDirective.getTitle());
        Assert.assertNull(linkDirective.getAnchor());
    }

    @Test
    public void testTestSecondExample() {
        final String expected = "<mailto:timbl@w3.org>; rev=\"Made\"; title=\"Tim Berners-Lee\"";
        LinkDirective linkDirective = new LinkDirective(DirectivesParser.parse(expected).iterator().next());
        Assert.assertNull(linkDirective.getRel());
        Assert.assertNull(linkDirective.getAnchor());
        Assert.assertEquals("Tim Berners-Lee", linkDirective.getTitle());
        Assert.assertEquals("Made", linkDirective.getRev());
        Assert.assertEquals(URI.create("mailto:timbl@w3.org"), linkDirective.getURI());
        Assert.assertEquals(expected, linkDirective.toString());
    }

    @Test
    public void testTestThirdExample() {
        String expected = "<../media/contrast.css>; rel=\"stylesheet alternate\"; title=\"High Contrast Styles\"; type=\"text/css\"; media=\"screen\", <../media/print.css>; rel=\"stylesheet\"; type=\"text/css\"; media=\"print\"";
        Directives directives = DirectivesParser.parse(expected);
        Assert.assertEquals(2, directives.size());
        
        List<LinkDirective> linkDirectives = new ArrayList<LinkDirective>();
        for (Directive directive : directives) {
            linkDirectives.add(new LinkDirective(directive));
        }
        Assert.assertEquals(2, linkDirectives.size());
        LinkDirective linkDirective = linkDirectives.get(0);
        Assert.assertEquals("stylesheet alternate", linkDirective.getRel());
        Assert.assertNull(linkDirective.getAnchor());
        Assert.assertEquals("High Contrast Styles", linkDirective.getTitle());
        Assert.assertEquals("screen", linkDirective.getParameterValue("media"));
        Assert.assertEquals("text/css", linkDirective.getParameterValue("type"));
        Assert.assertEquals(URI.create("../media/contrast.css"), linkDirective.getURI());

        linkDirective = linkDirectives.get(1);
        Assert.assertEquals("stylesheet", linkDirective.getRel());
        Assert.assertNull(linkDirective.getAnchor());
        Assert.assertNull(linkDirective.getTitle());
        Assert.assertEquals("print", linkDirective.getParameterValue("media"));
        Assert.assertEquals("text/css", linkDirective.getParameterValue("type"));
        Assert.assertEquals(URI.create("../media/print.css"), linkDirective.getURI());

        Header header = HeaderUtils.toLinkHeader(linkDirectives);
        Assert.assertEquals(new Header("Link", expected), header);
    }
}
