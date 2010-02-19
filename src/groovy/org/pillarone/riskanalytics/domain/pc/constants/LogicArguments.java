package org.pillarone.riskanalytics.domain.pc.constants;

import java.util.Map;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public enum LogicArguments {
    AND, OR;

    public Object getConstructionString(Map parameters) {
        return getClass().getName() + "." + this;
    }
}
