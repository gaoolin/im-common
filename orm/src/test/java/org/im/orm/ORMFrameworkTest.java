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

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ORM框架测试类
 * 用于验证ORM框架的基本功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */
public class ORMFrameworkTest {

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
            // 添加department_id字段以匹配User实体类
            String createTableSQL = "CREATE TABLE users (" +
                    "id BIGSERIAL PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "department_id BIGINT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            statement.execute(createTableSQL);
            System.out.println("表结构创建完成");
        }
    }

    /**
     * 测试保存实体
     */
    @Test
    public void testSaveEntity() {
        System.out.println("测试保存实体...");
        User user = new User("测试用户", "testing@example.com");
        session.save(user);
        System.out.println("保存用户成功: " + user);
        assertNotNull(user.getId());
    }

    /**
     * 测试根据ID查找实体
     */
    @Test
    public void testFindById() {
        // 先保存一个用户
        User user = new User("查找测试用户", "find@example.com");
        session.save(user);

        System.out.println("测试根据ID查找实体...");
        User foundUser = session.findById(User.class, user.getId());
        System.out.println("找到用户: " + foundUser);
        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getUsername(), foundUser.getUsername());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    /**
     * 测试查找所有实体
     */
    @Test
    public void testFindAll() {
        // 先保存几个用户
        User user1 = new User("用户1", "user1@example.com");
        User user2 = new User("用户2", "user2@example.com");
        session.save(user1);
        session.save(user2);

        System.out.println("测试查找所有实体...");
        List<User> users = session.findAll(User.class);
        System.out.println("找到 " + users.size() + " 个用户");
        for (User u : users) {
            System.out.println("  用户: " + u);
        }
        assertTrue(users.size() >= 2);
    }

    /**
     * 测试更新实体
     */
    @Test
    public void testUpdateEntity() {
        System.out.println("测试更新实体...");
        // 先保存一个用户
        User user = new User("原始用户名", "original@example.com");
        session.save(user);

        user.setEmail("updated@example.com");
        session.update(user);
        System.out.println("更新用户成功");

        // 再次查找以验证更新
        User updatedUser = session.findById(User.class, user.getId());
        System.out.println("更新后的用户: " + updatedUser);
        assertNotNull(updatedUser);
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    /**
     * 测试删除实体
     */
    @Test
    public void testDeleteEntity() {
        System.out.println("测试删除实体...");
        // 先保存一个用户
        User user = new User("待删除用户", "todelete@example.com");
        session.save(user);

        session.delete(user);
        System.out.println("删除用户成功");

        // 再次查找以验证删除
        User deletedUser = session.findById(User.class, user.getId());
        System.out.println("用户已成功删除: " + (deletedUser == null));
        assertNull(deletedUser);
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
