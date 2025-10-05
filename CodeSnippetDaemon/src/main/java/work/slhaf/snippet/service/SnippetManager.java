package work.slhaf.snippet.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import work.slhaf.snippet.common.Constant;
import work.slhaf.snippet.entity.Snippet;
import work.slhaf.snippet.entity.file.EditEntity;
import work.slhaf.snippet.entity.file.RebuildEntity;
import work.slhaf.snippet.exception.SnippetManagerException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class SnippetManager {

    private final SnippetReader snippetReader = new SnippetReader();

    public HashMap<String, String> getFileStatus() {
        File file = new File(System.getenv(Constant.Property.DIR));
        HashMap<String, String> map = new HashMap<>();
        listFileStatus(file, map);
        return map;
    }

    private void listFileStatus(File file, HashMap<String, String> map) {
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                listFileStatus(f, map);
            } else {
                String sha = DigestUtil.sha256Hex(f);
                map.put(f.getAbsolutePath(), sha);
            }
        }
    }

    public void add(Snippet snippet, Path path) throws IOException {
        String markdown = snippet.toMarkdown();
        File file = path.toFile();
        if (file.exists()) {
            throw new SnippetManagerException("文件已存在: " + file.getAbsolutePath());
        }
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), markdown, StandardCharsets.UTF_8);
    }

    public void update(EditEntity entity, UpdateAction action) throws IOException {
        log.info("编辑操作: {}, {}", entity.getPath(), action);
        switch (action) {
            case EDIT -> edit(entity);
            case CONFIRM -> confirm(entity);
            case FALLBACK -> fallback(entity);
        }

    }

    private void fallback(EditEntity entity) throws IOException {
        String path = entity.getPath();
        String tempPath = getTempPath(path);
        Files.move(Paths.get(tempPath), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
    }

    private void confirm(EditEntity entity) throws IOException {
        String tempPath = getTempPath(entity.getPath());
        Files.deleteIfExists(Paths.get(tempPath));
    }

    private void edit(EditEntity entity) throws IOException {
        String path = entity.getPath();
        String tempPath = getTempPath(path);
        Path p = Paths.get(path);

        Snippet snippet = new Snippet();
        BeanUtil.copyProperties(entity, snippet);

        Files.move(p, Paths.get(tempPath), StandardCopyOption.REPLACE_EXISTING);
        Files.writeString(p, snippet.toMarkdown(), StandardCharsets.UTF_8);
    }

    private String getTempPath(String path) {
        return path + ".tmp";
    }

    public void delete(String path) throws IOException {
        Files.deleteIfExists(Paths.get(path));
    }

    public String sha(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new SnippetManagerException("文件不存在: " + filePath);
        }
        return DigestUtil.sha256Hex(file);
    }

    public List<RebuildEntity> listAll() throws IOException {
        log.info("获取数据目录[{}]文件信息", System.getenv(Constant.Property.DIR));
        List<RebuildEntity> list = new ArrayList<>();
        File file = new File(System.getenv(Constant.Property.DIR));
        listAll(file, list);
        return list;
    }

    private void listAll(File file, List<RebuildEntity> list) throws IOException {
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                listAll(f, list);
            } else {
                FileReader reader = new FileReader(f, StandardCharsets.UTF_8);
                String s = reader.readAllAsString();
                Snippet snippet = snippetReader.visit(s);
                RebuildEntity rebuildEntity = new RebuildEntity();
                BeanUtil.copyProperties(snippet, rebuildEntity);
                rebuildEntity.setPath(f.getAbsolutePath());
                rebuildEntity.setSha(DigestUtil.sha256Hex(f));
                rebuildEntity.setName(FileNameUtil.mainName(f));
                list.add(rebuildEntity);
                reader.close();
            }
        }
    }

    public enum UpdateAction {
        EDIT, CONFIRM, FALLBACK
    }
}
