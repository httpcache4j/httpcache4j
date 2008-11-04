package org.codehaus.httpcache4j.cache;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
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
        baseDirectory = PlexusTestCase.getTestFile("target/gen");
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
        assertEquals("File was not equal to found file",tempFile, actual);
    }

    @Test
    public void testCreateNewGeneration() throws IOException {
        FileGenerationManager generationManager = new FileGenerationManager(baseDirectory, 3, 1);
        FileGenerationManager.Generation currentGeneration = generationManager.getCurrentGeneration();
        assertEquals("Wrong generation sequence", 1, currentGeneration.getSequence());
        File dir = currentGeneration.getGenerationDirectory();        
        File.createTempFile("foo", "bar", dir);
        File.createTempFile("foo", "bar", dir);
        currentGeneration = generationManager.getCurrentGeneration();
        assertEquals("Wrong generation sequence", 2, currentGeneration.getSequence());
    }

    @Test
    public void testOverflowGeneration() throws IOException {
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


    @After
    public void cleanup() {
        baseDirectory.listFiles(new DeletingFileFilter());
    }
}