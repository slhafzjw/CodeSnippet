package work.slhaf.snippet.gateway;

import lombok.extern.slf4j.Slf4j;
import work.slhaf.snippet.common.Constant;
import work.slhaf.snippet.service.ActionHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class CodeSnippetSocketServer {

    public void launch() throws IOException, SQLException {
        int port = Integer.parseInt(System.getenv(Constant.Property.PORT));
        ActionHandler handler = new ActionHandler();
        try (ServerSocket socket = new ServerSocket(port);
             ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            socket.setReuseAddress(true);
            while (true) {
                Future<?> future = executor.submit(new ClientSocket(socket.accept(), handler));
                try{
                    future.get();
                }catch (Exception e){
                    log.error(e.getLocalizedMessage());
                }
            }
        }
    }

}
