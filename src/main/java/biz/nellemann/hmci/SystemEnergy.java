package biz.nellemann.hmci;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import biz.nellemann.hmci.dto.json.Temperature;
import biz.nellemann.hmci.dto.xml.Link;
import biz.nellemann.hmci.dto.xml.XmlFeed;

class SystemEnergy extends Resource {

    private final static Logger log = LoggerFactory.getLogger(SystemEnergy.class);

    private final Session session;
    private final ManagedSystem managedSystem;

    protected String id;
    protected String name;


    public SystemEnergy(Session session, ManagedSystem managedSystem) {
        log.debug("SystemEnergy()");
        this.session = session;
        this.managedSystem = managedSystem;
    }


    public void refresh() {

        log.debug("refresh()");
        try {
            String xml = session.getRestClient().getRequest(String.format("/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?Type=Energy&NoOfSamples=%d", managedSystem.id, noOfSamples));

            // Do not try to parse empty response
            if(xml == null || xml.length() <= 1) {
                log.debug("refresh() - no data.");  // We do not log as 'warn' as many systems do not have this enabled.
                return;
            }

            XmlMapper xmlMapper = new XmlMapper();
            XmlFeed xmlFeed = xmlMapper.readValue(xml, XmlFeed.class);

            xmlFeed.entries.forEach((entry) -> {
                if (entry.category.term.equals("ManagedSystem")) {
                    Link link = entry.link;
                    if (link.getType() != null && Objects.equals(link.getType(), "application/json")) {
                        try {
                            URI jsonUri = URI.create(link.getHref());
                            String json = session.getRestClient().getRequest(jsonUri.getPath());
                            deserialize(json);
                        } catch (IOException e) {
                            log.error("refresh() - error 1: {}", e.getMessage());
                        }
                    }
                }
            });

        } catch (IOException e) {
            log.error("refresh() - error: {} {}", e.getClass(), e.getMessage());
        }

    }



    @Override
    public void process(int sample) {
        if(metric != null) {
            log.debug("process() - sample: {}", sample);

            List<MeasurementBundle> powerMeasurementGroups = getPowerMetrics(sample);
            List<MeasurementBundle> thermalMeasurementGroups = getThermalMetrics(sample);

            session.writeMetric(powerMeasurementGroups);
            session.writeMetric(thermalMeasurementGroups);
        }
    }


    List<MeasurementBundle> getPowerMetrics(int sample) {

        List<MeasurementBundle> list = new ArrayList<>();
        try {
            HashMap<String, String> tags = new HashMap<>();
            Map<String, Object> fields = new HashMap<>();
            List<MeasurementItem> items = new ArrayList<>();

            tags.put("system", managedSystem.name);
            log.trace("getPowerMetrics() - tags: {}", tags);

            fields.put("watts", metric.getSample(sample).energyUtil.powerUtil.powerReading);
            items.add(
                new MeasurementItem(
                    MeasurementType.GAUGE,
                    MeasurementUnit.WATTS,
                    "power",
                    metric.getSample(sample).energyUtil.powerUtil.powerReading)
            );
            log.trace("getPowerMetrics() - fields: {}", items);

            list.add(new MeasurementBundle(getTimestamp(sample), "system_energy", tags, fields, items));
        } catch (Exception e) {
            log.warn("getPowerMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    List<MeasurementBundle> getThermalMetrics(int sample) {

        List<MeasurementBundle> bundles = new ArrayList<>();
        try {
            HashMap<String, String> tags = new HashMap<>();
            Map<String, Object> fields = new HashMap<>();
            List<MeasurementItem> items = new ArrayList<>();

            tags.put("system", managedSystem.name);
            log.trace("getThermalMetrics() - tags: {}", tags);

            // Only store 1st CPU temperature
            if(metric.getSample(sample).energyUtil.thermalUtil.cpuTemperatures.size() >= 1) {
                Temperature t = metric.getSample(sample).energyUtil.thermalUtil.cpuTemperatures.get(0);
                fields.put("cpu__" + t.entityInstance, t.temperatureReading);
                items.add(
                    new MeasurementItem(
                        MeasurementType.GAUGE,
                        MeasurementUnit.CELSIUS,
                        "cpu_" + t.entityInstance,
                            t.temperatureReading));
            }

            metric.getSample(sample).energyUtil.thermalUtil.inletTemperatures.forEach((t) -> {
                fields.put("inlet_" + t.entityInstance, t.temperatureReading);

                items.add(
                    new MeasurementItem(
                        MeasurementType.GAUGE,
                        MeasurementUnit.CELSIUS,
                        "inlet_" + t.entityInstance,
                        t.temperatureReading)
                );
            });

            /* Disabled, not sure if useful
            for(Temperature t : metrics.systemUtil.sample.energyUtil.thermalUtil.baseboardTemperatures) {
                fieldsMap.put("baseboardTemperature_" + t.entityInstance, t.temperatureReading);
            }*/
            log.trace("getThermalMetrics() - fields: {}", fields);

            bundles.add(new MeasurementBundle(getTimestamp(sample), "system_thermal", tags, fields, items));

        } catch (Exception e) {
            log.warn("getThermalMetrics() - error: {}", e.getMessage());
        }

        return bundles;
    }

}

