package org.pillarone.riskanalytics.domain.pc.lob

import org.pillarone.riskanalytics.core.components.MultipleCalculationPhaseComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.domain.pc.aggregators.UnderwritingInfoAggregator
import org.pillarone.riskanalytics.domain.pc.claims.ClaimsFilterByOriginalOrigin
import org.pillarone.riskanalytics.domain.pc.claims.MarketClaimsMerger
import org.pillarone.riskanalytics.domain.pc.claims.MarketToLineOfBusinessClaims
import org.pillarone.riskanalytics.domain.pc.reserves.LineOfBusinessReserves
import org.pillarone.riskanalytics.domain.pc.reserves.fasttrack.ClaimDevelopmentLeanPacket
import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingFilterByOriginalOrigin
import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingLineOfBusinessComposer
import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo
import org.pillarone.riskanalytics.domain.pc.claims.Claim
import org.pillarone.riskanalytics.domain.pc.aggregators.UnderwritingInfoNetCalculator
import org.pillarone.riskanalytics.domain.pc.claims.MarketGrossNetClaimsMerger

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class ConfigurableLobWithReserves extends MultipleCalculationPhaseComposedComponent implements LobMarker {

    PacketList<UnderwritingInfo> inUnderwritingInfoGross = new PacketList(UnderwritingInfo.class);
    PacketList<UnderwritingInfo> inUnderwritingInfoCeded = new PacketList(UnderwritingInfo.class);
    PacketList<Claim> inClaimsGross = new PacketList(Claim.class);
    PacketList<Claim> inClaimsCeded = new PacketList(Claim.class);

    PacketList<Claim> outClaimsNet = new PacketList<Claim>(Claim.class);
    PacketList<Claim> outClaimsGross = new PacketList<Claim>(Claim.class);
    PacketList<Claim> outClaimsCeded = new PacketList<Claim>(Claim.class);

    // todo(sku): remove the following and related lines as soon as PMO-648 is resolved
    PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanNet = new PacketList(ClaimDevelopmentLeanPacket)
    PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanGross = new PacketList(ClaimDevelopmentLeanPacket)
    PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanCeded = new PacketList(ClaimDevelopmentLeanPacket)

    PacketList<UnderwritingInfo> outUnderwritingInfoNet = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
    PacketList<UnderwritingInfo> outUnderwritingInfoGross = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);
    PacketList<UnderwritingInfo> outUnderwritingInfoCeded = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);

    MarketToLineOfBusinessClaims subClaimsFilter;
    LineOfBusinessReserves subReservesFilter;
    UnderwritingLineOfBusinessComposer subUnderwritingFilter;

    ClaimsFilterByOriginalOrigin subClaimsFilterCeded;
    MarketGrossNetClaimsMerger subClaimsAggregator;
    UnderwritingFilterByOriginalOrigin subUnderwritingInfoFilterCeded;
    UnderwritingInfoNetCalculator subUnderwritingInfoAggregator;

    ConfigurableLobWithReserves() {
        subClaimsFilter = new MarketToLineOfBusinessClaims();
        subReservesFilter = new LineOfBusinessReserves();
        subUnderwritingFilter = new UnderwritingLineOfBusinessComposer();
        subClaimsFilterCeded = new ClaimsFilterByOriginalOrigin();
        subClaimsAggregator = new MarketGrossNetClaimsMerger();
        subUnderwritingInfoFilterCeded = new UnderwritingFilterByOriginalOrigin();
        subUnderwritingInfoAggregator = new UnderwritingInfoNetCalculator();
    }

    public void wire() {
        WiringUtils.use(WireCategory) {
            subUnderwritingInfoAggregator.inUnderwritingInfoGross = subUnderwritingFilter.outUnderwritingInfo
            subClaimsFilterCeded.inClaimsGross = subClaimsFilter.outClaims
            subClaimsFilterCeded.inClaimsGross = subReservesFilter.outClaims
            subClaimsAggregator.inClaimsGross = subClaimsFilter.outClaims
            subClaimsAggregator.inClaimsGross = subReservesFilter.outClaims
            subClaimsAggregator.inClaimsCeded = subClaimsFilterCeded.outClaims
            subUnderwritingInfoFilterCeded.inUnderwritingInfoGross = subUnderwritingFilter.outUnderwritingInfo
            subUnderwritingInfoAggregator.inUnderwritingInfoCeded = subUnderwritingInfoFilterCeded.outUnderwritingInfo
        }
        WiringUtils.use(PortReplicatorCategory) {
            subUnderwritingFilter.inUnderwritingInfo = this.inUnderwritingInfoGross
            subClaimsFilter.inClaims = this.inClaimsGross
            subReservesFilter.inClaims = this.inClaimsGross

            this.outUnderwritingInfoGross = subUnderwritingFilter.outUnderwritingInfo
            this.outClaimsGross = subClaimsFilter.outClaims
            this.outClaimsDevelopmentLeanGross = subClaimsFilter.outClaimsDevelopmentLean
            this.outClaimsGross = subReservesFilter.outClaims
            this.outClaimsDevelopmentLeanGross = subReservesFilter.outClaimsDevelopmentLean

            subUnderwritingInfoFilterCeded.inUnderwritingInfoCeded = this.inUnderwritingInfoCeded
            subClaimsFilterCeded.inClaimsCeded = this.inClaimsCeded

            this.outUnderwritingInfoCeded = subUnderwritingInfoAggregator.outUnderwritingInfoCeded
            this.outUnderwritingInfoNet = subUnderwritingInfoAggregator.outUnderwritingInfoNet
            this.outClaimsCeded = subClaimsAggregator.outClaimsCeded
            this.outClaimsDevelopmentLeanCeded = subClaimsAggregator.outClaimsDevelopmentLeanCeded
            this.outClaimsNet = subClaimsAggregator.outClaimsNet
            this.outClaimsDevelopmentLeanNet = subClaimsAggregator.outClaimsDevelopmentLeanNet
        }
    }

    public void allocateChannelsToPhases() {
        setTransmitterPhaseInput(inUnderwritingInfoGross, MultipleCalculationPhaseComposedComponent.PHASE_START);
        setTransmitterPhaseOutput(outUnderwritingInfoGross, MultipleCalculationPhaseComposedComponent.PHASE_START);
        setTransmitterPhaseInput(inClaimsGross, MultipleCalculationPhaseComposedComponent.PHASE_START);
        setTransmitterPhaseOutput(outClaimsGross, MultipleCalculationPhaseComposedComponent.PHASE_START);
        setTransmitterPhaseOutput(outClaimsDevelopmentLeanGross, MultipleCalculationPhaseComposedComponent.PHASE_START);
        setTransmitterPhaseInput(inUnderwritingInfoCeded, MultipleCalculationPhaseComposedComponent.PHASE_DO_CALCULATION);
        setTransmitterPhaseInput(inClaimsCeded, MultipleCalculationPhaseComposedComponent.PHASE_DO_CALCULATION);
        setTransmitterPhaseOutput(outUnderwritingInfoCeded, MultipleCalculationPhaseComposedComponent.PHASE_DO_CALCULATION);
        setTransmitterPhaseOutput(outUnderwritingInfoNet, MultipleCalculationPhaseComposedComponent.PHASE_DO_CALCULATION);
        setTransmitterPhaseOutput(outClaimsCeded, MultipleCalculationPhaseComposedComponent.PHASE_DO_CALCULATION);
        setTransmitterPhaseOutput(outClaimsDevelopmentLeanCeded, MultipleCalculationPhaseComposedComponent.PHASE_DO_CALCULATION);
        setTransmitterPhaseOutput(outClaimsNet, MultipleCalculationPhaseComposedComponent.PHASE_DO_CALCULATION);
        setTransmitterPhaseOutput(outClaimsDevelopmentLeanNet, MultipleCalculationPhaseComposedComponent.PHASE_DO_CALCULATION);
    }
}