package com.foco.boot.mongo.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lucoo
 * @version 1.0.0
 * @Description TODO
 * @date 2021-07-21 16:29
 */
public class FormatUtils {
    public static Pattern regex = Pattern.compile("\\$\\{([^}]*)\\}");

    public FormatUtils() {
    }

    public static String bson(String json) {
        json = transString(json);
        String blank = "    ";
        String indent = "";
        StringBuilder sb = new StringBuilder();
        char[] var4 = json.toCharArray();
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            char c = var4[var6];
            switch(c) {
                case ',':
                    sb.append(",\n").append(indent);
                    break;
                case '[':
                    indent = indent + blank;
                    sb.append("[\n").append(indent);
                    break;
                case ']':
                    indent = indent.substring(0, indent.length() - blank.length());
                    sb.append("\n").append(indent).append("]");
                    break;
                case '{':
                    indent = indent + blank;
                    sb.append("{\n").append(indent);
                    break;
                case '}':
                    indent = indent.substring(0, indent.length() - blank.length());
                    sb.append("\n").append(indent).append("}");
                    break;
                default:
                    sb.append(c);
            }
        }

        return sb.toString();
    }

    private static String transString(String str) {
        str = str.replace(", ", ",").replace("{\"$oid\":", "${");
        List<String> temp = getContentInfo(str);

        String tp;
        for(Iterator var2 = temp.iterator(); var2.hasNext(); str = str.replace("${" + tp + "}", "ObjectId(" + tp + ")")) {
            tp = (String)var2.next();
        }

        return str;
    }

    private static List<String> getContentInfo(String content) {
        Matcher matcher = regex.matcher(content);
        ArrayList list = new ArrayList();

        while(matcher.find()) {
            list.add(matcher.group(1));
        }

        return list;
    }
}

