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
public class LinkTest {

    private final DirectivesParser parser = new DirectivesParser();

    @Test
    public void testFirstExample() {
        String expected = "<http://www.cern.ch/TheBook/chapter2>; rel=\"Previous\"";
        Link link = new Link(parser.parse(expected).get(0));
        Assert.assertEquals("Previous", link.getRel());
        Assert.assertEquals(URI.create("http://www.cern.ch/TheBook/chapter2"), link.getURI());
        Assert.assertEquals(expected, link.toString());
        Assert.assertNull(link.getRev());
        Assert.assertNull(link.getTitle());
        Assert.assertNull(link.getAnchor());
    }

    @Test
    public void testTestSecondExample() {
        final String expected = "<mailto:timbl@w3.org>; rev=\"Made\"; title=\"Tim Berners-Lee\"";
        Link link = new Link(parser.parse(expected).get(0));
        Assert.assertNull(link.getRel());
        Assert.assertNull(link.getAnchor());
        Assert.assertEquals("Tim Berners-Lee", link.getTitle());
        Assert.assertEquals("Made", link.getRev());
        Assert.assertEquals(URI.create("mailto:timbl@w3.org"), link.getURI());
        Assert.assertEquals(expected, link.toString());
    }

    @Test
    public void testTestThirdExample() {
        String expected = "<../media/contrast.css>; rel=\"stylesheet alternate\"; title=\"High Contrast Styles\"; type=\"text/css\"; media=\"screen\", <../media/print.css>; rel=\"stylesheet\"; type=\"text/css\"; media=\"print\"";
        List<Directive> directives = parser.parse(expected);
        Assert.assertEquals(2, directives.size());
        
        List<Link> links = new ArrayList<Link>();
        for (Directive directive : directives) {
            links.add(new Link(directive));
        }
        Assert.assertEquals(2, links.size());
        Link link = links.get(0);
        Assert.assertEquals("stylesheet alternate", link.getRel());
        Assert.assertNull(link.getAnchor());
        Assert.assertEquals("High Contrast Styles", link.getTitle());
        Assert.assertEquals("screen", link.getParameterValue("media"));
        Assert.assertEquals("text/css", link.getParameterValue("type"));
        Assert.assertEquals(URI.create("../media/contrast.css"), link.getURI());

        link = links.get(1);
        Assert.assertEquals("stylesheet", link.getRel());
        Assert.assertNull(link.getAnchor());
        Assert.assertNull(link.getTitle());
        Assert.assertEquals("print", link.getParameterValue("media"));
        Assert.assertEquals("text/css", link.getParameterValue("type"));
        Assert.assertEquals(URI.create("../media/print.css"), link.getURI());

        Header header = HeaderUtils.toLinkHeader(links);
        Assert.assertEquals(new Header("Link", expected), header);
    }
}
