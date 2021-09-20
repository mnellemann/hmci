package biz.nellemann.hmci

import spock.lang.Specification

class SystemEnergyTest extends Specification {

    void "test processPcmJson for ManagedSystem Energy"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-energy.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        system.energy.processMetrics(testJson)

        then:
        system.energy.metrics.systemUtil.sample.energyUtil.powerUtil.powerReading == 542.0
        system.energy.metrics.systemUtil.sample.energyUtil.thermalUtil.cpuTemperatures.first().entityId == "CPU temperature sensors(41h)"
        system.energy.metrics.systemUtil.sample.energyUtil.thermalUtil.cpuTemperatures.first().temperatureReading == 54.0
        system.energy.metrics.systemUtil.sample.energyUtil.thermalUtil.inletTemperatures.first().temperatureReading == 26.0
        system.energy.metrics.systemUtil.sample.energyUtil.thermalUtil.baseboardTemperatures.first().entityId == "Baseboard temperature sensors(42h)"
        system.energy.metrics.systemUtil.sample.energyUtil.thermalUtil.baseboardTemperatures.first().temperatureReading == 45.0
    }


    void "test power readings for ManagedSystem Energy"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-energy.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        system.energy.processMetrics(testJson)
        List<Measurement> measurements = system.energy.getPowerMetrics()

        then:
        measurements.first().tags.get('servername') == 'Test Name'
        measurements.first().fields.get('powerReading') == 542.0f
    }


    void "test thermal readings for ManagedSystem Energy"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-energy.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        system.energy.processMetrics(testJson)
        List<Measurement> measurements = system.energy.getThermalMetrics()

        then:
        measurements.first().tags.get('servername') == 'Test Name'
        measurements.first().fields.get('cpuTemperature_1') == 46.0f
        measurements.first().fields.get('cpuTemperature_2') == 54.0f
        measurements.first().fields.get('inletTemperature_1') == 26.0f
    }

}
