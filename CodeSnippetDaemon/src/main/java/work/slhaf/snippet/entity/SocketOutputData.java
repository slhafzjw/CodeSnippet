package work.slhaf.snippet.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import work.slhaf.snippet.common.Constant;

@Data
@AllArgsConstructor
public class SocketOutputData {
    private Constant.Status status;
    private String data;
}
