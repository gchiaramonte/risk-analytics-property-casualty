package org.pillarone.riskanalytics.domain.pc.reinsurance.programs

import org.pillarone.riskanalytics.domain.pc.lob.LobMarker
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.MultiLineReinsuranceContract
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.ReinsuranceContractStrategyFactory
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.ReinsuranceContractType
import org.pillarone.riskanalytics.core.parameterization.ComboBoxTableMultiDimensionalParameter

/**
 * A MultiLineDynamicReinsuranceProgram is an almost identical analog to DynamicReinsuranceProgram,
 * the salient difference being that the contracts it contains are MultiLineReinsuranceContracts
 * (which know which lines, perils or reserves they cover) rather than ReinsuranceContracts
 * (which make no discrimination and cover everything they are given).
 *
 * @deprecated use DynamicMultiCoverAttributeReinsuranceProgram instead
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
@Deprecated
public class MultiLineDynamicReinsuranceProgram extends DynamicReinsuranceProgram {

    public MultiLineReinsuranceContract createDefaultSubComponent() {
        MultiLineReinsuranceContract contract = new MultiLineReinsuranceContract(
                parmInuringPriority: 0,
                parmContractStrategy: ReinsuranceContractStrategyFactory.getContractStrategy(ReinsuranceContractType.TRIVIAL, [:]),
                parmCoveredLines: new ComboBoxTableMultiDimensionalParameter([''], ['Covered Lines'], LobMarker)
        )
        return contract
    }
}