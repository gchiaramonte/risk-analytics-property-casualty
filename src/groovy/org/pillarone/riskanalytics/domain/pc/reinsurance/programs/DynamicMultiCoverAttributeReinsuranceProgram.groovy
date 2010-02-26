package org.pillarone.riskanalytics.domain.pc.reinsurance.programs

import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.ReinsuranceContractStrategyFactory
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.ReinsuranceContractType
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.MultiCoverAttributeReinsuranceContract
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.cover.CoverAttributeStrategyType
import org.pillarone.riskanalytics.domain.pc.constants.IncludeType
import org.pillarone.riskanalytics.domain.pc.reinsurance.ReinsuranceResultWithCommissionPacket
import org.pillarone.riskanalytics.core.packets.PacketList

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class DynamicMultiCoverAttributeReinsuranceProgram extends DynamicReinsuranceProgram {

    PacketList<ReinsuranceResultWithCommissionPacket> outContractFinancials = new PacketList<ReinsuranceResultWithCommissionPacket>(ReinsuranceResultWithCommissionPacket.class);

    public MultiCoverAttributeReinsuranceContract createDefaultSubComponent() {
        MultiCoverAttributeReinsuranceContract contract = new MultiCoverAttributeReinsuranceContract(
                parmInuringPriority: 0,
                parmContractStrategy: ReinsuranceContractStrategyFactory.getContractStrategy(ReinsuranceContractType.TRIVIAL, [:]),
                parmCover: CoverAttributeStrategyType.getStrategy(
                            CoverAttributeStrategyType.ALL, ['reserves': IncludeType.NOTINCLUDED]))
        return contract
    }

    void wire() {
        super.wire()
        replicateOutChannels this, 'outContractFinancials'
    }

}