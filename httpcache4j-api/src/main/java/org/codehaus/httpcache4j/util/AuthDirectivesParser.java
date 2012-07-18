package org.codehaus.httpcache4j.util;

import org.codehaus.httpcache4j.AuthDirective;
import org.codehaus.httpcache4j.Directive;
import org.codehaus.httpcache4j.Directives;
import org.codehaus.httpcache4j.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copied from Abdera 2, and then adapted to use.
 *
 */
public final class AuthDirectivesParser {

    private final static String TOKEN = "[\\!\\#\\$\\%\\&\\'\\*\\+\\-\\.\\^\\_\\`\\|\\~a-zA-Z0-9]+";
    private final static String B64 = "([a-zA-Z0-9\\-\\.\\_\\~\\+\\/]+\\=*)";
    private final static String PARAM = TOKEN+"\\s*=\\s*(?:(?:\"(?:(?:\\Q\\\"\\E)|[^\"])*\")|(?:"+TOKEN+"))";
    private final static String PARAMS = "\\s*,?\\s*(" + PARAM + "(?:\\s*,\\s*(?:"+PARAM+")?)*)";
    private final static String B64orPARAM = "(?:" + PARAMS + "|" + B64 + ")";
    private final static String PATTERN = "("+TOKEN+")(?:\\s*" + B64orPARAM + ")?";
    private final static Pattern pattern =
            Pattern.compile(PATTERN);
    private final static Pattern param =
            Pattern.compile("(" + PARAM + ")");


    public static Directives parse(String challenge) {
        checkNotNull(challenge);
        List<Directive> challenges = new ArrayList<Directive>();
        Matcher matcher = pattern.matcher(challenge);
        while (matcher.find()) {
            String scheme = matcher.group(1);
            String params = matcher.group(2);
            params = params != null ? params.replaceAll(",\\s*,", ",").replaceAll(",\\s*,", ",") : null;
            String b64token = matcher.group(3);
            List<Parameter> parameters = new ArrayList<Parameter>();
            if (params != null) {
                Matcher mparams = param.matcher(params);
                while(mparams.find()) {
                    String p = mparams.group(1);
                    String[] ps = p.split("\\s*=\\s*", 2);
                    String name = ps[0];
                    if (name.charAt(name.length()-1)=='*')
                        name = name.substring(0,name.length()-1);
                    parameters.add(DirectivesParser.createParameter(name, ps[1]));
                }
            }
            challenges.add(new AuthDirective(scheme, b64token, parameters));
        }
        return new Directives(challenges);
    }

}
