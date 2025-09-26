package org.im.semiconductor.quality.core;

import org.im.exception.type.eqp.SemiconductorException;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */


public interface QualityControl {
    void checkQuality() throws SemiconductorException.QualityException;

    QualityReport generateReport();
}