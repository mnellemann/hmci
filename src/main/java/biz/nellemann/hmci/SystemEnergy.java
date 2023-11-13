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

import biz.nellemann.hmci.dto.xml.Link;
import biz.nellemann.hmci.dto.xml.XmlFeed;

class SystemEnergy extends Resource {

    private final static Logger log = LoggerFactory.getLogger(SystemEnergy.class);

    private final RestClient restClient;
    private final InfluxClient influxClient;
    private final ManagedSystem managedSystem;

    protected String id;
    protected String name;


    public SystemEnergy(RestClient restClient, InfluxClient influxClient, ManagedSystem managedSystem) {
        log.debug("SystemEnergy()");
        this.restClient = restClient;
        this.influxClient = influxClient;
        this.managedSystem = managedSystem;
    }


    public void refresh() {

        log.debug("refresh()");
        try {
            String xml = restClient.getRequest(String.format("/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?Type=Energy&NoOfSamples=%d", managedSystem.id, noOfSamples));

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
                            String json = restClient.getRequest(jsonUri.getPath());
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
            influxClient.write(getPowerMetrics(sample), "server_energy_power");
            influxClient.write(getThermalMetrics(sample), "server_energy_thermal");
        }
    }


    List<Measurement> getPowerMetrics(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            HashMap<String, String> tagsMap = new HashMap<>();
            Map<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", managedSystem.name);
            log.trace("getPowerMetrics() - tags: {}", tagsMap);

            fieldsMap.put("powerReading", metric.getSample(sample).energyUtil.powerUtil.powerReading);
            log.trace("getPowerMetrics() - fields: {}", fieldsMap);

            list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getPowerMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    List<Measurement> getThermalMetrics(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            HashMap<String, String> tagsMap = new HashMap<>();
            Map<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", managedSystem.name);
            log.trace("getThermalMetrics() - tags: {}", tagsMap);

            metric.getSample(sample).energyUtil.thermalUtil.cpuTemperatures.forEach((t) -> {
                fieldsMap.put("cpuTemperature_" + t.entityInstance, t.temperatureReading);
            });

            metric.getSample(sample).energyUtil.thermalUtil.inletTemperatures.forEach((t) -> {
                fieldsMap.put("inletTemperature_" + t.entityInstance, t.temperatureReading);
            });

            /* Disabled, not sure if useful
            for(Temperature t : metrics.systemUtil.sample.energyUtil.thermalUtil.baseboardTemperatures) {
                fieldsMap.put("baseboardTemperature_" + t.entityInstance, t.temperatureReading);
            }*/
            log.trace("getThermalMetrics() - fields: {}", fieldsMap);


            list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));

        } catch (Exception e) {
            log.warn("getThermalMetrics() - error: {}", e.getMessage());
        }

        return list;
    }

}

