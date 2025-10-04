package work.slhaf.snippet.entity.file;

import lombok.Data;

@Data
public class RebuildEntity {
    private String name;
    private String path;
    private String sha;

    private String language;
    private String[] tags;
    private String description;

}
