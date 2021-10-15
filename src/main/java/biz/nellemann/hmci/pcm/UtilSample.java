package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

import java.util.ArrayList;
import java.util.List;

public final class UtilSample {

    public String sampleType = "";
    public final SampleInfo sampleInfo = new SampleInfo();
    public final SystemFirmware systemFirmwareUtil = new SystemFirmware();
    public final ServerUtil serverUtil = new ServerUtil();
    public final EnergyUtil energyUtil = new EnergyUtil();
    public final List<ViosUtil> viosUtil = new ArrayList<>();

    @FirstElement
    public final LparUtil lparsUtil = new LparUtil();

}
