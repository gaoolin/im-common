package org.im.orm;

import com.zaxxer.hikari.HikariConfig;
import org.im.orm.core.MultiDataSourceSession;
import org.im.orm.core.SessionFactory;
import org.im.orm.datasource.DataSourceManager;
import org.im.orm.datasource.HikariConnectionProvider;
import org.im.orm.example.Department;
import org.im.orm.example.User;
import org.im.orm.util.Constants;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * 关联映射功能测试类
 * 用于验证ORM框架的关联映射功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */

public class AssociationMappingTest {

    public static void main(String[] args) {
        try {
            // 初始化数据源
            initializeDataSources();

            // 创建表结构
            createTableStructure();

            // 测试关联映射功能
            testAssociationMapping();

            // 清理资源
            cleanup();

            System.out.println("关联映射功能测试完成");
        } catch (Exception e) {
            System.err.println("关联映射功能测试失败: " + e.getMessage());
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
                statement.execute("DROP TABLE IF EXISTS departments");

                // 创建departments表
                String createDepartmentsTableSQL = "CREATE TABLE departments (" +
                        "id BIGSERIAL PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";
                statement.execute(createDepartmentsTableSQL);

                // 创建users表，包含外键关联departments表
                String createUsersTableSQL = "CREATE TABLE users (" +
                        "id BIGSERIAL PRIMARY KEY, " +
                        "username VARCHAR(50) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL, " +
                        "department_id BIGINT, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (department_id) REFERENCES departments(id)" +
                        ")";
                statement.execute(createUsersTableSQL);

                System.out.println("表结构创建完成");
            }
        } catch (Exception e) {
            System.err.println("创建表结构失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试关联映射功能
     */
    private static void testAssociationMapping() {
        System.out.println("开始测试关联映射功能...");

        // 创建会话
        MultiDataSourceSession session = SessionFactory.createSession("postgresql");

        try {
            // 测试保存部门和用户（带关联关系）
            testSaveWithAssociation(session);

            // 测试查询带关联关系的实体
            testQueryWithAssociation(session);

        } finally {
            // 关闭会话
            session.close();
        }

        System.out.println("关联映射功能测试完成");
    }

    /**
     * 测试保存带关联关系的实体
     *
     * @param session 会话
     */
    private static void testSaveWithAssociation(MultiDataSourceSession session) {
        System.out.println("测试保存带关联关系的实体...");

        // 创建部门
        Department department = new Department("研发部");
        session.save(department);
        System.out.println("保存部门: " + department);

        // 创建用户并关联到部门
        User user = new User("张三", "zhangsan@example.com");
        user.setDepartment(department);
        session.save(user);
        System.out.println("保存用户: " + user);
    }

    /**
     * 测试查询带关联关系的实体
     *
     * @param session 会话
     */
    private static void testQueryWithAssociation(MultiDataSourceSession session) {
        System.out.println("测试查询带关联关系的实体...");

        // 查询所有用户
        List<User> users = session.findAll(User.class);
        System.out.println("查询到 " + users.size() + " 个用户:");
        for (User user : users) {
            System.out.println("  " + user);
        }

        // 查询所有部门
        List<Department> departments = session.findAll(Department.class);
        System.out.println("查询到 " + departments.size() + " 个部门:");
        for (Department department : departments) {
            System.out.println("  " + department);
        }
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