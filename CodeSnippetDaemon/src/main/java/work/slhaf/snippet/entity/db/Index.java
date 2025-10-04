package work.slhaf.snippet.entity.db;

import lombok.Data;

@Data
public class Index {
    private int id;
    private String name;
    private String language;
    private String[] tags;
    private String sha;
    private String description;
    private String path;
}
