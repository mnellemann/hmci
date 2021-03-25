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

import biz.nellemann.hmci.pcm.Temperature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SystemEnergy extends MetaSystem {

    private final static Logger log = LoggerFactory.getLogger(SystemEnergy.class);

    public final ManagedSystem system;


    SystemEnergy(ManagedSystem system) {
        this.system = system;
    }


    @Override
    public String toString() {
        return system.name;
    }


    List<Measurement> getPowerMetrics() {

        List<Measurement> list = new ArrayList<>();

        HashMap<String, String> tagsMap = new HashMap<>();
        tagsMap.put("servername", system.name);
        log.trace("getPowerMetrics() - tags: " + tagsMap.toString());

        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("powerReading", metrics.systemUtil.sample.energyUtil.powerUtil.powerReading);
        log.trace("getPowerMetrics() - fields: " + fieldsMap.toString());

        list.add(new Measurement(tagsMap, fieldsMap));
        return list;
    }


    List<Measurement> getThermalMetrics() {

        List<Measurement> list = new ArrayList<>();

        HashMap<String, String> tagsMap = new HashMap<>();
        tagsMap.put("servername", system.name);
        log.trace("getThermalMetrics() - tags: " + tagsMap.toString());

        Map<String, Object> fieldsMap = new HashMap<>();

        for(Temperature t : metrics.systemUtil.sample.energyUtil.thermalUtil.cpuTemperatures) {
            fieldsMap.put("cpuTemperature_" + t.entityInstance, t.temperatureReading);
        }

        for(Temperature t : metrics.systemUtil.sample.energyUtil.thermalUtil.inletTemperatures) {
            fieldsMap.put("inletTemperature_" + t.entityInstance, t.temperatureReading);
        }

        /* Disabled, not sure if useful
        for(Temperature t : metrics.systemUtil.sample.energyUtil.thermalUtil.baseboardTemperatures) {
            fieldsMap.put("baseboardTemperature_" + t.entityInstance, t.temperatureReading);
        }*/

        log.trace("getThermalMetrics() - fields: " + fieldsMap.toString());
        list.add(new Measurement(tagsMap, fieldsMap));
        return list;
    }
}
