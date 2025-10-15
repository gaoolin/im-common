package org.im.orm.example;

import org.im.orm.mapping.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门实体类
 * 演示一对一和一对多关联映射
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */
@Entity(table = "departments", enableAssociations = true)
public class Department {
    @Id(autoGenerate = true)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 一对多关联：一个部门有多个员工
    @OneToMany(mappedBy = "department", lazy = true)
    private List<User> users;

    // 一对一关联示例（假设每个部门有一个部门负责人）
    @OneToOne(mappedBy = "managedDepartment", lazy = true)
    private User manager;

    /**
     * 无参构造函数
     */
    public Department() {
    }

    /**
     * 带参数的构造函数
     *
     * @param name 部门名称
     */
    public Department(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", users=" + (users != null ? users.size() : "null") + "个" +
                ", manager=" + (manager != null ? manager.getUsername() : "null") +
                '}';
    }
}