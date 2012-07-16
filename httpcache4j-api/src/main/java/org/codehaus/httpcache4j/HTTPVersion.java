package org.codehaus.httpcache4j;


import org.codehaus.httpcache4j.util.NumberUtils;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public enum HTTPVersion {
    HTTP_1_1(1, 1),
    HTTP_1_0(1, 0);
    private final int major;
    private final int minor;

    HTTPVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }



    public static HTTPVersion get(String version) {
        if (version != null && version.length() == 3) {
            int major = NumberUtils.toInt(version.substring(0, 1), 1);
            int minor = NumberUtils.toInt(version.substring(2), 1);
            if (HTTP_1_1.getMajor() == major && HTTP_1_1.getMinor() == minor) {
                return HTTP_1_1;
            }
            else if (HTTP_1_0.getMajor() == major && HTTP_1_0.getMinor() == minor) {
                return HTTP_1_0;
            }
        }
        return HTTP_1_1;
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }
}
