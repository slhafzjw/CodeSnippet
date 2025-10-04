package work.slhaf.snippet.entity.file;

import lombok.Data;

@Data
public class ListEntity implements Comparable<ListEntity> {
    private String id;
    private String path;
    private String name;
    private int score;

    @Override
    public int compareTo(ListEntity listEntity) {
        return Integer.compare(listEntity.getScore(), this.score);
    }
}
