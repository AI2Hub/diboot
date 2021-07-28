/*
 * Copyright (c) 2015-2020, www.dibo.ltd (service@dibo.ltd).
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
package com.diboot.iam.exception;

import com.diboot.core.exception.BusinessException;
import com.diboot.core.vo.Status;

/**
 * 权限校验异常
 * @author mazc@dibo.ltd
 * @version v2.0
 * @date 2019/12/30
 */
public class PermissionException extends BusinessException {

    public PermissionException() {
        super(Status.FAIL_NO_PERMISSION);
    }

    public PermissionException(Status status,Throwable ex) {
        super(status,ex);
    }

    public PermissionException(Throwable ex) {
        this(Status.FAIL_NO_PERMISSION, ex);
    }

    public PermissionException(String msg) {
        super(Status.FAIL_NO_PERMISSION, msg);
    }

    public PermissionException(String msg, Throwable ex) {
        super(Status.FAIL_NO_PERMISSION, msg, ex);
    }

}
