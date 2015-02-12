package org.codehaus.httpcache4j.payload;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.codehaus.httpcache4j.MIMEType;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class PayloadTest {

    @Test
    public void generateByteArrayFromString() throws IOException {
        String s = String.format("abcdefghijklmnopqrstuvwxyz%s%s%s1234567890", (char)0x00E6, (char)0x00F8, (char)0x00E5);

        ByteArrayPayload bytes = new ByteArrayPayload(s.getBytes(Charsets.UTF_8), MIMEType.APPLICATION_OCTET_STREAM);
        assertEquals(42L, bytes.length());
        assertEquals(s, new String(ByteStreams.toByteArray(bytes.getInputStream()), Charsets.UTF_8));
    }

    @Test
    public void generateFromString() throws IOException {
        String s = String.format("abcdefghijklmnopqrstuvwxyz%s%s%s1234567890", (char)0x00E6, (char)0x00F8, (char)0x00E5);

        StringPayload bytes = new StringPayload(s, MIMEType.APPLICATION_OCTET_STREAM);
        assertEquals(42L, bytes.length());
        assertEquals(s, new String(ByteStreams.toByteArray(bytes.getInputStream()), Charsets.UTF_8));
    }
}
