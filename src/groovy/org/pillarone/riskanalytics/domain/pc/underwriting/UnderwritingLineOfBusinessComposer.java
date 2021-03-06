package org.pillarone.riskanalytics.domain.pc.underwriting;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.ComponentCategory;
import org.pillarone.riskanalytics.core.packets.Packet;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter;
import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory;
import org.pillarone.riskanalytics.core.util.GroovyUtils;
import org.pillarone.riskanalytics.domain.utils.InputFormatConverter;
import org.pillarone.riskanalytics.domain.utils.constraint.UnderwritingPortion;
import org.pillarone.riskanalytics.domain.utils.marker.ISegmentMarker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
@ComponentCategory(categories = {"UNDERWRITING"})
public class UnderwritingLineOfBusinessComposer extends Component {

    private static final String UNDERWRITING = "Underwriting";
    private static final String portion = "Portion";

    private PacketList<UnderwritingInfo> inUnderwritingInfo = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
    private PacketList<UnderwritingInfo> outUnderwritingInfo = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
    private ConstrainedMultiDimensionalParameter parmPortions = new ConstrainedMultiDimensionalParameter(
            GroovyUtils.toList("[[],[]]"),
            Arrays.asList(UNDERWRITING, portion),
            ConstraintsFactory.getConstraints(UnderwritingPortion.IDENTIFIER));


    protected void doCalculation() {
        if (inUnderwritingInfo.size() > 0) {
            List<UnderwritingInfo> lobUnderwritingInfos = new ArrayList<UnderwritingInfo>();
            int portionColumn = parmPortions.getColumnIndex(portion);
            Component lineOfBusiness = inUnderwritingInfo.get(0).sender; // works only if this component is part of a component implementing ISegmentMarker
            for (UnderwritingInfo underwritingInfo : inUnderwritingInfo) {
                String originName = underwritingInfo.origin.getName();
                int row = parmPortions.getColumnByName(UNDERWRITING).indexOf(originName);
                if (row > -1) {
                    UnderwritingInfo lobUnderwritingInfo = UnderwritingInfoPacketFactory.copy(underwritingInfo);
                    // error message in MarketUnderwritingInfoMerger (reinsurance program) if reference to same underwritingInfo
                    lobUnderwritingInfo.setOriginalUnderwritingInfo(lobUnderwritingInfo);
                    lobUnderwritingInfo.setPremium(lobUnderwritingInfo.getPremium() * InputFormatConverter.getDouble(parmPortions.getValueAt(row + 1, portionColumn)));
                    lobUnderwritingInfo.setSumInsured(lobUnderwritingInfo.getSumInsured() * InputFormatConverter.getDouble(parmPortions.getValueAt(row + 1, portionColumn)));
                    lobUnderwritingInfo.setMaxSumInsured(lobUnderwritingInfo.getMaxSumInsured() * InputFormatConverter.getDouble(parmPortions.getValueAt(row + 1, portionColumn)));
                    lobUnderwritingInfo.setCommission(lobUnderwritingInfo.getCommission() * InputFormatConverter.getDouble(parmPortions.getValueAt(row + 1, portionColumn)));
                    lobUnderwritingInfo.origin = lineOfBusiness;
                    lobUnderwritingInfo.setLineOfBusiness((ISegmentMarker) lineOfBusiness);
                    lobUnderwritingInfos.add(lobUnderwritingInfo);
                }
            }
            outUnderwritingInfo.addAll(lobUnderwritingInfos);
        }
    }

    @Override
    public void filterInChannel(PacketList inChannel, PacketList source) {
        if (inChannel == inUnderwritingInfo) {
            if (parmPortions.getRowCount() - parmPortions.getTitleRowCount() > 0) {
                for (Object underwritingInfo : source) {
                    String originName = ((Packet) underwritingInfo).origin.getNormalizedName();
                    int row = parmPortions.getColumnByName(UNDERWRITING).indexOf(originName);
                    if (row > -1) {
                        inUnderwritingInfo.add((UnderwritingInfo) underwritingInfo);
                    }
                }
            }
        }
        else {
            super.filterInChannel(inChannel, source);
        }
    }

    public PacketList<UnderwritingInfo> getInUnderwritingInfo() {
        return inUnderwritingInfo;
    }

    public void setInUnderwritingInfo(PacketList<UnderwritingInfo> inUnderwritingInfo) {
        this.inUnderwritingInfo = inUnderwritingInfo;
    }

    public PacketList<UnderwritingInfo> getOutUnderwritingInfo() {
        return outUnderwritingInfo;
    }

    public void setOutUnderwritingInfo(PacketList<UnderwritingInfo> outUnderwritingInfo) {
        this.outUnderwritingInfo = outUnderwritingInfo;
    }

    public ConstrainedMultiDimensionalParameter getParmPortions() {
        return parmPortions;
    }

    public void setParmPortions(ConstrainedMultiDimensionalParameter parmPortions) {
        this.parmPortions = parmPortions;
    }
}