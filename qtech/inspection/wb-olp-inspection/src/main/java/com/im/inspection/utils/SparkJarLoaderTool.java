package com.im.inspection.utils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/04 14:37:28
 */

public class SparkJarLoaderTool {
    private static final Logger logger = LoggerFactory.getLogger(SparkJarLoaderTool.class);

    // 加载 JAR 文件中的加密类
    // 加载 JAR 文件中的加密类
    public static Dataset<Row> loadAndRunClass(String jarFilePath, String publicKeyPath, String tokenFilePath, String className, String methodName, Dataset<Row> dataset1, Dataset<Row> dataset2) throws Exception {
        // 初始化自定义类加载器
        Path publicKey = Paths.get(publicKeyPath);
        Path tokenFile = Paths.get(tokenFilePath);
        File jarFile = new File(jarFilePath);

        try (JarFile jar = new JarFile(jarFile)) {
            CustomClassLoader classLoader = new CustomClassLoader(
                    SparkJarLoaderTool.class.getClassLoader(),
                    publicKey,
                    tokenFile,
                    jar
            );

            // 加载类
            Class<?> clazz = classLoader.loadClass(className);

            // 创建实例
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 获取方法
            Method method = clazz.getMethod(methodName, Dataset.class, Dataset.class);

            // 调用方法
            return (Dataset<Row>) method.invoke(instance, dataset1, dataset2);
        } catch (Exception e) {
            logger.error("加载 JAR 文件中的加密类失败", e);
        }
        return null;
    }
}
