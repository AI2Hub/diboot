package com.diboot.ai.models.ali.params;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通义千问请求消息
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/25
 */
@Getter
@Setter
@Accessors(chain = true)
public class AliMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 7080298360964364141L;
    /**
     * 角色
     */
    // system、user、assistant和tool。
    private String role;

    /**
     * 内容
     */
    private String content;

    /**
     * role为tool时不能省略
     */
    private String name;
}
