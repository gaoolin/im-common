package com.im.inspection.utils;

import java.util.Date;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/03 21:25:57
 */


// TokenData 类的定义，确保包含项目信息、作者、确认信息和过期时间
public class TokenData {
    private String projectName;
    private String author;
    private String confirmation;
    private Date expirationDate;

    public TokenData(String projectName, String author, String confirmation, Date expirationDate) {
        this.projectName = projectName;
        this.author = author;
        this.confirmation = confirmation;
        this.expirationDate = expirationDate;
    }

    // Getters and setters
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
