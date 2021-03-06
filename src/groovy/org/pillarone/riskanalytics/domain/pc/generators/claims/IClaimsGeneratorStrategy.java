package org.pillarone.riskanalytics.domain.pc.generators.claims;

import org.pillarone.riskanalytics.core.parameterization.IParameterObject;
import org.pillarone.riskanalytics.domain.pc.constants.Exposure;
import org.pillarone.riskanalytics.domain.utils.DistributionModified;
import org.pillarone.riskanalytics.domain.utils.RandomDistribution;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public interface IClaimsGeneratorStrategy extends IParameterObject {
    Exposure getClaimsSizeBase();

    RandomDistribution getClaimsSizeDistribution();

    DistributionModified getClaimsSizeModification();
}