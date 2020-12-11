package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

import java.util.ArrayList;
import java.util.List;

public class UtilSample {

    public String sampleType;
    public SampleInfo sampleInfo;
    public ServerUtil serverUtil;
    public EnergyUtil energyUtil = new EnergyUtil();
    public List<ViosUtil> viosUtil = new ArrayList<>();

    @FirstElement
    public LparUtil lparsUtil;

    /*
    public LparUtil getLparsUtil() {
        if(lparsUtil == null || lparsUtil.isEmpty()) {
            return new LparUtil();
        } else {
            return lparsUtil.get(0);
        }
    }*/


}
