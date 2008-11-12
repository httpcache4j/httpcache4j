package org.codehaus.httpcache4j.util;

import java.io.FileFilter;
import java.io.File;
import java.util.Arrays;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class DeletingFileFilter implements FileFilter {
    public boolean accept(File pathname) {
        if (pathname.isFile()) {
            return !pathname.delete();
        }
        else if (pathname.isDirectory()) {
            File[] files = pathname.listFiles(this); // Optimization: No need to create new filter, as there's no state
            if (files == null || files.length == 0) {
                return !pathname.delete();
            }
            else {
                System.err.println("Unable to delete these files: " + Arrays.toString(files));
            }
        }
        return true;
    }
}