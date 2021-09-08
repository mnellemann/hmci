package biz.nellemann.hmci.pcm;

public final class ViosUtil {

    public String id = "";
    public String uuid = "";
    public String name = "";
    public String state = "";
    public Integer affinityScore = 0;

    public final ViosMemory memory = new ViosMemory();
    public final LparProcessor processor = new LparProcessor();
    public final Network network = new Network();
    public final Storage storage = new Storage();

}
