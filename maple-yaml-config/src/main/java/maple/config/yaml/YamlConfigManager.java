package maple.config.yaml;

import maple.api.config.ConfigManager;
import maple.api.config.Configurable;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class YamlConfigManager implements ConfigManager {
    ConfigManager impl;

    private static ConfigManager tryJackson(Path configFile) {
        try {
            Class.forName("com.fasterxml.jackson.dataformat.yaml.YAMLMapper");
            return new JacksonConfigManager(configFile);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public YamlConfigManager(){
        try {
            var path = Paths.get(YamlConfigManager.class.getClassLoader().getResource("maple.yaml").toURI());
            impl = tryJackson(path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void bind(Configurable configurable) {
        impl.bind(configurable);
    }

    @Override
    public void configure() {
        impl.configure();
    }

    @Override
    public void updateConfigs(ConfigItem... configItems) {
        impl.updateConfigs(configItems);
    }
}
