package biz.nellemann.hmci.pcm;

public final class LparUtil {

    public Integer id = 0;
    public String uuid = "";
    public String name = "";
    public String state = "";
    public String type = "";
    public String osType = "";
    public Number affinityScore = 0.0f;

    public final LparMemory memory = new LparMemory();
    public final LparProcessor processor = new LparProcessor();
    public final Network network = new Network();
    public final Storage storage = new Storage();

}
