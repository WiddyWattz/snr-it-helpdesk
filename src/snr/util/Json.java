package snr.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Minimal JSON reader/writer for Supabase responses: objects -> Map, arrays -> List.
public final class Json {
    private final String text;
    private int pos;

    private Json(String text) { this.text = text; }

    public static Object parse(String text) {
        Json json = new Json(text);
        Object value = json.readValue();
        json.skipSpaces();
        if (json.pos != text.length()) throw new IllegalArgumentException("Bad JSON at " + json.pos);
        return value;
    }

    public static String write(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (sb.length() > 1) sb.append(',');
            sb.append(quote(e.getKey())).append(':');
            Object v = e.getValue();
            if (v == null) sb.append("null");
            else if (v instanceof Number || v instanceof Boolean) sb.append(v);
            else sb.append(quote(v.toString()));
        }
        return sb.append('}').toString();
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    private Object readValue() {
        skipSpaces();
        char c = text.charAt(pos);
        if (c == '{') return readObject();
        if (c == '[') return readArray();
        if (c == '"') return readString();
        if (text.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
        if (text.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        if (text.startsWith("null", pos)) { pos += 4; return null; }
        return readNumber();
    }

    private Map<String, Object> readObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++;
        skipSpaces();
        if (text.charAt(pos) == '}') { pos++; return map; }
        while (true) {
            skipSpaces();
            String key = readString();
            skipSpaces();
            pos++; // ':'
            map.put(key, readValue());
            skipSpaces();
            if (text.charAt(pos++) == '}') return map;
        }
    }

    private List<Object> readArray() {
        List<Object> list = new ArrayList<>();
        pos++;
        skipSpaces();
        if (text.charAt(pos) == ']') { pos++; return list; }
        while (true) {
            list.add(readValue());
            skipSpaces();
            if (text.charAt(pos++) == ']') return list;
        }
    }

    private String readString() {
        StringBuilder sb = new StringBuilder();
        pos++;
        while (true) {
            char c = text.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c != '\\') { sb.append(c); continue; }
            char e = text.charAt(pos++);
            switch (e) {
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'u': sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16)); pos += 4; break;
                default: sb.append(e);
            }
        }
    }

    private Number readNumber() {
        int start = pos;
        while (pos < text.length() && "+-0123456789.eE".indexOf(text.charAt(pos)) >= 0) pos++;
        String n = text.substring(start, pos);
        if (n.contains(".") || n.contains("e") || n.contains("E")) return Double.parseDouble(n);
        return Long.parseLong(n);
    }

    private void skipSpaces() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
    }
}
