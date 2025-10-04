package work.slhaf.snippet.entity.file;

import lombok.Data;

@Data
public class AddEntity {
    private String name;
    private String language;
    private String content;

    public boolean checkEmpty(){
        return name.isEmpty() || language.isEmpty() || content.isEmpty();
    }
}
