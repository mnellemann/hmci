package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ServerUtil {

    public final ServerProcessor processor = new ServerProcessor();
    public final ServerMemory memory = new ServerMemory();
    public PhysicalProcessorPool physicalProcessorPool = new PhysicalProcessorPool();
    public List<SharedProcessorPool> sharedProcessorPool = new ArrayList<>();
    public Network network = new Network();

}
