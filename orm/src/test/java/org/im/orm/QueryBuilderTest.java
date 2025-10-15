package org.im.orm;

import com.zaxxer.hikari.HikariConfig;
import org.im.orm.core.MultiDataSourceSession;
import org.im.orm.core.SessionFactory;
import org.im.orm.datasource.DataSourceManager;
import org.im.orm.datasource.HikariConnectionProvider;
import org.im.orm.example.Department;
import org.im.orm.example.User;
import org.im.orm.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */

public class QueryBuilderTest {

    private MultiDataSourceSession session;

    @Before
    public void setUp() throws Exception {
        // 初始化数据源
        initializeDataSources();

        // 创建表结构
        createTableStructure();

        // 创建会话
        session = SessionFactory.createSession("postgresql");

        // 初始化测试数据
        initializeTestData();
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
    }

    /**
     * 初始化测试数据
     */
    private void initializeTestData() {
        System.out.println("初始化测试数据...");

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
        } catch (Exception e) {
            System.err.println("初始化测试数据失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试基本查询
     */
    @Test
    public void testBasicQuery() {
        System.out.println("测试基本查询...");

        // 查询所有用户
        List<User> users = session.createQuery(User.class).getResultList();
        System.out.println("查询到 " + users.size() + " 个用户");
        assertTrue(users.size() > 0);

        // 查询单个用户
        User user = session.createQuery(User.class).eq("username", "张三").getSingleResult();
        System.out.println("查询到用户: " + user);
        assertNotNull(user);
        assertEquals("张三", user.getUsername());
    }

    /**
     * 测试条件查询
     */
    @Test
    public void testConditionalQuery() {
        System.out.println("测试条件查询...");

        // 测试等于条件
        List<User> users1 = session.createQuery(User.class)
                .eq("username", "张三")
                .getResultList();
        System.out.println("用户名等于'张三'的用户数: " + users1.size());
        assertEquals(1, users1.size());

        // 测试不等于条件
        List<User> users2 = session.createQuery(User.class)
                .ne("username", "张三")
                .getResultList();
        System.out.println("用户名不等于'张三'的用户数: " + users2.size());
        assertEquals(3, users2.size());

        // 测试LIKE条件
        List<User> users3 = session.createQuery(User.class)
                .like("username", "%三")
                .getResultList();
        System.out.println("用户名LIKE'%三'的用户数: " + users3.size());
        assertEquals(1, users3.size());

        // 测试IN条件
        List<User> users4 = session.createQuery(User.class)
                .in("username", Arrays.asList("张三", "李四", "王五"))
                .getResultList();
        System.out.println("用户名IN('张三', '李四', '王五')的用户数: " + users4.size());
        assertEquals(3, users4.size());
    }

    /**
     * 测试排序查询
     */
    @Test
    public void testOrderByQuery() {
        System.out.println("测试排序查询...");

        // 按用户名升序排列
        List<User> users1 = session.createQuery(User.class)
                .orderBy("username", true)
                .getResultList();
        System.out.println("按用户名升序排列的用户:");
        for (User user : users1) {
            System.out.println("  " + user.getUsername());
        }
        assertTrue(users1.size() > 0);

        // 按用户名降序排列
        List<User> users2 = session.createQuery(User.class)
                .orderBy("username", false)
                .getResultList();
        System.out.println("按用户名降序排列的用户:");
        for (User user : users2) {
            System.out.println("  " + user.getUsername());
        }
        assertTrue(users2.size() > 0);
    }

    /**
     * 测试分页查询
     */
    @Test
    public void testPaginationQuery() {
        System.out.println("测试分页查询...");

        // 查询前2个用户
        List<User> users1 = session.createQuery(User.class)
                .limit(2)
                .getResultList();
        System.out.println("前2个用户:");
        for (User user : users1) {
            System.out.println("  " + user.getUsername());
        }
        assertEquals(2, users1.size());

        // 查询跳过前2个后的2个用户
        List<User> users2 = session.createQuery(User.class)
                .offset(2)
                .limit(2)
                .getResultList();
        System.out.println("跳过前2个后的2个用户:");
        for (User user : users2) {
            System.out.println("  " + user.getUsername());
        }
        assertEquals(2, users2.size());
    }

    /**
     * 测试COUNT查询
     */
    @Test
    public void testCountQuery() {
        System.out.println("测试COUNT查询...");

        // 统计所有用户数
        long count1 = session.createQuery(User.class).count();
        System.out.println("所有用户数: " + count1);
        assertEquals(4, count1);

        // 统计用户名包含"三"的用户数
        long count2 = session.createQuery(User.class)
                .like("username", "%三")
                .count();
        System.out.println("用户名包含'三'的用户数: " + count2);
        assertEquals(1, count2);
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
