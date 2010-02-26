package org.pillarone.riskanalytics.domain.pc.reinsurance.contracts;

import org.apache.commons.lang.ArrayUtils;
import org.pillarone.riskanalytics.core.packets.PacketList;
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope;
import org.pillarone.riskanalytics.domain.pc.claims.Claim;
import org.pillarone.riskanalytics.domain.pc.claims.ClaimFilterUtilities;
import org.pillarone.riskanalytics.domain.pc.claims.ClaimUtilities;
import org.pillarone.riskanalytics.domain.pc.claims.SortClaimsByFractionOfPeriod;
import org.pillarone.riskanalytics.domain.pc.constants.IncludeType;
import org.pillarone.riskanalytics.domain.pc.constants.LogicArguments;
import org.pillarone.riskanalytics.domain.pc.generators.claims.PerilMarker;
import org.pillarone.riskanalytics.domain.pc.lob.LobMarker;
import org.pillarone.riskanalytics.domain.pc.reinsurance.ReinsuranceResultWithCommissionPacket;
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.cover.*;
import org.pillarone.riskanalytics.domain.pc.reserves.IReserveMarker;
import org.pillarone.riskanalytics.domain.pc.reserves.fasttrack.ClaimDevelopmentLeanPacket;
import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingFilterUtilities;
import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfo;
import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfoUtilities;

import java.util.Collections;
import java.util.List;

