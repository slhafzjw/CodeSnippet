package work.slhaf.snippet.gateway;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import work.slhaf.snippet.common.Constant;
import work.slhaf.snippet.entity.SocketInputData;
import work.slhaf.snippet.entity.SocketOutputData;
import work.slhaf.snippet.service.ActionHandler;

import java.io.*;
import java.net.Socket;

@Slf4j
public record ClientSocket(Socket socket, ActionHandler handler) implements Runnable {

    @Override
    public void run() {
        System.out.println("建立连接: " + socket.getRemoteSocketAddress());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            SocketInputData inputData = JSONUtil.toBean(sb.toString(), SocketInputData.class);
            SocketOutputData outputData = handler.handle(inputData);

            writer.write(JSONUtil.toJsonStr(outputData));
            writer.newLine();
            writer.flush();

        } catch (Exception e) {
            SocketOutputData outputData = new SocketOutputData(Constant.Status.FAILED, e.getLocalizedMessage());
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(JSONUtil.toJsonStr(outputData));
                writer.newLine();
                writer.flush();
            } catch (IOException ex) {
                log.error(ex.getLocalizedMessage());
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.error(e.getLocalizedMessage());
            }
        }
    }

}
