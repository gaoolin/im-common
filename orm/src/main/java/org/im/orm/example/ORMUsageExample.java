package org.im.orm.example;


import com.zaxxer.hikari.HikariConfig;
import org.im.orm.core.MultiDataSourceSession;
import org.im.orm.core.MultiDataSourceSessionImpl;
import org.im.orm.datasource.DataSourceManager;
import org.im.orm.datasource.HikariConnectionProvider;

/**
 * ORM框架使用示例
 * 演示如何配置和使用ORM框架
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */
public class ORMUsageExample {

    public static void main(String[] args) {
        try {
            // 1. 配置数据源
            setupDataSources();

            // 2. 创建多数据源会话
            MultiDataSourceSession session = new MultiDataSourceSessionImpl("primary");

            // 3. 执行数据库操作
            performDatabaseOperations(session);

            // 4. 关闭会话
            session.close();

            // 5. 关闭所有数据源
            DataSourceManager.closeAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置数据源
     */
    private static void setupDataSources() {
        // 配置主数据源
        HikariConfig primaryConfig = new HikariConfig();
        primaryConfig.setJdbcUrl("jdbc:mysql://localhost:3306/primary_db");
        primaryConfig.setUsername("user");
        primaryConfig.setPassword("password");
        primaryConfig.setMaximumPoolSize(10);

        HikariConnectionProvider primaryProvider = new HikariConnectionProvider(primaryConfig);
        DataSourceManager.registerDataSource("primary", primaryProvider);

        // 配置备用数据源
        HikariConfig secondaryConfig = new HikariConfig();
        secondaryConfig.setJdbcUrl("jdbc:mysql://localhost:3306/secondary_db");
        secondaryConfig.setUsername("user");
        secondaryConfig.setPassword("password");
        secondaryConfig.setMaximumPoolSize(5);

        HikariConnectionProvider secondaryProvider = new HikariConnectionProvider(secondaryConfig);
        DataSourceManager.registerDataSource("secondary", secondaryProvider);
    }

    /**
     * 执行数据库操作
     *
     * @param session 会话
     */
    private static void performDatabaseOperations(MultiDataSourceSession session) {
        try {
            // 在主数据源上创建用户
            User user = new User("张三", "zhangsan@example.com");
            session.save(user);
            System.out.println("在主数据源上创建用户: " + user);

            // 查询用户
            User foundUser = session.findById(User.class, user.getId());
            System.out.println("从主数据源查询用户: " + foundUser);

            // 更新用户
            foundUser.setEmail("zhangsan_new@example.com");
            session.update(foundUser);
            System.out.println("更新用户邮箱");

            // 切换到备用数据源
            session.switchDataSource("secondary");
            System.out.println("切换到备用数据源: " + session.getCurrentDataSourceName());

            // 在备用数据源上创建用户
            User secondaryUser = new User("李四", "lisi@example.com");
            session.save(secondaryUser);
            System.out.println("在备用数据源上创建用户: " + secondaryUser);

            // 查询备用数据源上的用户
            User foundSecondaryUser = session.findById(User.class, secondaryUser.getId());
            System.out.println("从备用数据源查询用户: " + foundSecondaryUser);

            // 切换回主数据源
            session.switchDataSource("primary");
            System.out.println("切换回主数据源: " + session.getCurrentDataSourceName());

            // 事务操作示例
            session.beginTransaction();
            try {
                User user1 = new User("王五", "wangwu@example.com");
                User user2 = new User("赵六", "zhaoliu@example.com");

                session.save(user1);
                session.save(user2);

                session.commit();
                System.out.println("事务提交成功");
            } catch (Exception e) {
                session.rollback();
                System.out.println("事务回滚: " + e.getMessage());
                throw e;
            }

        } catch (Exception e) {
            System.err.println("数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
