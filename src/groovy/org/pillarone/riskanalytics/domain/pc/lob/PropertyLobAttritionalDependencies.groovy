package org.pillarone.riskanalytics.domain.pc.lob

import org.pillarone.riskanalytics.core.components.ComposedComponent
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.wiring.PortReplicatorCategory
import org.pillarone.riskanalytics.core.wiring.WireCategory
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.domain.pc.generators.claims.AttritionalSingleEQFloodStormClaimsGenerator
import org.pillarone.riskanalytics.domain.pc.generators.copulas.DependenceStream
import org.pillarone.riskanalytics.domain.pc.reinsurance.programs.ReinsuranceProgram3SerialContracts
import org.pillarone.riskanalytics.domain.pc.severities.ProbabilityExtractor
import org.pillarone.riskanalytics.domain.pc.claims.allocation.RiskBands
import org.pillarone.riskanalytics.domain.pc.claims.allocation.RiskAllocator
import org.pillarone.riskanalytics.core.components.ComponentCategory
import org.pillarone.riskanalytics.domain.utils.marker.ISegmentMarker

/**
 *  This example line of business contains an underwriting, claims generator and a
 *  reinsurance program. The later with a fixed number of three serial contracts.
 *  Furthermore several aggregators are included for the collection and aggregation of packets.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
@ComponentCategory(categories = ['SEGMENT'])
class PropertyLobAttritionalDependencies extends ComposedComponent implements ISegmentMarker {

    PacketList<DependenceStream> inProbabilities = new PacketList(DependenceStream)

    ProbabilityExtractor subAttritionalSeverityExtractor = new ProbabilityExtractor()
    RiskBands subUnderwriting = new RiskBands()
    AttritionalSingleEQFloodStormClaimsGenerator subClaimsGenerator = new AttritionalSingleEQFloodStormClaimsGenerator()
    RiskAllocator subAllocator = new RiskAllocator()
    ReinsuranceProgram3SerialContracts subRiProgram = new ReinsuranceProgram3SerialContracts()

    public void wire() {
        if (isReceiverWired(inProbabilities)) {
            WiringUtils.use(PortReplicatorCategory) {
                subAttritionalSeverityExtractor.inProbabilities = this.inProbabilities
            }
        }
        WiringUtils.use(WireCategory) {
            subClaimsGenerator.inProbabilities = subAttritionalSeverityExtractor.outProbabilities
            subClaimsGenerator.inUnderwritingInfo = subUnderwriting.outUnderwritingInfo
            subAllocator.inUnderwritingInfo = subUnderwriting.outUnderwritingInfo
            subAllocator.inTargetDistribution = subUnderwriting.outAttritionalTargetDistribution

            subRiProgram.inUnderwritingInfo = subAllocator.outUnderwritingInfo
            subAllocator.inClaims = subClaimsGenerator.outClaims
            subRiProgram.inClaims = subAllocator.outClaims
        }
    }
}