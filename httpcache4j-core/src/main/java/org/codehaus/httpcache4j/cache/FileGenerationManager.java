package org.codehaus.httpcache4j.cache;

import org.apache.commons.lang.Validate;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import org.codehaus.httpcache4j.util.DeletingFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class FileGenerationManager {
    private final File baseDirectory;
    private int generationSize;
    private int numberOfGenerations;
    private FileFilter generationFilter;

    public FileGenerationManager(final File baseDirectory, final int numberOfGenerations) {
        this(baseDirectory, numberOfGenerations, 100);
    }

    public FileGenerationManager(final File baseDirectory, final int numberOfGenerations, final int generationSize) {
        Validate.isTrue(numberOfGenerations > 0, "You may not create 0 generations");
        Validate.notNull(baseDirectory, "You may not have a null base directory");
        if (!baseDirectory.exists()) {
            Validate.isTrue(baseDirectory.mkdirs(), "Could not create base directory: " + baseDirectory);
        }
        this.baseDirectory = baseDirectory;
        this.generationSize = generationSize;
        this.numberOfGenerations = numberOfGenerations;
        generationFilter = new AndFileFilter(DirectoryFileFilter.DIRECTORY, new RegexFileFilter("[0-9]*"));
        getGenerations();
    }

    /**
     * Creates generations of the directories in the base directory.
     *
     * @return the created generations.
     */
    //TODO: Is this heavy?
    //TODO: Maybe we should do this when we miss in getFile() ?
    public List<Generation> getGenerations() {
        final List<Generation> generations = new ArrayList<Generation>();
        //handle existing generations...
        File[] directories = baseDirectory.listFiles(generationFilter);
        if (directories.length > 0) {
            for (File directory : directories) {
                generations.add(new Generation(baseDirectory, Integer.parseInt(directory.getName())));
            }
        }
        else {
            generations.add(new Generation(baseDirectory, 1));
        }
        Collections.sort(generations);
        Generation currentGeneration = generations.get(0);
        if (currentGeneration.getGenerationDirectory().list().length > generationSize) {
            generations.add(0, new Generation(baseDirectory, currentGeneration.getSequence() + 1));
            removeLastGeneration(generations);
        }
        while (generations.size() > numberOfGenerations) {
            removeLastGeneration(generations);
        }
        return Collections.unmodifiableList(generations);
    }

    private void removeLastGeneration(List<Generation> generations) {
        if (generations.size() > numberOfGenerations) {
            Generation generation = generations.remove(generations.size() - 1);
            generation.delete();
        }
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * Returns the most recent created generation
     *
     * @return the generation with the highest sequence number
     */
    public Generation getCurrentGeneration() {
        return getGenerations().get(0);
    }

    public File getFile(String fileName) {
        File target = new File(getCurrentGeneration().getGenerationDirectory(), fileName);
        for (Generation generation : getGenerations()) {
            File candidate = new File(generation.getGenerationDirectory(), fileName);
            if (candidate.exists()) {
                //because of; http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4017593
                if (!target.equals(candidate)) {
                    target.delete();
                    if (candidate.renameTo(target)) {
                        return target;
                    }
                }
                //TODO: what happens if the candidate does not exist?
            }
        }
        return target;
    }
    
    public void removeFile(String fileName) {
        File target = new File(getCurrentGeneration().getGenerationDirectory(), fileName);
        if (target.exists()) {
            target.delete();
        }
        for (Generation generation : getGenerations()) {
            File candidate = new File(generation.getGenerationDirectory(), fileName);
            if (candidate.exists()) {
                candidate.delete();
            }
        }
    }

    static class Generation implements Comparable<Generation> {
        private File generationDirectory;
        private int sequence;

        public Generation(final File baseDir, final int generationNumber) {
            Validate.notNull(baseDir, "Generation directory may not be null");
            File genFile = new File(baseDir, String.valueOf(generationNumber));
            genFile.mkdirs();
            this.generationDirectory = genFile;
            this.sequence = generationNumber;
        }

        public synchronized void delete() {
            File[] undeleteableFiles = generationDirectory.listFiles(new DeletingFileFilter());
            if (undeleteableFiles == null || undeleteableFiles.length == 0) {
                generationDirectory.delete();
            }
            else {
                System.err.println("Unable to delete these files: " + Arrays.toString(undeleteableFiles));
            }
        }

        public File getGenerationDirectory() {
            return generationDirectory;
        }

        public int getSequence() {
            return sequence;
        }

        public int compareTo(Generation generation) {
            return 1 - (sequence - generation.sequence);
        }
    }

}