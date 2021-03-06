package org.pillarone.riskanalytics.domain.pc.claims.allocation

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.packets.PacketList
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.TableMultiDimensionalParameter
import org.pillarone.riskanalytics.domain.pc.allocators.AllocationTable
import org.pillarone.riskanalytics.domain.pc.constants.RiskBandAllocationBase
import org.pillarone.riskanalytics.domain.pc.underwriting.UnderwritingInfoWithLossProbability
import org.pillarone.riskanalytics.domain.utils.InputFormatConverter

/**
 * @author: Michael-Noe (at) Web (dot) de
 */
@CompileStatic
class RiskBandsWithLossProbability extends RiskBands {
    static Map<RiskBandAllocationBase, String> singleAllocationBaseColumnName = [
        (RiskBandAllocationBase.PREMIUM): 'premium',
        (RiskBandAllocationBase.NUMBER_OF_POLICIES): 'number of policies/risks',
        (RiskBandAllocationBase.LOSS_PROBABILITY): 'loss probability',
        (RiskBandAllocationBase.CUSTOM): 'custom allocation number of single claims'
    ]

    static Map<RiskBandAllocationBase, String> attritionalAllocationBaseColumnName = [
        (RiskBandAllocationBase.PREMIUM): 'premium',
        (RiskBandAllocationBase.NUMBER_OF_POLICIES): 'number of policies/risks',
        (RiskBandAllocationBase.CUSTOM): 'custom allocation attritional claims'
    ]

    TableMultiDimensionalParameter parmUnderwritingInformation = new TableMultiDimensionalParameter([],
        ['maximum sum insured',
            'average sum insured',
            singleAllocationBaseColumnName.get(RiskBandAllocationBase.PREMIUM),
            singleAllocationBaseColumnName.get(RiskBandAllocationBase.NUMBER_OF_POLICIES),
            singleAllocationBaseColumnName.get(RiskBandAllocationBase.LOSS_PROBABILITY),
            singleAllocationBaseColumnName.get(RiskBandAllocationBase.CUSTOM),
            attritionalAllocationBaseColumnName.get(RiskBandAllocationBase.CUSTOM)]
    )

    RiskBandAllocationBase parmAllocationBaseAttritionalClaims = RiskBandAllocationBase.PREMIUM
    RiskBandAllocationBase parmAllocationBaseSingleClaims = RiskBandAllocationBase.NUMBER_OF_POLICIES

    PacketList<UnderwritingInfoWithLossProbability> outUnderwritingInfoWithLossProbability = new PacketList(UnderwritingInfoWithLossProbability)
    PacketList<AllocationTable> outSingleTargetDistribution = new PacketList(AllocationTable)
    PacketList<AllocationTable> outAttritionalTargetDistribution = new PacketList(AllocationTable)


    public void doCalculation() {
        // todo (sku): it is not necessary to execute this code for every iteration, once per period would
        //             be sufficient
        for (int i = 1; i < parmUnderwritingInformation.rowCount; i++) {
            UnderwritingInfoWithLossProbability underwritingInfoWLP = new UnderwritingInfoWithLossProbability()
            // todo : safer column selection
            // todo (mno): Look at the parameters which can be treated by the class RiskBands
            underwritingInfoWLP.premium = (Double) parmUnderwritingInformation.getValueAt(i, 2)
            underwritingInfoWLP.maxSumInsured = (Double) parmUnderwritingInformation.getValueAt(i, 0)
            underwritingInfoWLP.sumInsured = (Double) parmUnderwritingInformation.getValueAt(i, 1)
            underwritingInfoWLP.lossProbability = (Double) parmUnderwritingInformation.getValueAt(i, 4)
            underwritingInfoWLP.numberOfPolicies = (Double) parmUnderwritingInformation.getValueAt(i, 3)
            underwritingInfoWLP.origin = this
            underwritingInfoWLP.originalUnderwritingInfo = underwritingInfoWLP
            outUnderwritingInfoWithLossProbability << underwritingInfoWLP
        }

        int index = parmUnderwritingInformation.getColumnIndex('maximum sum insured')
        List<Double> sumInsuredList = new ArrayList<Double>()
        for (int i = 1; i < parmUnderwritingInformation.getRowCount(); i++) {
            sumInsuredList.add(InputFormatConverter.getDouble(parmUnderwritingInformation.getValueAt(i, index)))
        }
        index = parmUnderwritingInformation.getColumnIndex(singleAllocationBaseColumnName.get(parmAllocationBaseSingleClaims))
        List<Double> portionList = new ArrayList<Double>()
        for (int i = 1; i < parmUnderwritingInformation.getRowCount(); i++) {
            portionList.add(InputFormatConverter.getDouble(parmUnderwritingInformation.getValueAt(i, index)))
        }
        AbstractMultiDimensionalParameter singleAllocationTable = new TableMultiDimensionalParameter(
                [sumInsuredList, portionList],
                ['maximum sum insured', 'portion']
        )
        outSingleTargetDistribution << new AllocationTable(table: singleAllocationTable)

        index = parmUnderwritingInformation.getColumnIndex(attritionalAllocationBaseColumnName.get(parmAllocationBaseAttritionalClaims))
        portionList = new ArrayList<Double>()
        for (int i = 1; i < parmUnderwritingInformation.getRowCount(); i++) {
            portionList.add(InputFormatConverter.getDouble(parmUnderwritingInformation.getValueAt(i, index)))
        }
        AbstractMultiDimensionalParameter attritionalAllocationTable = new TableMultiDimensionalParameter(
                [sumInsuredList, portionList],
                ['maximum sum insured', 'portion']
        )
        outAttritionalTargetDistribution << new AllocationTable(table: attritionalAllocationTable)
    }

}