package work.slhaf.snippet.common.chat.pojo;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @NonNull
    private String role;
    @NonNull
    private String content;
}
