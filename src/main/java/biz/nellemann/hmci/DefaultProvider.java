package biz.nellemann.hmci;

import picocli.CommandLine;

public class DefaultProvider implements CommandLine.IDefaultValueProvider {

    public String defaultValue(CommandLine.Model.ArgSpec argSpec) {
        if(argSpec.isOption()) {
            switch (argSpec.paramLabel()) {
                case "<file>":
                    return getDefaultConfigFileLocation();
                default:
                    return null;
            }
        }
        return null;
    }

    private boolean isWindowsOperatingSystem() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().startsWith("windows");
    }

    private String getDefaultConfigFileLocation() {
        String configFilePath;
        if(isWindowsOperatingSystem()) {
            configFilePath = System.getProperty("user.home") + "\\hmci.toml";
        } else {
            configFilePath = "/etc/hmci.toml";
        }
        return configFilePath;
    }
}
