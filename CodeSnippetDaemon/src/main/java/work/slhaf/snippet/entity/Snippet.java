package work.slhaf.snippet.entity;

import lombok.Data;

/**
 * 片段完整数据，存储至完整的markdown文件中
 * 而数据库条目只需要元信息即可
 */
@Data
public class Snippet {

    private String language;
    private String[] tags;
    private String description;

    private String content;

    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();

        // Add Snippet content
        sb.append("## Snippet\n");
        sb.append("```").append(language).append("\n");
        sb.append(content).append("\n");
        sb.append("```\n\n");

        // Add MetaData section
        sb.append("## MetaData\n");
        sb.append("- Language\n");
        sb.append("  - ").append(language).append("\n");

        sb.append("- Tags\n");
        for (String tag : tags) {
            sb.append("  - ").append(tag).append("\n");
        }

        sb.append("- Description\n");
        sb.append("  - ").append(description).append("\n");

        return sb.toString();
    }
}
