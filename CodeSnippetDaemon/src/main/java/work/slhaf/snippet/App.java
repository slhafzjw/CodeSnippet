package work.slhaf.snippet;

import lombok.extern.slf4j.Slf4j;
import work.slhaf.snippet.common.Constant;
import work.slhaf.snippet.exception.LaunchCheckException;
import work.slhaf.snippet.gateway.CodeSnippetSocketServer;
import work.slhaf.snippet.service.IndexManager;
import work.slhaf.snippet.service.SnippetManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

@Slf4j
public class App{

    void main() throws IOException, SQLException {
        beforeLaunch();
        CodeSnippetSocketServer server = new CodeSnippetSocketServer();
        server.launch();
    }

    private void beforeLaunch() throws SQLException, IOException {
        log.info("启动前检查环境");
        //检查对应目录是否存在并创建
        checkEnv();
        checkDB();
    }

    private void checkDB() throws SQLException, IOException {
        log.info("检查索引数据库");
        //获取当前数据路径下的文件目录，查看文件路径、sha值与索引数据库是否一致，如果不一致需要重建
        IndexManager indexManager = IndexManager.getInstance();
        SnippetManager snippetManager = new SnippetManager();

        File data = new File(System.getenv(Constant.Property.DIR));
        File[] files = data.listFiles();
        if (files == null || files.length == 0) {
            if (!indexManager.isEmpty()) {
                log.info("数据库为空，但文件目录不为空，删除数据库");
                indexManager.resetIndex();
            }
            return;
        }

        HashMap<String, String> sha2PathDB = indexManager.getIndexStatus();
        HashMap<String, String> sha2PathMD = snippetManager.getFileStatus();
        if (!sha2PathMD.equals(sha2PathDB)) {
            log.info("数据库与文件目录不一致，重建索引数据库");
            indexManager.rebuildIndex();
        }
        log.info("索引数据库检查通过");
    }

    private void checkEnv() {
        log.info("检查环境变量");
        getEnvOrThrow(Constant.Property.PORT);
        getEnvOrThrow(Constant.Property.API_KEY);
        getEnvOrThrow(Constant.Property.BASE_URL);
        getEnvOrThrow(Constant.Property.MODEL);
        checkDir(Constant.Property.DIR);
        checkDir(Constant.Property.CONF);
        log.info("环境变量检查通过");
    }

    private void checkDir(String dir) {
        File file = new File(getEnvOrThrow(dir));
        if (file.exists()) return;

        boolean ok = file.mkdirs();
        if (!ok) {
            throw new LaunchCheckException("创建目录失败: " + dir);
        } else {
            log.info("创建目录成功: {}", dir);
        }

    }

    private String getEnvOrThrow(String key) {
        String value = System.getenv(key);
        if (value == null) throw new LaunchCheckException("未找到环境变量: " + key);
        return value;
    }

}
