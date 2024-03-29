package maple.config.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import maple.api.config.Config;
import maple.api.config.ConfigManager;
import maple.api.config.Configurable;
import maple.api.models.LogField;
import org.slf4j.event.Level;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class JacksonConfigManager implements ConfigManager {
    private static final LogField[] reference = new LogField[]{};
    private final ObjectMapper mapper;
    private final Path file;
    Configurable configurable;
    Node.RootNode config;

    public JacksonConfigManager(Path file) {
        this.mapper = new YAMLMapper();
        this.file = file;
    }

    private ConfigurationChange getUpdateFn(Node configNode) {
        var hasFields = (configNode.fields() != null) && (!configNode.fields().isEmpty());
        var hasLevel = configNode.level() != null;

        return ((Config base) -> base
                .replaceLevel(hasLevel ? Level.valueOf(configNode.level().toUpperCase()) : base.level())
                .replaceFields(hasFields ? configNode.fields().stream().map(LogField::fromFieldName).filter(Objects::nonNull).toArray(size -> Arrays.copyOf(reference, size)) : base.fields()));
    }

    private ConfigItem getConfigItem(String logger, Node node){
        return new ConfigItem.LoggerConfigItem(logger, getUpdateFn(node));
    }

    private ConfigItem getConfigItem(Node node){
        return new ConfigItem.RootConfigItem(getUpdateFn(node));
    }

    private ConfigItem[] configItemsFromYaml(){
        ConfigItem[] root = new ConfigItem[] {
                getConfigItem(config)
        };
        ConfigItem[] child = config.loggers().keySet().stream()
                .map(key -> getConfigItem(key, config.loggers().get(key)))
                .toArray(size -> Arrays.copyOf(new ConfigItem[0], size));
        ConfigItem[] result = new ConfigItem[root.length + child.length];
        System.arraycopy(root, 0, result, 0, root.length);
        System.arraycopy(child, 0, result, root.length, child.length);

        return result;
    }

    public void read() throws IOException {
        config = mapper.readValue(file.toFile(), Node.MapleConfig.class).maple();
    }

    @Override
    public void bind(Configurable configurable) {
        this.configurable = configurable;
    }

    @Override
    public void configure() {
        try {
            read();
            configurable.configure(configItemsFromYaml());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    @Override
    public void updateConfigs(ConfigItem... configItems) {
        configurable.configure(configItems);
    }
}

/* The yaml should look something like this:

- maple:
    level: info // optional
    fields:
        - thread
        - logger
        - message
        - mdc
    loggers:
        maple:
            level: debug
        maple.core.logger:
            level: trace
*/
