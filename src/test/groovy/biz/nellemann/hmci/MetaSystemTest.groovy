package biz.nellemann.hmci

import spock.lang.Specification

import java.time.Instant

class MetaSystemTest extends Specification {

    void "test timestamp retrieval from xml"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("site1", "e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        system.processMetrics(testJson)
        Instant instant = system.getTimestamp()

        then:
        instant.getEpochSecond() == 1597086630
    }

}
