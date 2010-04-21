package org.codehaus.httpcache4j;

import org.junit.Test;

import java.net.URI;

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
  }
}
