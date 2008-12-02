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

import org.apache.commons.lang.Validate;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import org.codehaus.httpcache4j.util.DeletingFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class FileGenerationManager implements Serializable{
    private static final long serialVersionUID = -1558644426181861334L;
    
    private final File baseDirectory;
    private final int generationSize;
    private final int numberOfGenerations;
    private final FileFilter generationFilter;

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

    /**
     * Returns the most recent created generation
     *
     * @return the generation with the highest sequence number
     */
    Generation getCurrentGeneration() {
        return getGenerations().get(0);
    }

    public File getFile(String fileName) {
        File target = new File(getCurrentGeneration().getGenerationDirectory(), fileName);
        for (Generation generation : getGenerations()) {
            File candidate = new File(generation.getGenerationDirectory(), fileName);
            if (candidate.exists()) {
                if (!target.equals(candidate)) {
                    //because of; http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4017593
                    target.delete();
                    if (candidate.renameTo(target)) {
                        return target;
                    }
                }
            }
        }
        return target;
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