package biz.nellemann.hmci.pcm;

import java.util.ArrayList;
import java.util.List;

public class ServerUtil {

    public ServerProcessor processor = new ServerProcessor();
    public ServerMemory memory = new ServerMemory();
    public PhysicalProcessorPool physicalProcessorPool = new PhysicalProcessorPool();
    public List<SharedProcessorPool> sharedProcessorPool = new ArrayList<>();

}
