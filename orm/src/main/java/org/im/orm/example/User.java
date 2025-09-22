package org.im.orm.example;

import org.im.orm.mapping.Column;
import org.im.orm.mapping.Entity;
import org.im.orm.mapping.Id;
import org.im.orm.mapping.ManyToOne;

import java.time.LocalDateTime;

/**
 * 用户实体示例
 * 演示ORM框架的基本使用方法
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */

/**
 * 用户实体类
 * 演示多对一关联映射
 */
@Entity(table = "users", enableAssociations = true)
public class User {
    @Id(autoGenerate = true)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 多对一关联：多个用户属于一个部门
    @ManyToOne(foreignKey = "department_id")
    private Department department;

    /**
     * 无参构造函数
     */
    public User() {
    }

    /**
     * 带参数的构造函数
     *
     * @param username 用户名
     * @param email    邮箱
     */
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
        if (department != null) {
            this.departmentId = department.getId();
        }
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", department=" + (department != null ? department.getName() : "null") +
                '}';
    }
}