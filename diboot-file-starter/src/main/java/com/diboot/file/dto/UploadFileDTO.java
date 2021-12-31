/*
 * Copyright (c) 2015-2021, www.dibo.ltd (service@dibo.ltd).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.diboot.file.dto;

import com.diboot.core.binding.query.BindQuery;
import com.diboot.core.binding.query.Comparison;
import com.diboot.core.util.D;
import com.diboot.file.entity.UploadFile;
import lombok.Getter;

import java.util.Date;

/**
 * 上传文件 DTO
 *
 * @author wind
 * @version v.2.4.0
 * @date 2021/11/28
 */
@Getter
public class UploadFileDTO extends UploadFile {

    /**
     * 创建时间-起始
     */
    @BindQuery(comparison = Comparison.GE, field = "createTime")
    private Date createTimeBegin;

    /**
     * 创建时间-截止
     */
    @BindQuery(comparison = Comparison.LT, field = "createTime")
    private Date createTimeEnd;

    public UploadFile setCreatetimeBegin(Date createtimeBegin) {
        this.createTimeBegin = createtimeBegin;
        return this;
    }

    public UploadFile setCreatetimeEnd(Date createtimeEnd) {
        this.createTimeEnd = D.nextDay(createtimeEnd);
        return this;
    }
}
