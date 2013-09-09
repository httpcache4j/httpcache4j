package org.codehaus.httpcache4j;

import com.google.common.base.Splitter;
import org.codehaus.httpcache4j.util.URIDecoder;
import org.codehaus.httpcache4j.util.URIEncoder;

import java.text.Collator;
import java.util.*;

public class Parameters implements Iterable<Parameter> {
    private final Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();

    public Parameters() {
        this(Collections.<String, List<String>>emptyMap());
    }

    public Parameters(Map<String, List<String>> parameters) {
        this.parameters.putAll(parameters);
    }

    public Parameters(Iterable<Parameter> parameters) {
        this(toMap(parameters));
    }

    public Parameters empty() {
        return new Parameters();
    }

    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    public boolean contains(String name) {
        return parameters.containsKey(name);
    }

    public boolean contains(String name, String value) {
        return contains(new Parameter(name, value));
    }

    public boolean contains(Parameter parameter) {
        List<String> values = parameters.get(parameter.getName());
        return values != null && values.contains(parameter.getValue());
    }

    public Parameters add(String name, String value) {
        return add(new Parameter(name, value));
    }

    public Parameters add(Parameter param) {
        return add(Arrays.asList(param));
    }

    public Parameters add(List<Parameter> params) {
        Map<String, List<String>> map = toMap(params);
        return add(map);
    }

    public Parameters add(Map<String, List<String>> params) {
        LinkedHashMap<String, List<String>> copy = copy();
        copy.putAll(params);
        return new Parameters(copy);
    }

    public Parameters set(Map<String, List<String>> params) {
        return new Parameters(params);
    }

    public Parameters set(Iterable<Parameter> params) {
        Map<String, List<String>> map = toMap(params);
        return new Parameters(map);
    }

    public Parameters set(String name, String value) {
        LinkedHashMap<String, List<String>> copy = copy();
        copy.remove(name);
        if (value != null) {
            ArrayList<String> list = new ArrayList<String>();
            list.add(value);
            copy.put(name, list);
        }
        return new Parameters(copy);
    }

    public Parameters set(String name, List<String> value) {
        LinkedHashMap<String, List<String>> copy = copy();
        copy.remove(name);
        if (!value.isEmpty()) {
            ArrayList<String> list = new ArrayList<String>();
            list.addAll(value);
            copy.put(name, list);
        }
        return new Parameters(copy);
    }

    public List<String> get(String name) {
        List<String> list = parameters.get(name);
        if (list != null) {
            return Collections.unmodifiableList(list);
        }
        return Collections.emptyList();
    }

    public String getFirst(String name) {
        List<String> values = get(name);
        if (!values.isEmpty()) return values.get(0);
        return null;
    }

    public Parameters remove(String name) {
        if (!parameters.containsKey(name)) {
            return this;
        }
        LinkedHashMap<String, List<String>> copy = copy();
        copy.remove(name);
        return new Parameters(copy);
    }

    public List<Parameter> asList() {
        List<Parameter> list = new ArrayList<Parameter>();
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            for (String value : entry.getValue()) {
                list.add(new Parameter(entry.getKey(), value));
            }
        }
        return Collections.unmodifiableList(list);
    }

    public Map<String, List<String>> asMap() {
        return Collections.unmodifiableMap(parameters);
    }

    public String toQuery(boolean sort) {
        StringBuilder builder = new StringBuilder();
        List<Parameter> params = new ArrayList<Parameter>(asList());
        if (sort) {
            Collections.sort(params, new Comparator<Parameter>() {
                @Override
                public int compare(Parameter o1, Parameter o2) {
                    return Collator.getInstance(Locale.getDefault()).compare(o1.getName(), o2.getName());
                }
            });
        }
        for (Parameter parameter : params) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            String value = parameter.getValue();
            builder.append(parameter.getName()).append("=").append(URIEncoder.encodeUTF8(value));
        }
        if (builder.length() == 0) {
            return null;
        }
        return builder.toString();
    }

    private LinkedHashMap<String, List<String>> copy() {
        return new LinkedHashMap<String, List<String>>(this.parameters);
    }

    @Override
    public Iterator<Parameter> iterator() {
        return asList().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameters that = (Parameters) o;

        if (!parameters.equals(that.parameters)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return parameters.hashCode();
    }


    public static Parameters parse(String query) {
        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        if (query != null) {
            Iterable<String> parts = Splitter.on("&").omitEmptyStrings().trimResults().split(query);
            for (String part : parts) {
                String[] equalParts = part.split("=");
                String name = null;
                String value = null;
                if (equalParts.length == 1) {
                    name = equalParts[0];
                }
                else if (equalParts.length == 2) {
                    name = equalParts[0];
                    value = equalParts[1];
                }
                if (name != null) {
                    addToQueryMap(map, URIDecoder.decodeUTF8(name), URIDecoder.decodeUTF8(value));
                }
            }
        }

        return new Parameters(map);
    }

    private static Map<String, List<String>> toMap(Iterable<Parameter> parameters) {
        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        for (Parameter parameter : parameters) {
            addToQueryMap(map, parameter.getName(), parameter.getValue());
        }
        return map;
    }

    private static void addToQueryMap(Map<String, List<String>> map, String name, String value) {
        List<String> list = map.get(name);
        if (list == null) {
            list = new ArrayList<String>();
        }
        if (value != null) {
            list.add(value);
        }
        map.put(name, list);
    }
}
