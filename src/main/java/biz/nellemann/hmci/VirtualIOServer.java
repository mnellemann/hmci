package biz.nellemann.hmci;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import biz.nellemann.hmci.dto.xml.VirtualIOServerEntry;
import biz.nellemann.hmci.dto.xml.XmlEntry;

public class VirtualIOServer {
    private final static Logger log = LoggerFactory.getLogger(VirtualIOServer.class);

    private final RestClient restClient;
    private final ManagedSystem managedSystem;

    protected String id;
    private String uriPath;
    protected VirtualIOServerEntry entry;


    public VirtualIOServer(RestClient restClient, String href, ManagedSystem system) {
        log.debug("VirtualIOServer() - {}", href);
        this.restClient = restClient;
        this.managedSystem = system;
        try {
            URI uri = new URI(href);
            uriPath = uri.getPath();
            //refresh();
        } catch (URISyntaxException e) {
            log.error("VirtualIOServer() - {}", e.getMessage());
        }
    }

    public void discover() {
        try {
            String xml = restClient.getRequest(uriPath);

            // Do not try to parse empty response
            if(xml == null || xml.length() <= 1) {
                log.warn("discover() - no data.");
                return;
            }

            XmlMapper xmlMapper = new XmlMapper();
            XmlEntry xmlEntry = xmlMapper.readValue(xml, XmlEntry.class);

            if(xmlEntry.getContent() == null){
                log.warn("discover() - no content.");
                return;
            }

            if(xmlEntry.getContent().isVirtualIOServer()) {
                entry = xmlEntry.getContent().getVirtualIOServerEntry();
                log.debug("discover() - {}", entry.getName());
            } else {
                throw new UnsupportedOperationException("Failed to deserialize VirtualIOServer");
            }

        } catch (IOException e) {
            log.error("discover() - error: {}", e.getMessage());
        }
    }

}
