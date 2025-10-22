package com.im.inspection.config;

import com.im.qtech.data.dpp.conf.UnifiedHadoopConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.im.exception.constants.ErrorCode;
import org.im.exception.type.common.BusinessException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Hadoop HA Configuration 配置构建工具类
 *
 * @author :  gaozhilin
 * @email :  gaoolin@gmail.com
 * @date :  2025/05/14 15:41:54
 */

public class HadoopConfigBuilder {

    /**
     * 构建适用于 Hadoop HA + Kerberos 的 Configuration
     *
     * @param principal  Kerberos 主体（如 user@REALM）
     * @param keytabPath Keytab 文件路径（如 /etc/security/keytabs/user.keytab）
     * @return 已配置好的 Configuration 实例
     */
    public static Configuration buildHaKerberosEnabledConfig(String principal, String keytabPath) {
        Configuration conf = new Configuration();

        // 使用统一配置管理器获取Hadoop配置
        applyHadoopConfigurations(conf);

        // 启用 Kerberos 安全认证
        conf.set("hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(conf);

        try {
            UserGroupInformation.loginUserFromKeytab(principal, keytabPath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED, "Kerberos login failed.", e);
        }

        return conf;
    }

    /**
     * 构建仅 HA 不启用 Kerberos 的 Configuration
     */
    public static Configuration buildHaNoKerberosConfig() {
        Configuration conf = new Configuration();

        // 应用统一Hadoop配置
        applyHadoopConfigurations(conf);

        // 设置Hadoop用户
        String hadoopUser = System.getProperty("HADOOP_USER_NAME", "qtech");
        System.setProperty("HADOOP_USER_NAME", hadoopUser);
        conf.set("hadoop.user.name", hadoopUser);

        return conf;
    }

    /**
     * 应用统一的Hadoop配置
     */
    private static void applyHadoopConfigurations(Configuration conf) {
        // 使用qtech-common中的统一配置管理
        Configuration unifiedConf = UnifiedHadoopConfig.createHadoopConfiguration();
        conf.addResource(unifiedConf);
    }

    /**
     * 从 properties 文件加载配置（更灵活）
     */
    public static Configuration buildFromPropertiesFile(String propertiesFilePath) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(new File(propertiesFilePath))) {
            props.load(fis);
        }

        Configuration conf = new Configuration();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            conf.set((String) entry.getKey(), (String) entry.getValue());
        }

        // 检查是否启用 Kerberos
        if ("kerberos".equals(conf.get("hadoop.security.authentication"))) {
            String principal = conf.get("kerberos.principal");
            String keytab = conf.get("kerberos.keytab.path");
            try {
                UserGroupInformation.setConfiguration(conf);
                UserGroupInformation.loginUserFromKeytab(principal, keytab);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED, "Kerberos login failed from properties file.", e);
            }
        }

        return conf;
    }
}
