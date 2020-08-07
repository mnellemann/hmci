package biz.nellemann.hmci


import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor
import groovy.util.logging.Slf4j

@Slf4j
class App {

    static void main(String... args) {

        def cli = new CliBuilder()
        cli.h(longOpt: 'help', 'display usage')
        cli.v(longOpt: 'version', 'display version')
        cli.c(longOpt: 'config', args: 1, required: true, defaultValue: '~/.config/hmci.properties', 'configuration file')


        OptionAccessor options = cli.parse(args)
        if (options.h) cli.usage()

        if(options.c) {
            //println("TODO: Use configuration file: " + options.config)
        }

        Hmc hmc
        try {
            hmc = new Hmc("https://10.32.64.39:12443", "hmci", "hmcihmci")
            hmc.login()

            hmc.getManagedSystems()
            hmc.getLogicalPartitions()
            hmc.getProcessedMetrics()

            hmc.logoff()
        } catch(Exception e) {
            log.error(e.message)
        }


        System.exit(0);
    }


}