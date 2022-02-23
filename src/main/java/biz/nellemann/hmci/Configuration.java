/*
 *    Copyright 2020 Mark Nellemann <mark.nellemann@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package biz.nellemann.hmci;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Configuration {

    final private Long update;
    final private Long rescan;

    final private InfluxObject influx;
    final private List<HmcObject> hmcList;

    Configuration(Path configurationFile) throws IOException {

        TomlParseResult result = Toml.parse(configurationFile);
        result.errors().forEach(error -> System.err.println(error.toString()));

        if(result.contains("hmci.update"))  {
            update = result.getLong("hmci.update");
        } else {
            update = 30L;
        }

        if(result.contains("hmci.rescan")) {
            rescan = result.getLong("hmci.rescan");
        } else {
            rescan = 60L;
        }

        hmcList = parseConfigurationForHmc(result);
        influx = parseConfigurationForInflux(result);

    }


    private List<HmcObject> parseConfigurationForHmc(TomlParseResult result) {

        ArrayList<HmcObject> list = new ArrayList<>();

        if(result.contains("hmc") && result.isTable("hmc")) {
            TomlTable hmcTable = result.getTable("hmc");
            if(hmcTable == null) {
                return list;
            }
            for(String key : hmcTable.keySet()) {

                HmcObject c = new HmcObject();
                c.name = key;
                c.update = update;
                c.rescan = rescan;

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

                if(hmcTable.contains(key+".energy")) {
                    c.energy = hmcTable.getBoolean(key+".energy");
                } else {
                    c.energy = true;
                }

                if(hmcTable.contains(key+".trace")) {
                    c.trace = hmcTable.getString(key+".trace");
                } else {
                    c.trace = null;
                }

                if(hmcTable.contains(key+".excludeSystems")) {
                    List<Object> tmpList = hmcTable.getArrayOrEmpty(key+".excludeSystems").toList();
                    c.excludeSystems = tmpList.stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.toList());
                } else {
                    c.excludeSystems = new ArrayList<>();
                }

                if(hmcTable.contains(key+".includeSystems")) {
                    List<Object> tmpList = hmcTable.getArrayOrEmpty(key+".includeSystems").toList();
                    c.includeSystems = tmpList.stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.toList());
                } else {
                    c.includeSystems = new ArrayList<>();
                }

                if(hmcTable.contains(key+".excludePartitions")) {
                    List<Object> tmpList = hmcTable.getArrayOrEmpty(key+".excludePartitions").toList();
                    c.excludePartitions = tmpList.stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.toList());
                } else {
                    c.excludePartitions = new ArrayList<>();
                }

                if(hmcTable.contains(key+".includePartitions")) {
                    List<Object> tmpList = hmcTable.getArrayOrEmpty(key+".includePartitions").toList();
                    c.includePartitions = tmpList.stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.toList());
                } else {
                    c.includePartitions = new ArrayList<>();
                }

                list.add(c);
            }
        }

        return list;
    }


    private InfluxObject parseConfigurationForInflux(TomlParseResult result) {

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


    public List<HmcObject> getHmc() {
        return hmcList;
    }


    public InfluxObject getInflux() {
        return influx;
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

        // TODO: Implement validation
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
        Boolean energy = true;
        String trace;
        List<String> excludeSystems;
        List<String> includeSystems;
        List<String> excludePartitions;
        List<String> includePartitions;
        Long update = 30L;
        Long rescan = 60L;

        private boolean validated = false;

        HmcObject() { }

        HmcObject(String name, String url, String username, String password, Boolean unsafe, Long update, Long rescan) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.unsafe = unsafe;
            this.update = update;
            this.rescan = rescan;
        }


        Boolean isValid() {
            return validated;
        }

        // TODO: Implement validation
        void validate() {
            validated = true;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
