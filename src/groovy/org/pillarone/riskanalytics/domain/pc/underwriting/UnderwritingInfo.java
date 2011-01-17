package org.pillarone.riskanalytics.domain.pc.underwriting;

import org.pillarone.riskanalytics.core.packets.MultiValuePacket;
import org.pillarone.riskanalytics.domain.pc.constants.Exposure;
import org.pillarone.riskanalytics.domain.pc.constants.FrequencyBase;
import org.pillarone.riskanalytics.domain.pc.lob.LobMarker;
import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.IReinsuranceContractMarker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class UnderwritingInfo extends MultiValuePacket {

    private UnderwritingInfo originalUnderwritingInfo;

    private double premium;
    private double fixedPremium;
    private double variablePremium;

    private double commission;
    private double fixedCommission;
    private double variableCommission;

    private double numberOfPolicies;
    private double sumInsured;
    private double maxSumInsured;

    private Exposure exposureDefinition;
    private LobMarker lineOfBusiness;
    private IReinsuranceContractMarker reinsuranceContract;

    private static final String PREMIUM = "premium";
    private static final String PREMIUM_FIXED = "fixed premium";
    private static final String PREMIUM_VARIABLE = "variable premium";
    private static final String COMMISSION = "commission";
    private static final String COMMISSION_FIXED = "fixed commission";
    private static final String COMMISSION_VARIABLE = "variable commission";

    public UnderwritingInfo() {
        super();
    }

    @Override
    public UnderwritingInfo copy() {
        UnderwritingInfo copy = UnderwritingInfoPacketFactory.createPacket();
        copy.set(this);
        return copy;
    }

    public void set(UnderwritingInfo underwritingInfo) {
        setOrigin(underwritingInfo.getOrigin());
        exposureDefinition = underwritingInfo.exposureDefinition;
        numberOfPolicies = underwritingInfo.numberOfPolicies;
        sumInsured = underwritingInfo.sumInsured;
        maxSumInsured = underwritingInfo.maxSumInsured;
        originalUnderwritingInfo = underwritingInfo.originalUnderwritingInfo;
        premium = underwritingInfo.premium;
        fixedPremium = underwritingInfo.fixedPremium;
        variablePremium = underwritingInfo.variablePremium;
        commission = underwritingInfo.commission;
        fixedCommission = underwritingInfo.fixedCommission;
        variableCommission = underwritingInfo.variableCommission;
        lineOfBusiness = underwritingInfo.getLineOfBusiness();
        reinsuranceContract = underwritingInfo.getReinsuranceContract();
    }

    /**
     * Warning: if the number or names of values is modified, UnderwritingBatchInsertDBCollector
     * has to be corrected accordingly.
     *
     * @return
     * @throws IllegalAccessException
     */
    @Override
    public Map<String, Number> getValuesToSave() throws IllegalAccessException {
        Map<String, Number> map = new HashMap<String, Number>();
        map.put(PREMIUM, premium);
        map.put(PREMIUM_FIXED, fixedPremium);
        map.put(PREMIUM_VARIABLE, variablePremium);
        map.put(COMMISSION, commission);
        map.put(COMMISSION_FIXED, fixedCommission);
        map.put(COMMISSION_VARIABLE, variableCommission);
        return map;
    }

    @Override
    public List<String> getFieldNames() {
        return Arrays.asList(PREMIUM, PREMIUM_FIXED, PREMIUM_VARIABLE, COMMISSION, COMMISSION_FIXED, COMMISSION_VARIABLE);
    }

    public double scaleValue(Exposure base) {
        switch (base) {
            case ABSOLUTE:
                return 1d;
            case PREMIUM_WRITTEN:
                return getPremium();
            case NUMBER_OF_POLICIES:
                return getNumberOfPolicies();
        }
        return 0;
    }

    public double scaleValue(FrequencyBase base) {
        switch (base) {
            case ABSOLUTE:
                return 1d;
            case NUMBER_OF_POLICIES:
                return getNumberOfPolicies();
        }
        return 1;
    }

    /**
     * Adds additive UnderwritingInfo fields (premium, commission) as well as combining ExposureInfo fields.
     * @param other
     * @return UnderwritingInfo packet with resulting fields
     */
    public UnderwritingInfo plus(UnderwritingInfo other) {
        if (other == null) return this;
        sumInsured = (numberOfPolicies * sumInsured + other.numberOfPolicies * other.sumInsured);
        numberOfPolicies += other.numberOfPolicies;
        if (numberOfPolicies > 0) {
            sumInsured = sumInsured / numberOfPolicies;
        }
        maxSumInsured = Math.max(maxSumInsured, other.maxSumInsured);
        if (exposureDefinition != other.exposureDefinition) {
            exposureDefinition = null;
        }

        premium += other.premium;
        commission += other.commission;
        fixedPremium += other.fixedPremium;
        variablePremium += other.variablePremium;
        fixedCommission += other.fixedCommission;
        variableCommission += other.variableCommission;
        return this;
    }

    public UnderwritingInfo minus(UnderwritingInfo other) {
        if (other == null) return this;
        sumInsured = numberOfPolicies * sumInsured - other.numberOfPolicies * other.sumInsured;
        numberOfPolicies -= other.numberOfPolicies;
        if (numberOfPolicies > 0) {
            sumInsured = sumInsured / numberOfPolicies;
        }
        if (exposureDefinition != null && exposureDefinition != other.exposureDefinition) {
            exposureDefinition = null;
        }

        premium -= other.premium;
        commission -= other.commission;
        fixedPremium -= other.fixedPremium;
        variablePremium -= other.variablePremium;
        fixedCommission -= other.fixedCommission;
        variableCommission -= other.variableCommission;
        if (premium == 0 && commission == 0 && sumInsured == 0) {
            numberOfPolicies = 0;
        }
        return this;
    }

    public void scale(double factor) {
        maxSumInsured *= factor;
        sumInsured *= factor;
        if (factor == 0) {
            numberOfPolicies = 0;
        }

        commission *= factor;
        premium *= factor;
        fixedPremium *= factor;
        variablePremium *= factor;
        fixedCommission *= factor;
        variableCommission *= factor;
    }

    public double getPremium() {
        return premium;
    }

    public void setPremium(double premium) {
        this.premium = premium;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public UnderwritingInfo getOriginalUnderwritingInfo() {
        return originalUnderwritingInfo;
    }

    public void setOriginalUnderwritingInfo(UnderwritingInfo originalUnderwritingInfo) {
        this.originalUnderwritingInfo = originalUnderwritingInfo;
    }

    public String toString() {
        return "premium: " + String.valueOf(premium) + ", commission: " + commission +
                ", origin: " + (origin == null ? "null" : origin.getNormalizedName()) +
                ", original " + (originalUnderwritingInfo==null ? "null" :
                                 originalUnderwritingInfo.origin==null ? "unnamed" :
                                 originalUnderwritingInfo.origin.getNormalizedName());
    }

    public LobMarker getLineOfBusiness() {
        return lineOfBusiness;
    }

    public void setLineOfBusiness(LobMarker lineOfBusiness) {
        this.lineOfBusiness = lineOfBusiness;
    }

    public IReinsuranceContractMarker getReinsuranceContract() {
        return reinsuranceContract;
    }

    public void setReinsuranceContract(IReinsuranceContractMarker reinsuranceContract) {
        this.reinsuranceContract = reinsuranceContract;
    }

    public double getFixedPremium() {
        return fixedPremium;
    }

    public void setFixedPremium(double fixedPremium) {
        this.fixedPremium = fixedPremium;
    }

    public double getVariablePremium() {
        return variablePremium;
    }

    public void setVariablePremium(double variablePremium) {
        this.variablePremium = variablePremium;
    }

    public double getFixedCommission() {
        return fixedCommission;
    }

    public void setFixedCommission(double fixedCommission) {
        this.fixedCommission = fixedCommission;
    }

    public double getVariableCommission() {
        return variableCommission;
    }

    public void setVariableCommission(double variableCommission) {
        this.variableCommission = variableCommission;
    }

    public double getNumberOfPolicies() {
        return numberOfPolicies;
    }

    public void setNumberOfPolicies(double numberOfPolicies) {
        this.numberOfPolicies = numberOfPolicies;
    }

    public double getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(double sumInsured) {
        this.sumInsured = sumInsured;
    }

    public double getMaxSumInsured() {
        return maxSumInsured;
    }

    public void setMaxSumInsured(double maxSumInsured) {
        this.maxSumInsured = maxSumInsured;
    }

    public Exposure getExposureDefinition() {
        return exposureDefinition;
    }

    public void setExposureDefinition(Exposure exposureDefinition) {
        this.exposureDefinition = exposureDefinition;
    }
}
