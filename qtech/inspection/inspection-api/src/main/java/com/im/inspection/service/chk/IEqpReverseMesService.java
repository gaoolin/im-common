package com.im.inspection.service.chk;

import com.im.qtech.data.dto.reverse.EqpReversePOJO;

import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/13 17:27:36
 */


public interface IEqpReverseMesService {
    public List<EqpReversePOJO> getList(EqpReversePOJO pojo);

    public EqpReversePOJO getOneBySimId(String simId);
}
