package biz.nellemann.hmci

import biz.nellemann.hmci.dto.xml.ManagedSystemEntry
import biz.nellemann.hmci.dto.xml.XmlEntry
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import spock.lang.Specification

class ManagedSystemEntryTest extends Specification {


    void "parsing hmc xml managed system"() {

        setup:
        def testFile = new File(getClass().getResource('/2-managed-system.xml').toURI())
        XmlMapper xmlMapper = new XmlMapper();

        when:
        XmlEntry entry = xmlMapper.readValue(testFile, XmlEntry.class);
        ManagedSystemEntry managedSystem = entry.getContent().getManagedSystemEntry()

        then:
        managedSystem != null
        managedSystem.activatedLevel == 145
        managedSystem.activatedServicePackNameAndLevel == "FW930.50 (145)"

    }
}

