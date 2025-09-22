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
 * 一对多关联加载功能测试类
 * 用于验证ORM框架的一对多关联加载功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */
public class OneToManyAssociationTestFixed {
    public static void main(String[] args) {
        try {
            // 初始化数据源
            initializeDataSources();

            // 创建表结构
            createTableStructure();

            // 初始化测试数据
            initializeTestData();

            // 测试一对多关联加载功能
            testOneToManyAssociation();

            // 清理资源
            cleanup();

            System.out.println("一对多关联加载功能测试完成");
        } catch (Exception e) {
            System.err.println("一对多关联加载功能测试失败: " + e.getMessage());
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
     * 初始化测试数据
     */
    private static void initializeTestData() {
        System.out.println("初始化测试数据...");

        // 创建会话
        MultiDataSourceSession session = SessionFactory.createSession("postgresql");

        try {
            // 创建部门
            Department department1 = new Department("研发部");
            session.save(department1);

            Department department2 = new Department("市场部");
            session.save(department2);

            Department department3 = new Department("人事部");
            session.save(department3);

            // 创建用户
            User user1 = new User("张三", "zhangsan@example.com");
            user1.setDepartment(department1);
            session.save(user1);

            User user2 = new User("李四", "lisi@example.com");
            user2.setDepartment(department1);
            session.save(user2);

            User user3 = new User("王五", "wangwu@example.com");
            user3.setDepartment(department2);
            session.save(user3);

            User user4 = new User("赵六", "zhaoliu@example.com");
            user4.setDepartment(department3);
            session.save(user4);

            System.out.println("测试数据初始化完成");
        } finally {
            session.close();
        }
    }

    /**
     * 测试一对多关联加载功能
     */
    private static void testOneToManyAssociation() {
        System.out.println("开始测试一对多关联加载功能...");

        // 创建会话
        MultiDataSourceSession session = SessionFactory.createSession("postgresql");

        try {
            // 测试查询部门及其关联的用户
            testQueryDepartmentWithUsers(session);

        } finally {
            // 关闭会话
            session.close();
        }

        System.out.println("一对多关联加载功能测试完成");
    }

    /**
     * 测试查询部门及其关联的用户
     *
     * @param session 会话
     */
    private static void testQueryDepartmentWithUsers(MultiDataSourceSession session) {
        System.out.println("测试查询部门及其关联的用户...");

        // 查询所有部门
        List<Department> departments = session.findAll(Department.class);
        System.out.println("查询到 " + departments.size() + " 个部门:");

        for (Department department : departments) {
            System.out.println("部门: " + department);

            // 检查是否加载了关联的用户
            if (department.getUsers() != null) {
                System.out.println("  关联的用户数: " + department.getUsers().size());
                for (User user : department.getUsers()) {
                    System.out.println("    用户: " + user.getUsername());
                }
            } else {
                System.out.println("  关联的用户: null");
            }
        }

        // 根据ID查询单个部门
        Department department = session.findById(Department.class, 1L);
        System.out.println("根据ID查询部门: " + department);

        if (department != null && department.getUsers() != null) {
            System.out.println("  关联的用户数: " + department.getUsers().size());
            for (User user : department.getUsers()) {
                System.out.println("    用户: " + user.getUsername());
            }
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