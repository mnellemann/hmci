package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

import java.util.ArrayList;
import java.util.List;

public class UtilSample {

    public String sampleType = "";
    public SampleInfo sampleInfo = new SampleInfo();
    public SystemFirmware systemFirmwareUtil = new SystemFirmware();
    public ServerUtil serverUtil = new ServerUtil();
    public EnergyUtil energyUtil = new EnergyUtil();
    public List<ViosUtil> viosUtil = new ArrayList<>();

    @FirstElement
    public LparUtil lparsUtil = new LparUtil();

}
