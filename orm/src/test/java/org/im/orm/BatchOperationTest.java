package org.im.orm;

import com.zaxxer.hikari.HikariConfig;
import org.im.orm.core.MultiDataSourceSession;
import org.im.orm.core.SessionFactory;
import org.im.orm.datasource.DataSourceManager;
import org.im.orm.datasource.HikariConnectionProvider;
import org.im.orm.example.User;
import org.im.orm.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */

public class BatchOperationTest {
    private static final Logger logger = LoggerFactory.getLogger(BatchOperationTest.class);

    public static void main(String[] args) {
        try {
            // 初始化数据源
            initializeDataSources();

            // 创建表结构
            createTableStructure();

            // 测试批量操作功能
            testBatchOperations();

            // 清理资源
            cleanup();

            System.out.println("批量操作功能测试完成");
        } catch (Exception e) {
            System.err.println("批量操作功能测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 初始化数据源
     */
    private static void initializeDataSources() {
        System.out.println("初始化数据源...");

        // 配置PostgreSQL数据源
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Constants.POSTGRES_URL);
        config.setUsername(Constants.POSTGRES_USER);
        config.setPassword(Constants.POSTGRES_PASSWORD);
        config.setDriverClassName(Constants.POSTGRES_DRIVER_CLASS);
        config.setMaximumPoolSize(5);

        HikariConnectionProvider provider = new HikariConnectionProvider(config);
        DataSourceManager.registerDataSource("postgresql", provider);

        System.out.println("PostgreSQL数据源初始化完成");
    }

    /**
     * 创建表结构
     */
    private static void createTableStructure() {
        System.out.println("创建表结构...");

        try {
            HikariConnectionProvider provider = (HikariConnectionProvider) DataSourceManager.getDataSource("postgresql");
            try (Connection connection = provider.getConnection();
                 Statement statement = connection.createStatement()) {

                // 删除已存在的表（如果存在）
                statement.execute("DROP TABLE IF EXISTS users");

                // 创建users表，使用SERIAL类型实现自增主键
                String createTableSQL = "CREATE TABLE users (" +
                        "id BIGSERIAL PRIMARY KEY, " +
                        "username VARCHAR(50) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";

                statement.execute(createTableSQL);
                System.out.println("表结构创建完成");
            }
        } catch (Exception e) {
            System.err.println("创建表结构失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试批量操作功能
     */
    private static void testBatchOperations() {
        System.out.println("开始测试批量操作功能...");

        // 创建会话
        MultiDataSourceSession session = SessionFactory.createSession("postgresql");

        try {
            // 测试批量保存
            testBatchSave(session);

            // 测试批量更新
            testBatchUpdate(session);

            // 测试批量删除
            testBatchDelete(session);

        } finally {
            // 关闭会话
            session.close();
        }

        System.out.println("批量操作功能测试完成");
    }

    /**
     * 测试批量保存
     *
     * @param session 会话
     */
    private static void testBatchSave(MultiDataSourceSession session) {
        System.out.println("测试批量保存...");

        // 创建多个用户
        List<User> users = new ArrayList<>();
        users.add(new User("批量用户1", "batch1@example.com"));
        users.add(new User("批量用户2", "batch2@example.com"));
        users.add(new User("批量用户3", "batch3@example.com"));
        users.add(new User("批量用户4", "batch4@example.com"));
        users.add(new User("批量用户5", "batch5@example.com"));

        long startTime = System.currentTimeMillis();
        session.saveBatch(users);
        long endTime = System.currentTimeMillis();

        System.out.println("批量保存 " + users.size() + " 个用户，耗时: " + (endTime - startTime) + "ms");
        for (User user : users) {
            System.out.println("  保存的用户: " + user);
        }
    }

    /**
     * 测试批量更新
     *
     * @param session 会话
     */
    private static void testBatchUpdate(MultiDataSourceSession session) {
        System.out.println("测试批量更新...");

        // 查询所有用户
        List<User> users = session.findAll(User.class);

        // 修改所有用户的邮箱
        for (User user : users) {
            user.setEmail("updated_" + user.getEmail());
        }

        long startTime = System.currentTimeMillis();
        session.updateBatch(users);
        long endTime = System.currentTimeMillis();

        System.out.println("批量更新 " + users.size() + " 个用户，耗时: " + (endTime - startTime) + "ms");

        // 验证更新结果
        List<User> updatedUsers = session.findAll(User.class);
        System.out.println("更新后的用户:");
        for (User user : updatedUsers) {
            System.out.println("  " + user);
        }
    }

    /**
     * 测试批量删除
     *
     * @param session 会话
     */
    private static void testBatchDelete(MultiDataSourceSession session) {
        System.out.println("测试批量删除...");

        // 查询所有用户
        List<User> users = session.findAll(User.class);

        long startTime = System.currentTimeMillis();
        session.deleteBatch(users);
        long endTime = System.currentTimeMillis();

        System.out.println("批量删除 " + users.size() + " 个用户，耗时: " + (endTime - startTime) + "ms");

        // 验证删除结果
        List<User> remainingUsers = session.findAll(User.class);
        System.out.println("删除后剩余用户数: " + remainingUsers.size());
    }

    /**
     * 清理资源
     */
    private static void cleanup() {
        System.out.println("清理资源...");
        DataSourceManager.closeAll();
        System.out.println("资源清理完成");
    }
}