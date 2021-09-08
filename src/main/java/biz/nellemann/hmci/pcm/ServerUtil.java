package biz.nellemann.hmci.pcm;

import java.util.ArrayList;
import java.util.List;

public final class ServerUtil {

    public final ServerProcessor processor = new ServerProcessor();
    public final ServerMemory memory = new ServerMemory();
    public final PhysicalProcessorPool physicalProcessorPool = new PhysicalProcessorPool();
    public final List<SharedProcessorPool> sharedProcessorPool = new ArrayList<>();

}
