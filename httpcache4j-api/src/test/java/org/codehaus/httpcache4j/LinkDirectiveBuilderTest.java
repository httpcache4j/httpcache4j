package org.codehaus.httpcache4j;

import org.junit.Test;

import java.net.URI;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class LinkDirectiveBuilderTest {
  @Test
  public void testLinkBuilderWithAllNormalFields() {
      LinkDirectiveBuilder builder = LinkDirectiveBuilder.create(URI.create("foo"));
      LinkDirective linkDirective = builder.title("title").anchor(URI.create("#hey")).rel("rel").rev("rev").build();
      assertEquals("title", linkDirective.getTitle());
      assertEquals(URI.create("#hey"), linkDirective.getAnchor());
      assertEquals("rel", linkDirective.getRel());
      assertEquals("rev", linkDirective.getRev());
      assertEquals(4, linkDirective.getParameters().size());
      String expected = "<foo>; title=\"title\"; anchor=\"#hey\"; rel=\"rel\"; rev=\"rev\"";
      assertEquals(expected, linkDirective.toString());
      assertEquals(new Header("Link", new Directives(Arrays.<Directive>asList(linkDirective))), new Header("Link", expected));
  }
}
