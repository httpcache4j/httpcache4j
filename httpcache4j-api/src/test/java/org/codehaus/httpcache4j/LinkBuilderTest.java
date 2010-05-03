package org.codehaus.httpcache4j;

import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class LinkBuilderTest {
  @Test
  public void testLinkBuilderWithAllNormalFields() {
      LinkBuilder builder = LinkBuilder.create(URI.create("foo"));
      Link link = builder.title("title").anchor(URI.create("#hey")).rel("rel").rev("rev").build();
      assertEquals("title", link.getTitle());
      assertEquals(URI.create("#hey"), link.getAnchor());
      assertEquals("rel", link.getRel());
      assertEquals("rev", link.getRev());
      assertEquals(4, link.getParameters().size());
      String expected = "<foo>; title=\"title\"; anchor=\"#hey\"; rel=\"rel\"; rev=\"rev\"";
      assertEquals(expected, link.toString());
      assertEquals(new Header("Link", new Directives(Arrays.<Directive>asList(link))), new Header("Link", expected));
  }
}
