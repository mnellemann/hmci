package biz.nellemann.hmci.pcm;

import java.util.ArrayList;
import java.util.List;

public class VirtualFiberChannelAdapter {

    public String wwpn = "";
    public String wwpn2 = "";
    public String physicalLocation = "";
    public String physicalPortWWPN = "";
    public Integer viosId = 0;
    public List<Number> numOfReads = new ArrayList<>();
    public List<Number> numOfWrites = new ArrayList<>();
    public List<Number> readBytes = new ArrayList<>();
    public List<Number> writeBytes = new ArrayList<>();
    public List<Number> runningSpeed = new ArrayList<>();
    public List<Number> transmittedBytes = new ArrayList<>();

}
