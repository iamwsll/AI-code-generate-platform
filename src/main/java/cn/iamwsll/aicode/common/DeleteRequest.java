package cn.iamwsll.aicode.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
/**
 * 用来封装删除请求参数
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;
}
