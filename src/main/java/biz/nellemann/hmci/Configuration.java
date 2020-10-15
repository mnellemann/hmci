package biz.nellemann.hmci;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Configuration {

    //private final static Logger log = LoggerFactory.getLogger(Configuration.class);

    final public Long refresh;
    final public Long rescan;
    final public InfluxObject influx;
    final public List<HmcObject> hmc;

    Configuration(String configurationFile) throws IOException {

        Path source = Paths.get(configurationFile);
        TomlParseResult result = Toml.parse(source);
        result.errors().forEach(error -> System.err.println(error.toString()));

        if(result.contains("refresh")) {
            refresh = result.getLong("refresh");
        } else {
            refresh = 15L;
        }

        if(result.contains("rescan")) {
            rescan = result.getLong("rescan");
        } else {
            rescan = 60L;
        }

        hmc = getHmc(result);
        influx = getInflux(result);

    }


    List<HmcObject> getHmc(TomlParseResult result) {

        ArrayList<HmcObject> list = new ArrayList<>();

        if(result.contains("hmc") && result.isTable("hmc")) {
            TomlTable hmcTable = result.getTable("hmc");
            if(hmcTable == null) {
                return list;
            }
            for(String key : hmcTable.keySet()) {

                HmcObject c = new HmcObject();
                c.name = key;

                if(hmcTable.contains(key+".url")) {
                    c.url = hmcTable.getString(key+".url");
                }

                if(hmcTable.contains(key+".username")) {
                    c.username = hmcTable.getString(key+".username");
                }

                if(hmcTable.contains(key+".password")) {
                    c.password = hmcTable.getString(key+".password");
                }

                if(hmcTable.contains(key+".unsafe")) {
                    c.unsafe = hmcTable.getBoolean(key+".unsafe");
                } else {
                    c.unsafe = false;
                }

                list.add(c);
            }
        }

        return list;
    }


    InfluxObject getInflux(TomlParseResult result) {

        InfluxObject c = new InfluxObject();

        if(result.contains("influx")) {
            TomlTable t = result.getTable("influx");

            if(t != null && t.contains("url")) {
                c.url = t.getString("url");
            }

            if(t != null && t.contains("username")) {
                c.username = t.getString("username");
            }

            if(t != null && t.contains("password")) {
                c.password = t.getString("password");
            }

            if(t != null && t.contains("database")) {
                c.database = t.getString("database");
            }

        }

        return c;
    }


    static class InfluxObject {

        String url = "http://localhost:8086";
        String username = "root";
        String password = "";
        String database = "hmci";

        private boolean validated = false;

        InfluxObject() { }

        InfluxObject(String url, String username, String password, String database) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.database = database;
        }

        Boolean isValid() {
            return validated;
        }

        // TODO: Fixme
        void validate() {
            validated = true;
        }

        @Override
        public String toString() {
            return url;
        }
    }


    static class HmcObject {

        String name;
        String url;
        String username;
        String password;
        Boolean unsafe = false;

        private boolean validated = false;

        HmcObject() { }

        HmcObject(String name, String url, String username, String password, Boolean unsafe) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.unsafe = unsafe;
        }


        Boolean isValid() {
            return validated;
        }

        // TODO: Fixme
        void validate() {
            validated = true;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
