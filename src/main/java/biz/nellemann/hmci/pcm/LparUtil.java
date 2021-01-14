package biz.nellemann.hmci.pcm;

public class LparUtil {

    public Integer id = 0;
    public String uuid = "";
    public String name = "";
    public String state = "";
    public String type = "";
    public String osType = "";
    public Number affinityScore = 0.0f;

    public LparMemory memory = new LparMemory();
    public LparProcessor processor = new LparProcessor();
    public Network network = new Network();
    public Storage storage = new Storage();

}
