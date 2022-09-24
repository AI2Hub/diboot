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
package diboot.core.test.binder.vo;

import com.diboot.core.binding.annotation.BindEntity;
import diboot.core.test.binder.entity.Department;
import diboot.core.test.binder.entity.Organization;
import diboot.core.test.binder.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author mazc@dibo.ltd
 * @version v2.0
 * @date 2019/1/30
 */
@Getter
@Setter
@Accessors(chain = true)
public class EntityBinderVO extends User {
    private static final long serialVersionUID = 3526115343377985725L;

    // 实体关联，支持附加条件
    @BindEntity(entity= Department.class, condition="this.department_id=id AND name like '发'") // AND is_deleted=1
    private Department department;

    // 通过中间表关联Entity
    @BindEntity(entity = Organization.class, condition = "this.department_id=department.id AND department.org_id=id AND parent_id=0") // AND ...
    private OrganizationVO organizationVO;

    // 实体关联
    @BindEntity(entity= Department.class, condition="this.department_id=id") // AND is_deleted=1
    private Department department2;

}