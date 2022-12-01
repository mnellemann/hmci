package biz.nellemann.hmci

import biz.nellemann.hmci.dto.xml.ManagementConsoleEntry
import biz.nellemann.hmci.dto.xml.XmlEntry
import biz.nellemann.hmci.dto.xml.XmlFeed
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import spock.lang.Specification


class ManagementConsoleEntryTest extends Specification {

    void "parsing hmc xml feed and entry"() {

        setup:
        def testFile = new File(getClass().getResource('/1-hmc.xml').toURI())
        XmlMapper xmlMapper = new XmlMapper();

        when:
        XmlFeed feed = xmlMapper.readValue(testFile.getText(), XmlFeed.class);
        XmlEntry entry = feed.entry;

        then:
        feed.id == "347ecfcf-acac-3724-8915-a3d7d7a6f298"
        //feed.links.first().rel == "SELF"
        //feed.links.first().href == "https://10.32.64.39:12443/rest/api/uom/ManagementConsole"
        entry.id == "2c6b6620-e3e3-3294-aaf5-38e546ff672b"
        entry.title == "ManagementConsole"

    }

    void "parsing hmc xml management console"() {

        setup:
        def testFile = new File(getClass().getResource('/1-hmc.xml').toURI())
        XmlMapper xmlMapper = new XmlMapper();

        when:
        XmlFeed feed = xmlMapper.readValue(testFile.getText(), XmlFeed.class);
        ManagementConsoleEntry managementConsole = feed.entry.getContent().getManagementConsole();

        then:
        managementConsole.getMachineTypeModelAndSerialNumber() != null
        managementConsole.getMachineTypeModelAndSerialNumber().getMachineType() == "7042"
        managementConsole.getMachineTypeModelAndSerialNumber().getModel() == "CR7"
        managementConsole.getMachineTypeModelAndSerialNumber().getSerialNumber() == "21D3CBC"
        managementConsole.getAssociatedManagedSystems().size() == 1;
        managementConsole.getAssociatedManagedSystems().first().href == "https://10.32.64.39:12443/rest/api/uom/ManagementConsole/2c6b6620-e3e3-3294-aaf5-38e546ff672b/ManagedSystem/b597e4da-2aab-3f52-8616-341d62153559"
        managementConsole.managementConsoleName == 'HMC-P9\n                '
        managementConsole.versionInfo.buildLevel == '2011270432'
        managementConsole.versionInfo.servicePackName == '942'
        managementConsole.iFixDetails.iFixDetailList.size() == 2
        managementConsole.procConfiguration.numberOfProcessors == 6
        managementConsole.procConfiguration.modelName == "Intel(R) Xeon(R) CPU E5-2640 0 @ 2.50GHz"
        managementConsole.procConfiguration.architecture == "x86_64"
        managementConsole.memConfiguration.totalMemory == 7957
        managementConsole.memConfiguration.totalSwapMemory == 2046

    }

}
