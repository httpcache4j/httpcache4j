/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
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
 *
 */

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.util.DeletingFileFilter;
import org.codehaus.httpcache4j.util.TestUtil;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.io.File;
import java.io.IOException;

public class FileGenerationManagerTest {
    private File baseDirectory;

    @Before
    public void init() {
        baseDirectory = TestUtil.getTestFile("target/gen");
    }

    @Test
    public void testCreationOfFoldersIsCorrect() {
        FileGenerationManager generationManager = new FileGenerationManager(baseDirectory, 1);
        assertEquals("Wrong number of generations", 1, generationManager.getGenerations().size());
        assertEquals("Wrong number in sequence", 1, generationManager.getCurrentGeneration().getSequence());
        assertEquals("Wrong number of directories", 1, baseDirectory.list().length);
    }

    @Test
    public void testSearchForFileWhichDoesNotExist() {
        FileGenerationManager generationManager = new FileGenerationManager(baseDirectory, 1);
        File file = generationManager.getFile("fooooobbarrrr");
        assertTrue("File did exist", !file.exists());
    }

    @Test
    public void testSearchForFile() throws IOException {
        FileGenerationManager generationManager = new FileGenerationManager(baseDirectory, 1);
        File tempFile = File.createTempFile("foo", "bar", generationManager.getCurrentGeneration().getGenerationDirectory());
        assertTrue("File did not exist", tempFile.exists());
        File actual = generationManager.getFile(tempFile.getName());
        assertTrue("File did not exist", actual.exists());
        assertEquals("File was not equal to found file", tempFile, actual);
    }

    @Test
    public void testCreateNewGeneration() throws IOException {
        FileGenerationManager generationManager = new FileGenerationManager(baseDirectory, 2, 1);
        FileGenerationManager.Generation currentGeneration = generationManager.getCurrentGeneration();
        assertEquals("Wrong generation sequence", 1, currentGeneration.getSequence());
        File dir = currentGeneration.getGenerationDirectory();
        File.createTempFile("foo", "bar", dir);
        File.createTempFile("foo", "bar", dir);
        currentGeneration = generationManager.getCurrentGeneration();
        assertEquals("Wrong generation sequence", 2, currentGeneration.getSequence());
        assertEquals("Wrong number of generations", 2, generationManager.getGenerations().size());
    }

    @Test
    public void testOverflowGeneration() throws IOException {
        FileGenerationManager generationManager = new FileGenerationManager(baseDirectory, 2, 1);
        FileGenerationManager.Generation currentGeneration = generationManager.getCurrentGeneration();
        assertEquals("Wrong generation sequence", 1, currentGeneration.getSequence());
        File.createTempFile("foo", "bar", currentGeneration.getGenerationDirectory());
        File.createTempFile("foo", "bar", currentGeneration.getGenerationDirectory());
        currentGeneration = generationManager.getCurrentGeneration();
        File.createTempFile("foo", "bar", currentGeneration.getGenerationDirectory());
        File.createTempFile("foo", "bar", currentGeneration.getGenerationDirectory());
        assertEquals("Wrong generation sequence", 2, currentGeneration.getSequence());
        assertEquals("Wrong number of generations", 2, generationManager.getGenerations().size());
        currentGeneration = generationManager.getCurrentGeneration();
        assertEquals("Wrong generation sequence", 3, currentGeneration.getSequence());
        assertEquals("Wrong number of generations", 2, generationManager.getGenerations().size());
    }

    @After
    public void cleanup() {
        baseDirectory.listFiles(new DeletingFileFilter());
    }
}