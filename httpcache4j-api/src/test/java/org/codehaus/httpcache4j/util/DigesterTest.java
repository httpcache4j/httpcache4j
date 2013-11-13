package org.codehaus.httpcache4j.util;

import org.junit.Test;

import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class DigesterTest {

    private Charset charset = Charset.forName("UTF-8");


    @Test
    public void md5() {
        String hello = Digester.md5("hello", charset);
        assertThat(hello, equalTo("5d41402abc4b2a76b9719d911017c592"));
    }

    @Test
    public void sha1() {
        String hello = Digester.sha1("hello", charset);
        assertThat(hello, equalTo("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d"));
    }

    @Test
    public void sha256() {
        String hello = Digester.sha256("hello", charset);
        assertThat(hello, equalTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));
    }
}
