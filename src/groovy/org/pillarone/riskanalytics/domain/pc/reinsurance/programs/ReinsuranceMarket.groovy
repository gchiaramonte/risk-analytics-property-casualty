package org.pillarone.riskanalytics.domain.pc.reinsurance.programs

import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.domain.pc.claims.MarketClaimsMerger
import org.pillarone.riskanalytics.domain.pc.constants.IncludeType
import org.pillarone.riskanalytics.domain.pc.constants.ReinsuranceContractBase
import org.pillarone.riskanalytics.domain.pc.reinsurance.ReinsuranceResultWithCommissionPacket
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.MultiCompanyCoverAttributeReinsuranceContract
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.MultiCoverAttributeReinsuranceContract
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.ReinsuranceContract
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.ReinsuranceContractType
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.cover.CoverAttributeStrategyType
import org.pillarone.riskanalytics.domain.pc.underwriting.MarketUnderwritingInfoMerger

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class ReinsuranceMarket extends DynamicReinsuranceProgram {

    PacketList<ReinsuranceResultWithCommissionPacket> outContractFinancials = new PacketList<ReinsuranceResultWithCommissionPacket>(ReinsuranceResultWithCommissionPacket.class);

    public MultiCompanyCoverAttributeReinsuranceContract createDefaultSubComponent() {
        MultiCompanyCoverAttributeReinsuranceContract contract = new MultiCompanyCoverAttributeReinsuranceContract(
                parmInuringPriority: 0,
                parmContractStrategy: ReinsuranceContractType.getStrategy(ReinsuranceContractType.TRIVIAL, [:]),
                parmCover: CoverAttributeStrategyType.getStrategy(
                            CoverAttributeStrategyType.ALL, ['reserves': IncludeType.NOTINCLUDED]))
        return contract
    }

    void wire() {
        super.wire()
        replicateOutChannels this, 'outContractFinancials'
    }

    protected void wireContractInClaims(ReinsuranceContract contract, MarketClaimsMerger claimsMerger) {
        if (((MultiCoverAttributeReinsuranceContract) contract).parmBasedOn.equals(ReinsuranceContractBase.NET)) {
            doWire WireCategory, contract, 'inClaims', claimsMerger, 'outClaimsNet'
        }
        else if (((MultiCoverAttributeReinsuranceContract) contract).parmBasedOn.equals(ReinsuranceContractBase.CEDED)) {
            doWire WireCategory, contract, 'inClaims', claimsMerger, 'outClaimsCeded'
        }
    }

    protected void wireContractInUnderwritingInfo(ReinsuranceContract contract, MarketUnderwritingInfoMerger underwritingInfoMerger) {
        if (((MultiCoverAttributeReinsuranceContract) contract).parmBasedOn.equals(ReinsuranceContractBase.NET)) {
            doWire WireCategory, contract, 'inUnderwritingInfo', underwritingInfoMerger, 'outUnderwritingInfoNet'
        }
        else if (((MultiCoverAttributeReinsuranceContract) contract).parmBasedOn.equals(ReinsuranceContractBase.CEDED)) {
            doWire WireCategory, contract, 'inUnderwritingInfo', underwritingInfoMerger, 'outUnderwritingInfoCeded'
        }
    }
}