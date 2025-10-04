package work.slhaf.snippet.entity;

import lombok.Data;
import work.slhaf.snippet.common.Constant;

@Data
public class SocketInputData {
    private Constant.Action action;
    private String data;
}
