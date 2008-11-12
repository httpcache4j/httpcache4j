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