package biz.nellemann.hmci


import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor
import groovy.util.logging.Slf4j

@Slf4j
class App {

    App(String... args) {

        println("App()")

        Map<String,ManagedSystem> systems = new HashMap<String, ManagedSystem>()
        Map<String, LogicalPartition> partitions = new HashMap<String, LogicalPartition>()


        HmcClient hmc
        try {
            hmc = new HmcClient("https://10.32.64.39:12443", "hmci", "hmcihmci")
            hmc.login()

            hmc.getManagedSystems().each { systemKey, system ->

                // Add to list of known systems
                systems.putIfAbsent(systemKey, system)

                // Get and process metrics for this system
                String json = hmc.getPcmForManagedSystemWithId(system)
                system.processPcmJson(json)


                /*hmc.getLogicalPartitionsForManagedSystem(systemValue).each { lparKey, lpar ->
                    partitions.putIfAbsent(lparKey, lpar)
                    //hmc.get
                }*/

            }


            hmc.logoff()
        } catch(Exception e) {
            log.error(e.message)
        }

    }


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

        // TODO: Read configuration file or create new empty file,
        //       pass the properties or configuration bean to App.

        new App(args)

        System.exit(0);
    }


}