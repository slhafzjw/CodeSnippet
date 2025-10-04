package work.slhaf.snippet.entity.file;

import lombok.Data;

@Data
public class EditEntity {
    private String id;
    private String path;

    private String[] tags;
    private String description;

    private String content;

    public boolean checkEmpty() {
        return id.isEmpty() ||
                path.isEmpty() ||
                content.isEmpty();
    }
}
