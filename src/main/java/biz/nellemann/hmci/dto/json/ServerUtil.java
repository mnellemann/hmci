package biz.nellemann.hmci.dto.json;


import java.util.ArrayList;
import java.util.List;

public final class ServerUtil {

    public final ServerProcessor processor = new ServerProcessor();
    public final ServerMemory memory = new ServerMemory();
    public PhysicalProcessorPool physicalProcessorPool = new PhysicalProcessorPool();
    public List<SharedProcessorPool> sharedProcessorPool = new ArrayList<>();
    public Network network = new Network();

}
