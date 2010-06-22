package org.pillarone.riskanalytics.domain.pc.underwriting;

import org.pillarone.riskanalytics.core.components.Component;
import org.pillarone.riskanalytics.core.components.IterationStore;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.parameterization.TableMultiDimensionalParameter;
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope;
import org.pillarone.riskanalytics.core.util.GroovyUtils;
import org.pillarone.riskanalytics.domain.utils.InputFormatConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The RiskBands class allows one to specify (in tabular format) a set of risk bands,
 * each (row) of which defines the values (columns):
 * - maximum sum insured (packet property: maxSumInsured)
 * - average sum insured (packet property: sumInsured)
 * - premium (lower limit; packet property: premiumWritten)
 * - number of policies (packet property: numberOfPolicies)
 *
 * An instance will emit outUnderwritingInfo packets, one per segment/band defined
 * (i.e. one underwriting packet for each row in the table), with the property names
 * indicated above. In addition, each packet will have a property premiumWrittenAsIf,
 * identical to premiumWritten, an origin pointing to the Riskband instance emitting the packet,
 * and a self-referential originalUnderwritingInfo property (i.e. pointing to the packet itself).
 *
 * @author martin.melchior (at) fhnw (dot) ch, stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class RiskBands extends Component implements IUnderwritingInfoMarker {

    private TableMultiDimensionalParameter parmUnderwritingInformation = new TableMultiDimensionalParameter(
            GroovyUtils.convertToListOfList(new Object[]{0d, 0d, 0d, 0d}),
            Arrays.asList(MAXIMUM_SUM_INSURED, AVERAGE_SUM_INSURED, PREMIUM, NUMBER_OF_POLICIES));

    private PacketList<UnderwritingInfo> outUnderwritingInfo = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);

    private IterationScope iterationScope;
    private IterationStore iterationStore;

    private static final String UNDERWRITING_INFOS = "underwriting infos";
    private static final String MAXIMUM_SUM_INSURED = "maximum sum insured";
    private static final String AVERAGE_SUM_INSURED = "average sum insured";
    private static final String PREMIUM = "premium";
    private static final String NUMBER_OF_POLICIES = "number of policies";

    private Integer numberOfSegments;
    private int columnIndexMaxSumInsured;
    private int columnIndexAverageSumInsured;
    private int columnIndexPremium;
    private int columnIndexNumberOfPolicies;


    public void doCalculation() {
        initSimulation();
        initDuringFirstIteration();
        outUnderwritingInfo.addAll((Collection<? extends UnderwritingInfo>) iterationStore.get(UNDERWRITING_INFOS));
    }

    private void initSimulation() {
        if (numberOfSegments == null) {
            numberOfSegments = parmUnderwritingInformation.getRowCount();
            columnIndexMaxSumInsured = parmUnderwritingInformation.getColumnIndex(MAXIMUM_SUM_INSURED);
            columnIndexAverageSumInsured = parmUnderwritingInformation.getColumnIndex(AVERAGE_SUM_INSURED);
            columnIndexPremium = parmUnderwritingInformation.getColumnIndex(PREMIUM);
            columnIndexNumberOfPolicies = parmUnderwritingInformation.getColumnIndex(NUMBER_OF_POLICIES);
        }
    }

    private void initDuringFirstIteration() {
        if (!iterationScope.isFirstIteration()) return;
        List<UnderwritingInfo> underwritingInfos = new ArrayList<UnderwritingInfo>(numberOfSegments);
        for (int i = 1; i < numberOfSegments; i++) {
            UnderwritingInfo underwritingInfo = UnderwritingInfoPacketFactory.createPacket();
            underwritingInfo.premiumWritten = InputFormatConverter.getDouble(parmUnderwritingInformation.getValueAt(i, columnIndexPremium));
            underwritingInfo.premiumWrittenAsIf = underwritingInfo.premiumWritten;
            underwritingInfo.maxSumInsured = InputFormatConverter.getDouble(parmUnderwritingInformation.getValueAt(i, columnIndexMaxSumInsured));
            underwritingInfo.sumInsured = InputFormatConverter.getDouble(parmUnderwritingInformation.getValueAt(i, columnIndexAverageSumInsured));
            underwritingInfo.numberOfPolicies = InputFormatConverter.getDouble(parmUnderwritingInformation.getValueAt(i, columnIndexNumberOfPolicies));
            underwritingInfo.origin = this;
            underwritingInfo.originalUnderwritingInfo = underwritingInfo;
            underwritingInfos.add(underwritingInfo);
        }
        iterationStore.put(UNDERWRITING_INFOS, underwritingInfos);
    }

    public TableMultiDimensionalParameter getParmUnderwritingInformation() {
        return parmUnderwritingInformation;
    }

    public void setParmUnderwritingInformation(TableMultiDimensionalParameter parmUnderwritingInformation) {
        this.parmUnderwritingInformation = parmUnderwritingInformation;
    }

    public PacketList<UnderwritingInfo> getOutUnderwritingInfo() {
        return outUnderwritingInfo;
    }

    public void setOutUnderwritingInfo(PacketList<UnderwritingInfo> outUnderwritingInfo) {
        this.outUnderwritingInfo = outUnderwritingInfo;
    }

    public IterationStore getIterationStore() {
        return iterationStore;
    }

    public void setIterationStore(IterationStore iterationStore) {
        this.iterationStore = iterationStore;
    }

    public IterationScope getIterationScope() {
        return iterationScope;
    }

    public void setIterationScope(IterationScope iterationScope) {
        this.iterationScope = iterationScope;
    }
}