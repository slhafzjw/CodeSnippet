package work.slhaf.snippet.service;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import work.slhaf.snippet.common.Constant;
import work.slhaf.snippet.entity.db.Index;
import work.slhaf.snippet.entity.file.ListEntity;
import work.slhaf.snippet.entity.file.RebuildEntity;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class IndexManager {

    private static volatile IndexManager instance;

    private final Connection connection;
    private final SnippetManager snippetManager = new SnippetManager();

    public static IndexManager getInstance() throws SQLException {
        if (instance == null) {
            synchronized (IndexManager.class) {
                if (instance == null) {
                    instance = new IndexManager();
                }
            }
        }
        return instance;
    }

    private IndexManager() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + System.getenv(Constant.Property.CONF) + "/index.db");
        checkTable(connection);
    }

    private void checkTable(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("""
                create table if not exists code_snippet_index(
                    id integer primary key,
                    name varchar(255) not null,
                    language varchar(60) not null,
                    tags varchar(300) not null,
                    sha varchar(100) not null,
                    description varchar(300) not null,
                    path varchar(300) not null
                )
                """);
        statement.close();
    }

    public void rebuildIndex() throws SQLException, IOException {
        log.info("重建索引数据库");
        resetIndex();
        List<RebuildEntity> snippets = snippetManager.listAll();
        Set<Index> indexSet = snippets.stream().map(entity -> {
                    Index index = new Index();
                    BeanUtil.copyProperties(entity, index);
                    return index;
                })
                .collect(Collectors.toSet());
        for (Index index : indexSet) {
            add(index);
        }
    }

    public void add(Index index) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                insert into code_snippet_index(name,language, tags, sha, description, path) 
                values (?,?,?,?,?,?)
                """);
        statement.setString(1, index.getName());
        statement.setString(2, index.getLanguage());
        statement.setString(3, Arrays.toString(index.getTags()));
        statement.setString(4, index.getSha());
        statement.setString(5, index.getDescription());
        statement.setString(6, index.getPath());
        statement.execute();
        statement.close();
    }

    public void update(Index index) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                    update code_snippet_index
                    set tags = ?, description = ?, sha = ?
                    where id = ?
                """);
        statement.setString(1, Arrays.toString(index.getTags()));
        statement.setString(2, index.getDescription());
        statement.setString(3, index.getSha());
        statement.executeUpdate();
        statement.close();
    }

    public void delete(String path) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("delete from code_snippet_index where path = ?");
        statement.setString(1, path);
        statement.executeUpdate();
    }

    public List<ListEntity> list(String input) throws SQLException {
        String[] keywords = input.trim().toLowerCase().split("\\s+");

        // 构建 SQL 占位符字符串
        StringBuilder sb = new StringBuilder("SELECT * FROM code_snippet_index WHERE ");
        List<String> params = new ArrayList<>();
        for (int k = 0; k < keywords.length; k++) {
            if (k > 0) sb.append(" OR ");
            sb.append("(name LIKE ? OR description LIKE ? OR tags LIKE ? OR language LIKE ?)");
            for (int i = 0; i < 4; i++) params.add("%" + keywords[k] + "%");
        }

        PreparedStatement statement = connection.prepareStatement(sb.toString());
        for (int i = 0; i < params.size(); i++) {
            statement.setString(i + 1, params.get(i));
        }

        ResultSet rs = statement.executeQuery();
        List<ListEntity> list = new ArrayList<>();

        while (rs.next()) {
            ListEntity entity = new ListEntity();
            entity.setId(rs.getString("id"));
            entity.setPath(rs.getString("path"));
            entity.setName(rs.getString("name"));
            entity.setScore(calculateScore(rs, keywords)); // 多关键字打分
            list.add(entity);
        }

        // 按分数降序排序
        list.sort(Comparator.comparingInt(ListEntity::getScore).reversed());
        return list;
    }

    // 多关键字加权打分
    private int calculateScore(ResultSet rs, String[] keywords) throws SQLException {
        int score = 0;
        String name = rs.getString("name").toLowerCase();
        String description = rs.getString("description").toLowerCase();
        String language = rs.getString("language").toLowerCase();

        String tagsRaw = rs.getString("tags");
        List<String> tagList = Arrays.stream(tagsRaw.substring(1, tagsRaw.length() - 1)
                        .split(","))
                .map(String::toLowerCase)
                .toList();

        for (String key : keywords) {
            if (name.contains(key)) score += 5;
            if (tagList.stream().anyMatch(t -> t.contains(key))) score += 4;
            if (description.contains(key)) score += 3;
            if (language.contains(key)) score += 2;
        }
        return score;
    }


    public boolean isEmpty() throws SQLException {
        Statement statement = connection.createStatement();
        //判断表是否为空
        ResultSet rs = statement.executeQuery("select count(*) from code_snippet_index");
        rs.next();
        int count = rs.getInt(1);
        statement.close();
        return count == 0;
    }

    public void resetIndex() throws SQLException {
        log.info("重置索引");
        //删除表中全部索引条目
        Statement statement = connection.createStatement();
        statement.execute("delete from code_snippet_index");
        statement.close();
    }

    public HashMap<String, String> getIndexStatus() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("""
                    select sha,path from code_snippet_index
                """);
        HashMap<String, String> map = new HashMap<>();
        while (rs.next()) {
            map.put(rs.getString("path"), rs.getString("sha"));
        }
        return map;
    }
}
