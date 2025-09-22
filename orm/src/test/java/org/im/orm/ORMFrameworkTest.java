package org.im.orm;

import com.zaxxer.hikari.HikariConfig;
import org.im.orm.core.MultiDataSourceSession;
import org.im.orm.core.SessionFactory;
import org.im.orm.datasource.DataSourceManager;
import org.im.orm.datasource.HikariConnectionProvider;
import org.im.orm.example.User;
import org.im.orm.util.Constants;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * ORM框架测试类
 * 用于验证ORM框架的基本功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */
public class ORMFrameworkTest {

    public static void main(String[] args) {
        try {
            // 初始化数据源
            initializeDataSources();

            // 创建表结构
            createTableStructure();

            // 测试ORM框架功能
            testORMFramework();

            // 清理资源
            cleanup();

            System.out.println("ORM框架测试完成");
        } catch (Exception e) {
            System.err.println("ORM框架测试失败: " + e.getMessage());
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
     * 测试ORM框架功能
     */
    private static void testORMFramework() {
        System.out.println("开始测试ORM框架...");

        // 创建会话
        MultiDataSourceSession session = SessionFactory.createSession("postgresql");

        try {
            // 测试保存实体
            System.out.println("测试保存实体...");
            User user = new User("测试用户", "test@example.com");
            session.save(user);
            System.out.println("保存用户成功: " + user);

            // 测试根据ID查找实体
            System.out.println("测试根据ID查找实体...");
            User foundUser = session.findById(User.class, user.getId());
            System.out.println("找到用户: " + foundUser);

            // 测试查找所有实体
            System.out.println("测试查找所有实体...");
            List<User> users = session.findAll(User.class);
            System.out.println("找到 " + users.size() + " 个用户");
            for (User u : users) {
                System.out.println("  用户: " + u);
            }

            // 测试更新实体
            System.out.println("测试更新实体...");
            if (foundUser != null) {
                foundUser.setEmail("updated@example.com");
                session.update(foundUser);
                System.out.println("更新用户成功");

                // 再次查找以验证更新
                User updatedUser = session.findById(User.class, user.getId());
                System.out.println("更新后的用户: " + updatedUser);

                // 测试删除实体
                System.out.println("测试删除实体...");
                session.delete(foundUser);
                System.out.println("删除用户成功");

                // 再次查找以验证删除
                User deletedUser = session.findById(User.class, user.getId());
                if (deletedUser == null) {
                    System.out.println("用户已成功删除");
                } else {
                    System.out.println("用户删除失败");
                }
            }

        } finally {
            // 关闭会话
            session.close();
        }

        System.out.println("ORM框架功能测试完成");
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
