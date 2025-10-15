package org.im.orm;

import com.zaxxer.hikari.HikariConfig;
import org.im.orm.core.MultiDataSourceSession;
import org.im.orm.core.SessionFactory;
import org.im.orm.datasource.DataSourceManager;
import org.im.orm.datasource.HikariConnectionProvider;
import org.im.orm.example.User;
import org.im.orm.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */

public class BatchOperationTest {
    private static final Logger logger = LoggerFactory.getLogger(BatchOperationTest.class);

    private MultiDataSourceSession session;

    @Before
    public void setUp() throws Exception {
        // 初始化数据源
        initializeDataSources();

        // 创建表结构
        createTableStructure();

        // 创建会话
        session = SessionFactory.createSession("postgresql");
    }

    @After
    public void tearDown() throws Exception {
        if (session != null) {
            session.close();
        }
        // 清理资源
        cleanup();
    }

    /**
     * 初始化数据源
     */
    private void initializeDataSources() {
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
    private void createTableStructure() throws Exception {
        System.out.println("创建表结构...");

        HikariConnectionProvider provider = (HikariConnectionProvider) DataSourceManager.getDataSource("postgresql");
        try (Connection connection = provider.getConnection();
             Statement statement = connection.createStatement()) {

            // 删除已存在的表（如果存在）
            statement.execute("DROP TABLE IF EXISTS users");

            // 创建users表，使用SERIAL类型实现自增主键
            // 添加了department_id字段以匹配User实体类
            String createTableSQL = "CREATE TABLE users (" +
                    "id BIGSERIAL PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "department_id BIGINT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (department_id) REFERENCES departments(id)" +
                    ")";

            statement.execute(createTableSQL);

            // 如果departments表不存在，创建一个空的departments表
            try {
                statement.execute("CREATE TABLE departments (" +
                        "id BIGSERIAL PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");
            } catch (Exception e) {
                // 如果departments表已存在则忽略
            }

            System.out.println("表结构创建完成");
        }
    }

    /**
     * 测试批量保存
     */
    @Test
    public void testBatchSave() {
        System.out.println("测试批量保存...");

        // 创建多个用户
        List<User> users = new ArrayList<>();
        users.add(new User("批量用户1", "batch1@example.com"));
        users.add(new User("批量用户2", "batch2@example.com"));
        users.add(new User("批量用户3", "batch3@example.com"));
        users.add(new User("批量用户4", "batch4@example.com"));
        users.add(new User("批量用户5", "batch5@example.com"));

        // 确保关联字段正确初始化
        for (User user : users) {
            user.setDepartment(null);
        }

        long startTime = System.currentTimeMillis();
        session.saveBatch(users);
        long endTime = System.currentTimeMillis();

        System.out.println("批量保存 " + users.size() + " 个用户，耗时: " + (endTime - startTime) + "ms");
        for (User user : users) {
            System.out.println("  保存的用户: " + user);
            assertNotNull(user.getId()); // 验证ID已生成
        }

        assertEquals(5, users.size());
    }

    /**
     * 测试批量更新
     */
    @Test
    public void testBatchUpdate() {
        System.out.println("测试批量更新...");

        // 先插入一些测试数据
        List<User> initialUsers = new ArrayList<>();
        initialUsers.add(new User("初始用户1", "initial1@example.com"));
        initialUsers.add(new User("初始用户2", "initial2@example.com"));
        // 确保关联字段正确初始化
        for (User user : initialUsers) {
            user.setDepartment(null);
        }
        session.saveBatch(initialUsers);

        // 查询所有用户
        List<User> users = session.findAll(User.class);
        assertFalse(users.isEmpty());

        // 修改所有用户的邮箱，保持department为null
        for (User user : users) {
            user.setEmail("updated_" + user.getEmail());
            user.setDepartment(null); // 显式设置为null
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
            assertTrue(user.getEmail().startsWith("updated_"));
        }

        assertEquals(users.size(), updatedUsers.size());
    }

    /**
     * 测试批量删除
     */
    @Test
    public void testBatchDelete() {
        System.out.println("测试批量删除...");

        // 先插入一些测试数据
        List<User> initialUsers = new ArrayList<>();
        initialUsers.add(new User("待删除用户1", "delete1@example.com"));
        initialUsers.add(new User("待删除用户2", "delete2@example.com"));
        initialUsers.add(new User("待删除用户3", "delete3@example.com"));
        // 确保关联字段正确初始化
        for (User user : initialUsers) {
            user.setDepartment(null);
        }
        session.saveBatch(initialUsers);

        // 查询所有用户
        List<User> users = session.findAll(User.class);
        int userCount = users.size();
        assertTrue(userCount > 0);

        // 确保关联字段正确初始化
        for (User user : users) {
            user.setDepartment(null);
        }

        long startTime = System.currentTimeMillis();
        session.deleteBatch(users);
        long endTime = System.currentTimeMillis();

        System.out.println("批量删除 " + users.size() + " 个用户，耗时: " + (endTime - startTime) + "ms");

        // 验证删除结果
        List<User> remainingUsers = session.findAll(User.class);
        System.out.println("删除后剩余用户数: " + remainingUsers.size());

        assertEquals(0, remainingUsers.size());
    }

    /**
     * 清理资源
     */
    private void cleanup() {
        System.out.println("清理资源...");
        DataSourceManager.closeAll();
        System.out.println("资源清理完成");
    }
}