/**
 *  This component filters from the incoming claims and underwriting information
 *  the packets whose line is listed in parameter parmCoveredLines and provides
 *  them in the corresponding out Packetlists.
 *  If the parameter contains no line at all, all packets are sent as is to the
 *  next component. Packets are not modified.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class MultiCoverAttributeReinsuranceContract extends ReinsuranceContract {

    private SimulationScope simulationScope;

    private ICoverAttributeStrategy parmCover = CoverAttributeStrategyType.getStrategy(
            CoverAttributeStrategyType.ALL, ArrayUtils.toMap(new Object[][]{{"reserves", IncludeType.NOTINCLUDED}}));

    /** claims whose source is a covered line         */
    private PacketList<Claim> outFilteredClaims = new PacketList<Claim>(Claim.class);
    // todo(sku): remove the following and related lines as soon as PMO-648 is resolved
    private PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanNet = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
    private PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanGross = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);
    private PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanCeded = new PacketList<ClaimDevelopmentLeanPacket>(ClaimDevelopmentLeanPacket.class);

    private PacketList<UnderwritingInfo> outFilteredUnderwritingInfo = new PacketList<UnderwritingInfo>(UnderwritingInfo.class);

    public void doCalculation() {
        if (parmContractStrategy == null) {
            throw new IllegalStateException("A contract strategy must be set");
        }
        if (parmCover == null) {
            throw new IllegalStateException("A cover attribute strategy must be set");
        }
        filterInChannels();
        // initialize contract details
        parmContractStrategy.initBookKeepingFigures(outFilteredClaims, outFilteredUnderwritingInfo);

        Collections.sort(outFilteredClaims, SortClaimsByFractionOfPeriod.getInstance());
        if (isSenderWired(getOutUncoveredClaims()) || isSenderWired(outClaimsDevelopmentLeanNet)) {
            calculateClaims(outFilteredClaims, outCoveredClaims, outUncoveredClaims, this);
        }
        else {
            calculateCededClaims(outFilteredClaims, outCoveredClaims, this);
        }

        if (isSenderWired(outNetAfterCoverUnderwritingInfo)) {
            calculateUnderwritingInfos(outFilteredUnderwritingInfo, outCoverUnderwritingInfo, outNetAfterCoverUnderwritingInfo);
        }
        else if (isSenderWired(outCoverUnderwritingInfo) || isSenderWired(outContractFinancials)) {
            calculateCededUnderwritingInfos(outFilteredUnderwritingInfo, outCoverUnderwritingInfo);
        }
        if (inClaims.size() > 0 && inClaims.get(0) instanceof ClaimDevelopmentLeanPacket) {
            for (Claim claim : outFilteredClaims) {
                outClaimsDevelopmentLeanGross.add((ClaimDevelopmentLeanPacket) claim);
            }
        }
        if (outCoveredClaims.size() > 0 && outCoveredClaims.get(0) instanceof ClaimDevelopmentLeanPacket) {
            for (Claim claim : outUncoveredClaims) {
                outClaimsDevelopmentLeanNet.add((ClaimDevelopmentLeanPacket) claim);
            }
            for (Claim claim : outCoveredClaims) {
                outClaimsDevelopmentLeanCeded.add((ClaimDevelopmentLeanPacket) claim);
            }
        }
        if (isSenderWired(outContractFinancials)) {
            ReinsuranceResultWithCommissionPacket result = new ReinsuranceResultWithCommissionPacket();
            UnderwritingInfo underwritingInfo = UnderwritingInfoUtilities.aggregate(outCoverUnderwritingInfo);
            if (underwritingInfo != null) {
                result.setCededPremium(-underwritingInfo.getPremiumWritten());
                result.setCededCommission(underwritingInfo.getCommission());
            }
            result.setCededClaim(ClaimUtilities.aggregateClaims(outCoveredClaims, this).getUltimate());
            outContractFinancials.add(result);
        }
    }

    protected void filterInChannels() {
        if (parmCover instanceof NoneCoverAttributeStrategy) {
            // leave outFiltered* lists void
        }
        else if (parmCover instanceof AllCoverAttributeStrategy) {
            outFilteredClaims.addAll(inClaims);
            outFilteredUnderwritingInfo.addAll(inUnderwritingInfo);
        }
        else {
            List<LobMarker> coveredLines = parmCover instanceof ILinesOfBusinessCoverAttributeStrategy
                    ? ((ILinesOfBusinessCoverAttributeStrategy) parmCover).getLines().getValuesAsObjects() : null;
            List<PerilMarker> coveredPerils = parmCover instanceof IPerilCoverAttributeStrategy
                    ? ((IPerilCoverAttributeStrategy) parmCover).getPerils().getValuesAsObjects() : null;
            List<IReserveMarker> coveredReserves = parmCover instanceof IReservesCoverAttributeStrategy
                    ? ((IReservesCoverAttributeStrategy) parmCover).getReserves().getValuesAsObjects() : null;
            LogicArguments connection = parmCover instanceof ICombinedCoverAttributeStrategy
                    ? ((ICombinedCoverAttributeStrategy) parmCover).getConnection() : null;
            outFilteredClaims.addAll(ClaimFilterUtilities.filterClaimsByPerilLobReserve(inClaims, coveredPerils, coveredLines, coveredReserves, connection));
            if (coveredLines == null || coveredLines.size() == 0) {
               coveredLines = ClaimFilterUtilities.getLineOfBusiness(outFilteredClaims);
            }
            outFilteredUnderwritingInfo.addAll(UnderwritingFilterUtilities.filterUnderwritingInfoByLob(inUnderwritingInfo, coveredLines));
        }
    }

    public SimulationScope getSimulationScope() {
        return simulationScope;
    }

    public void setSimulationScope(SimulationScope simulationScope) {
        this.simulationScope = simulationScope;
    }

    public PacketList<Claim> getOutFilteredClaims() {
        return outFilteredClaims;
    }

    public void setOutFilteredClaims(PacketList<Claim> outFilteredClaims) {
        this.outFilteredClaims = outFilteredClaims;
    }

    public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsDevelopmentLeanNet() {
        return outClaimsDevelopmentLeanNet;
    }

    public void setOutClaimsDevelopmentLeanNet(PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanNet) {
        this.outClaimsDevelopmentLeanNet = outClaimsDevelopmentLeanNet;
    }

    public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsDevelopmentLeanGross() {
        return outClaimsDevelopmentLeanGross;
    }

    public void setOutClaimsDevelopmentLeanGross(PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanGross) {
        this.outClaimsDevelopmentLeanGross = outClaimsDevelopmentLeanGross;
    }

    public PacketList<ClaimDevelopmentLeanPacket> getOutClaimsDevelopmentLeanCeded() {
        return outClaimsDevelopmentLeanCeded;
    }

    public void setOutClaimsDevelopmentLeanCeded(PacketList<ClaimDevelopmentLeanPacket> outClaimsDevelopmentLeanCeded) {
        this.outClaimsDevelopmentLeanCeded = outClaimsDevelopmentLeanCeded;
    }

    public PacketList<ReinsuranceResultWithCommissionPacket> getOutContractFinancials() {
        return outContractFinancials;
    }

    public void setOutContractFinancials(PacketList<ReinsuranceResultWithCommissionPacket> outContractFinancials) {
        this.outContractFinancials = outContractFinancials;
    }

    public PacketList<UnderwritingInfo> getOutFilteredUnderwritingInfo() {
        return outFilteredUnderwritingInfo;
    }

    public void setOutFilteredUnderwritingInfo(PacketList<UnderwritingInfo> outFilteredUnderwritingInfo) {
        this.outFilteredUnderwritingInfo = outFilteredUnderwritingInfo;
    }

    public ICoverAttributeStrategy getParmCover() {
        return parmCover;
    }

    public void setParmCover(ICoverAttributeStrategy parmCover) {
        this.parmCover = parmCover;
    }
}