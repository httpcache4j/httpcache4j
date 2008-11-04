/* 
 * $Header: $ 
 * 
 * Copyright (C) 2008 Escenic. 
 * All Rights Reserved.  No use, copying or distribution of this 
 * work may be made except in accordance with a valid license 
 * agreement from Escenic.  This notice must be 
 * included on all copies, modifications and derivatives of this 
 * work. 
 */
package org.codehaus.httpcache4j.util;

import java.io.FileFilter;
import java.io.File;
import java.util.Arrays;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
* @author last modified by $Author: $
* @version $Id: $
*/
public class DeletingFileFilter implements FileFilter {
    public boolean accept(File pathname) {
        if (pathname.isFile()) {
            return !pathname.delete();
        }
        else if (pathname.isDirectory()) {
            File[] files = pathname.listFiles(new DeletingFileFilter());
            if (files.length == 0) {
                return !pathname.delete();
            }
            else {
                System.err.println("Unable to delete these files: " + Arrays.toString(files));
            }
        }
        return true;
    }
}