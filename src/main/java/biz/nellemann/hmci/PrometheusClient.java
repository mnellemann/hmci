package biz.nellemann.hmci;

import biz.nellemann.hmci.dto.toml.InfluxConfiguration;
import biz.nellemann.hmci.dto.toml.PrometheusConfiguration;
import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Info;
import io.prometheus.metrics.core.metrics.Metric;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.snapshots.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PrometheusClient {

    private final static Logger log = LoggerFactory.getLogger(PrometheusClient.class);

    private final Map<String, Metric> registered = new HashMap<>();


    public PrometheusClient(PrometheusConfiguration config) throws IOException {

        //JvmMetrics.builder().register(); // initialize the out-of-the-box JVM metrics
        HTTPServer server = HTTPServer.builder()
            .port(config.port)
            .buildAndStart();
        log.debug("HTTPServer listening on port http://localhost:{}/metrics", server.getPort());
    }


    public void write(List<MeasurementBundle> bundle) {
        //log.debug("write() - bundles: {}", bundle.size());
        if(!bundle.isEmpty()) {
            bundle.forEach(this::bundle);
        }
    }


    private void bundle(MeasurementBundle bundle) {
        //log.debug("bundle() - bundle: {}", bundle.name);

        String[] labelNames = new String[bundle.tags.size()];
        String[] labelValues = new String[bundle.tags.size()];
        int index = 0;
        for (Map.Entry<String, String> mapEntry : bundle.tags.entrySet()) {
            labelNames[index] = mapEntry.getKey();
            labelValues[index] = mapEntry.getValue();
            index++;
        }

        try {
            bundle.items.forEach((item) -> {
                String name = bundle.name + "_" + item.key;
                register(name, labelNames, item);
                process(name, labelValues, item);
            });
        } catch(Exception e) {
            log.error("bundle() - error: {}", e.getMessage());
        }

    }


    private void register(String name, String[] labels, MeasurementItem item) {

        if(registered.containsKey(name)) {
            return;
        }
        log.debug("register() - name: {}", name);

        Unit unit;
        switch (item.getMeasurementUnit()) {
            case UNITS:
                unit = new Unit("units");
                break;
            case BYTES:
                unit = Unit.BYTES;
                break;
            case SECONDS:
                unit = Unit.SECONDS;
                break;
            case CELSIUS:
                unit = Unit.CELSIUS;
                break;
            case RATIO:
                unit = Unit.RATIO;
            default:
                unit = new Unit(item.getMeasurementUnit().name().toLowerCase());
        }

        if(item.type.equals(MeasurementType.COUNTER)) {
            Counter counter = Counter.builder()
                .name(name)
                .help(item.getDescription())
                .unit(unit)
                .labelNames(labels)
                .register();
            registered.put(name, counter);
            log.info(counter.toString());
        }

        if (item.type.equals(MeasurementType.GAUGE)) {
            Gauge gauge = Gauge.builder()
                .name(name)
                .help(item.getDescription())
                .unit(unit)
                .labelNames(labels)
                .register();
            registered.put(name, gauge);
            log.info(gauge.toString());
        }

        // Info is special, we treat the items also as labels
        if(item.type.equals(MeasurementType.INFO)) {
            Info info = Info.builder()
                .name(name)
                .labelNames(labels)
                .register();
            registered.put(name, info);
            log.info(info.toString());
        }

    }


    private void process(String name, String[] labelValues, MeasurementItem item) {

        Metric m = registered.get(name);
        if(m instanceof Counter) {
            //log.debug("process() - name: {}, type: COUNTER", name);
            long v = (long) item.value;
            ((Counter)m).labelValues(labelValues).inc(v);
        } else if(m instanceof Gauge) {
            //log.debug("process() - name: {}, type: GAUGE", name);
            double v = (double) item.value;
            ((Gauge)m).labelValues(labelValues).set(v);
        }
        /*
        else if(m instanceof Info) {
            String[] newLabels = Arrays.copyOf(labelValues, labelValues.length + 1);
            newLabels[labelValues.length] = item.key;

            log.info("process() - name: {}, type: INFO", name);
            String v = (String) item.value;
            ((Info)m).setLabelValues(newLabels);
        }*/
    }

}
