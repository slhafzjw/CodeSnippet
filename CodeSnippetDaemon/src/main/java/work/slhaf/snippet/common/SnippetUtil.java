package work.slhaf.snippet.common;

public class SnippetUtil {
    public static String extractJson(String jsonStr) {
        jsonStr = jsonStr.replace("“", "\"").replace("”", "\"");
        int start = jsonStr.indexOf("{");
        int end = jsonStr.lastIndexOf("}");
        if (start != -1 && end != -1 && start < end) {
            return jsonStr.substring(start, end + 1);
        }
        return jsonStr;
    }
}
