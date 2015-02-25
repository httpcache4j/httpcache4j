package org.codehaus.httpcache4j.payload;

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.util.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.*;

public class PayloadTest {
    String s2 = "abcdefghijklmnopqrstuvwxyzæøå1234567890";
    String s = String.format("abcdefghijklmnopqrstuvwxyz%s%s%s1234567890", (char)0x00E6, (char)0x00F8, (char)0x00E5);

    @Test
    public void generateByteArrayFromString() throws IOException {

        ByteArrayPayload bytes = new ByteArrayPayload(s.getBytes(StandardCharsets.UTF_8), MIMEType.APPLICATION_OCTET_STREAM);
        assertEquals(42L, bytes.length());
        String actual = new String(IOUtils.toByteArray(bytes.getInputStream()), StandardCharsets.UTF_8);
        assertEquals(s, actual);
        assertEquals(s2, actual);
    }

    @Test
    public void generateFromString() throws IOException {
        StringPayload bytes = new StringPayload(s, MIMEType.APPLICATION_OCTET_STREAM);
        assertEquals(42L, bytes.length());
        String actual = new String(IOUtils.toByteArray(bytes.getInputStream()), StandardCharsets.UTF_8);
        assertEquals(s, actual);
        assertEquals(s2, actual);
    }

    @Test
    public void binaryStream() throws IOException {
        byte[] arr = new byte[2048];
        Random rand = new Random();
        rand.nextBytes(arr);
        ByteArrayPayload p = new ByteArrayPayload(arr, MIMEType.APPLICATION_OCTET_STREAM);
        byte[] actual = IOUtils.toByteArray(p.getInputStream());
        assertEquals(2048, p.length());
        assertArrayEquals(arr, actual);
    }
}
