/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.util.NullInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.After;
import org.codehaus.httpcache4j.util.TestUtil;
import org.codehaus.httpcache4j.util.DeletingFileFilter;

import java.net.URI;
import java.io.IOException;
import java.io.File;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class FileManagerTest {
    private FileManager fileManager;
    private File testFile;

    @Before
    public void setUp() {
        testFile = TestUtil.getTestFile("target/test");
        fileManager = new FileManager(testFile);
    }

    @Test
    public void testResolveToFile() throws IOException {
        File file = fileManager.createFile(Key.create(URI.create("foo"), new Vary()), new NullInputStream(1));
        assertNotNull("File was null", file);
        assertEquals(1, file.length());
    }

    @Test
    public void testResolveToEmptyFile() throws IOException {
      File file = fileManager.createFile(Key.create(URI.create("foo"), new Vary()), new NullInputStream(0));
      assertNull("File was not null", file);
    }
    
    @Test
    public void verifyResolve() {
        File file = fileManager.resolve(URI.create("http://hello.you.bastard.com"));
        assertFalse(file.exists());
        assertTrue(file.getAbsolutePath().endsWith("files/57/57b16f3b10b61ed6ce07a15268874bc6"));
    }


    @After
    public void tearDown() {
        testFile.listFiles(new DeletingFileFilter());
    }
}
