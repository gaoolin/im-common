package org.im.config.builder;

import org.im.config.ConfigSource;
import org.im.config.ConfigurationManager;
import org.im.config.impl.DefaultConfigurationManager;

/**
 * 配置管理器构建器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */

public class ConfigurationManagerBuilder {
    private String profile = "default";
    private ConfigSource[] additionalSources = new ConfigSource[0];

    public static ConfigurationManagerBuilder create() {
        return new ConfigurationManagerBuilder();
    }

    public ConfigurationManagerBuilder withProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public ConfigurationManagerBuilder withSources(ConfigSource... sources) {
        this.additionalSources = sources;
        return this;
    }

    public ConfigurationManager build() {
        ConfigurationManager manager = new DefaultConfigurationManager(profile);
        for (ConfigSource source : additionalSources) {
            manager.addConfigSource(source);
        }
        return manager;
    }
}