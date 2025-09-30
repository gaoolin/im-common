package org.im.semiconductor.common.parameter.core;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/29
 */

public class DefaultParameterInspection extends AbstractParameterInspection {
    @Override
    public String toString() {
        return "ParameterInspection{" +
                "differences=" + differences.size() +
                ", emptyInActual=" + emptyInActual.size() +
                ", emptyInStandard=" + emptyInStandard.size() + '}';
    }
}

