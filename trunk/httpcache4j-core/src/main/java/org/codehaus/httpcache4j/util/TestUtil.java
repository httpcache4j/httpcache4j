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

import java.io.File;

/**
 * Copied from PlexusTestCase.
 * original authors:
 * <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * <a href="mailto:michal@codehaus.org">Michal Maczka</a>
 *
 * This utility class makes us able to create temporary directories.
 * This should only be used by Test cases.
 * It is placed in main so we don't have to depend on the test sources.
 * 
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class TestUtil {
    private static String basedirPath;

    public static File getTestFile(String path) {
        return new File(getBasedir(), path);
    }

    public static File getTestFile(String basedir, String path) {
        File basedirFile = new File(basedir);

        if (!basedirFile.isAbsolute()) {
            basedirFile = getTestFile(basedir);
        }

        return new File(basedirFile, path);
    }

    public static String getTestPath(String path) {
        return getTestFile(path).getAbsolutePath();
    }

    public static String getTestPath(String basedir, String path) {
        return getTestFile(basedir, path).getAbsolutePath();
    }

    public static String getBasedir() {
        if (basedirPath != null) {
            return basedirPath;
        }

        basedirPath = System.getProperty("basedir");

        if (basedirPath == null) {
            basedirPath = new File("").getAbsolutePath();
        }

        return basedirPath;
    }
}
