/*
   Copyright 2020 mark.nellemann@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package biz.nellemann.hmci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "hmci",
    mixinStandardHelpOptions = true,
    description = "HMC Insights.",
    versionProvider = biz.nellemann.hmci.VersionProvider.class)
public class Main implements Callable<Integer> {

    private final static Logger log = LoggerFactory.getLogger(Main.class);

    @SuppressWarnings("FieldMayBeFinal")
    @CommandLine.Option(names = { "-c", "--conf" }, description = "Configuration file [default: '/etc/hmci.toml'].")
    private String configurationFile = "/etc/hmci.toml";

    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }


    @Override
    public Integer call() throws IOException {

        File file = new File(configurationFile);
        if(!file.exists()) {
            System.err.println("Error - No configuration file found at: " + file.toString());
            return -1;
        }

        Configuration configuration = new Configuration(configurationFile);
        Insights insights = new Insights(configuration);
        try {
            insights.run();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        return 0;
    }

}
