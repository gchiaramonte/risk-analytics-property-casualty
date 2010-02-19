package org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.cover;

import org.pillarone.riskanalytics.core.parameterization.ComboBoxTableMultiDimensionalParameter;
import org.pillarone.riskanalytics.domain.pc.lob.LobMarker;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class LineOfBusinessCoverAttributeStrategy implements ICoverAttributeStrategy {

    private ComboBoxTableMultiDimensionalParameter lines
            = new ComboBoxTableMultiDimensionalParameter(Collections.emptyList(), Arrays.asList("Covered Lines"), LobMarker.class);

    public Object getType() {
        return CoverAttributeStrategyType.LINESOFBUSINESS;
    }

    public Map getParameters() {
        Map<String, ComboBoxTableMultiDimensionalParameter> parameters = new HashMap<String, ComboBoxTableMultiDimensionalParameter>(1);
        parameters.put("lines", lines);
        return parameters;
    }
}