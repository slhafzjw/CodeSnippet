package work.slhaf.snippet.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import work.slhaf.snippet.common.Constant;
import work.slhaf.snippet.entity.Snippet;
import work.slhaf.snippet.entity.SocketInputData;
import work.slhaf.snippet.entity.SocketOutputData;
import work.slhaf.snippet.entity.db.Index;
import work.slhaf.snippet.entity.file.AddEntity;
import work.slhaf.snippet.entity.file.EditEntity;
import work.slhaf.snippet.entity.file.ListEntity;
import work.slhaf.snippet.entity.file.MetaDataEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class ActionHandler {

    private final SnippetManager snippetManager = new SnippetManager();
    private final IndexManager indexManager = IndexManager.getInstance();
    private final MetaDataExtractor metaDataExtractor = new MetaDataExtractor();

    public ActionHandler() throws SQLException {
    }

    public SocketOutputData handle(SocketInputData inputData) {
        log.info("收到请求: {}", inputData.getAction());
        String data = inputData.getData();
        try {
            return switch (inputData.getAction()) {
                case Constant.Action.ADD -> handleAdd(JSONUtil.toBean(data, AddEntity.class));
                case Constant.Action.EDIT -> handleEdit(JSONUtil.toBean(data, EditEntity.class));
                case Constant.Action.DELETE -> handleDelete(data);
                case Constant.Action.LIST -> handleList(data);
            };
        } catch (Exception e) {
            log.error("处理请求失败: {}", e.getLocalizedMessage());
            return new SocketOutputData(Constant.Status.FAILED, e.getLocalizedMessage());
        }

    }

    private SocketOutputData handleList(String key) throws SQLException {
        List<ListEntity> result = indexManager.list(key);
        return new SocketOutputData(Constant.Status.SUCCESS, JSONUtil.toJsonStr(result));
    }

    private SocketOutputData handleDelete(String path) throws SQLException, IOException {
        if (path.isEmpty()) {
            return new SocketOutputData(Constant.Status.FAILED, "Path不能为空!");
        }
        indexManager.delete(path);
        snippetManager.delete(path);
        return new SocketOutputData(Constant.Status.SUCCESS, "删除成功: " + path);
    }

    private SocketOutputData handleEdit(EditEntity entity) throws IOException {
        if (entity.checkEmpty()) {
            return new SocketOutputData(Constant.Status.FAILED, "Id、Path、代码内容均不能为空!");
        }
        try {
            snippetManager.update(entity, SnippetManager.UpdateAction.EDIT);
            Index index = new Index();
            BeanUtil.copyProperties(entity, index);
            String sha = snippetManager.sha(entity.getPath());
            index.setSha(sha);
            indexManager.update(index);
            snippetManager.update(entity, SnippetManager.UpdateAction.CONFIRM);
            return new SocketOutputData(Constant.Status.SUCCESS, "文件编辑成功: " + entity.getPath());
        } catch (Exception e) {
            snippetManager.update(entity, SnippetManager.UpdateAction.FALLBACK);
            log.error("文件编辑失败, 已回滚: {}", e.getLocalizedMessage());
            return new SocketOutputData(Constant.Status.FAILED, e.getLocalizedMessage());
        }
    }

    private SocketOutputData handleAdd(AddEntity entity) throws IOException {
        if (entity.checkEmpty()) {
            return new SocketOutputData(Constant.Status.FAILED, "Language、Name、代码片段内容均不能为空!");
        }
        Path path = Path.of(System.getenv(Constant.Property.DIR), entity.getLanguage().toLowerCase(), entity.getName() + ".md");
        try {
            Snippet snippet = fixSnippet(entity);
            snippetManager.add(snippet, path);
            Index index = fixIndexEntity(snippet, path);
            index.setName(entity.getName());
            indexManager.add(index);
            return new SocketOutputData(Constant.Status.SUCCESS, "代码片段已添加, 路径: " + path);
        } catch (Exception e) {
            Files.deleteIfExists(path);
            log.error("文件添加失败: {}", e.getLocalizedMessage());
            return new SocketOutputData(Constant.Status.FAILED, e.getLocalizedMessage());
        }
    }

    private Index fixIndexEntity(Snippet snippet, Path path) {
        Index index = new Index();
        BeanUtil.copyProperties(snippet, index);
        index.setPath(path.toString());
        index.setSha(snippetManager.sha(index.getPath()));
        return index;
    }

    private Snippet fixSnippet(AddEntity entity) {
        MetaDataEntity metaData = metaDataExtractor.extract(entity);
        Snippet snippet = new Snippet();
        snippet.setLanguage(entity.getLanguage().toLowerCase());
        snippet.setContent(entity.getContent());
        snippet.setDescription(metaData.getDescription());
        snippet.setTags(metaData.getTags());

        return snippet;
    }
}
