package org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.limit;

import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObject;
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class EventAalLimitStrategy extends AbstractParameterObject implements ILimitStrategy {

    private double eventLimit = 0;
    private double aal = 0;

    public IParameterObjectClassifier getType() {
        return LimitStrategyType.EVENTLIMITAAL;
    }

    public Map getParameters() {
        Map<String, Double> parameters = new HashMap<String, Double>(2);
        parameters.put("eventLimit", eventLimit);
        parameters.put("aal", aal);
        return parameters;
    }

    public double getEventLimit() { return eventLimit; }
    public double getAAL() { return aal; }
}
