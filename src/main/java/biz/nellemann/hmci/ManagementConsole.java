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

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import biz.nellemann.hmci.dto.xml.Link;
import biz.nellemann.hmci.dto.xml.ManagementConsoleEntry;
import biz.nellemann.hmci.dto.xml.XmlFeed;

class ManagementConsole implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ManagementConsole.class);

    private final List<ManagedSystem> managedSystems = new ArrayList<>();
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);
    protected Integer responseErrors = 0;
    private Session session;

    ManagementConsole(Session session) {
        this.session = session;
    }


    @Override
    public void run() {

        log.trace("run()");

        Instant lastDiscover = Instant.now();
        session.getRestClient().login();
        discover();

        do {
            Instant instantStart = Instant.now();
            try {
                refresh();
                if(instantStart.isAfter(lastDiscover.plus(session.discoverValue, ChronoUnit.MINUTES))) {
                    lastDiscover = instantStart;
                    discover();
                }
            } catch (Exception e) {
                log.error("run() - fatal error: {}", e.getMessage());
                keepRunning.set(false);
                throw new RuntimeException(e);
            }

            Instant instantEnd = Instant.now();
            long timeSpend = Duration.between(instantStart, instantEnd).toMillis();
            log.trace("run() - duration millis: " + timeSpend);
            if(timeSpend < (session.refreshValue * 1000)) {
                try {
                    long sleepTime = (session.refreshValue * 1000) - timeSpend;
                    log.trace("run() - sleeping millis: " + sleepTime);
                    if(sleepTime > 0) {
                        //noinspection BusyWait
                        sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    log.error("run() - sleep interrupted", e);
                }
            } else {
                log.warn("run() - possible slow response from this HMC");
            }

        } while (keepRunning.get());


        // Logout of HMC
        session.getRestClient().logoff();
    }

    public void setServices(Session session) {
        this.session = session;
    }


    private void discover() {

        try {
            String xml = session.getRestClient().getRequest("/rest/api/uom/ManagementConsole");

            // Do not try to parse empty response
            if(xml == null || xml.length() <= 1) {
                responseErrors++;
                log.warn("discover() - no data.");
                return;
            }

            XmlMapper xmlMapper = new XmlMapper();
            XmlFeed xmlFeed = xmlMapper.readValue(xml, XmlFeed.class);
            ManagementConsoleEntry entry;

            if(xmlFeed.getEntry() == null){
                log.warn("discover() - xmlFeed.entry == null");
                return;
            }

            if(xmlFeed.getEntry().getContent().isManagementConsole()) {
                entry = xmlFeed.getEntry().getContent().getManagementConsole();
                //log.info("discover() - {}", entry.getName());
            } else {
                throw new UnsupportedOperationException("Failed to deserialize ManagementConsole");
            }

            managedSystems.clear();
            for (Link link : entry.getAssociatedManagedSystems()) {
                ManagedSystem managedSystem = new ManagedSystem(session, link.getHref());
                managedSystem.setExcludePartitions(session.excludePartitions);
                managedSystem.setIncludePartitions(session.includePartitions);
                managedSystem.discover();

                // Only continue for powered-on operating systems
                if(managedSystem.entry != null && Objects.equals(managedSystem.entry.state, "operating")) {

                    if(session.doEnergy) {
                        managedSystem.getPcmPreferences();
                        managedSystem.setDoEnergy(session.doEnergy);
                    }

                    // Check exclude / include
                    if (!session.excludeSystems.contains(managedSystem.name) && session.includeSystems.isEmpty()) {
                        managedSystems.add(managedSystem);
                        //log.info("discover() - adding !excluded system: {}", managedSystem.name);
                    } else if (!session.includeSystems.isEmpty() && session.includeSystems.contains(managedSystem.name)) {
                        managedSystems.add(managedSystem);
                        //log.info("discover() - adding included system: {}", managedSystem.name);
                    }
                }
            }

        } catch (IOException e) {
            log.warn("discover() - error: {}", e.getMessage());
        }

    }


    private void refresh() {

        log.debug("refresh()");
        managedSystems.forEach( (system) -> {

            if(system.entry == null){
                log.warn("refresh() - no data.");
                return;
            }

            system.refresh();
            system.process();

        });

    }


}
