package com.im.inspection.util.chk;

import com.im.inspection.util.response.MesR;
import com.im.inspection.util.response.ResponseCode;
import com.im.qtech.data.dto.reverse.EqpReversePOJO;

import static com.im.inspection.util.chk.IoTReverseMsgBuilder.buildResponseMessage;

/**
 * 数采反控信息转换工具
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/14 11:17:29
 */
public class MesResponseConvertor {

    public static MesR<String> doConvert(EqpReversePOJO pojo) {
        if (pojo == null) {
            return createResponse(ResponseCode.SUCCESS.getCode(), "No data found");
        }

        int code = pojo.getCode();
        String message = buildResponseMessage(pojo);

        if (code == 0) {
            return createResponse(ResponseCode.SUCCESS.getCode(), message);
        } else {
            return createResponse(ResponseCode.FOUND.getCode(), message);
        }
    }

    private static MesR<String> createResponse(int code, String msg) {
        return new MesR<String>().setCode(code).setMsg(msg).setData(null);
    }
}
