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
package com.diboot.file.excel.listener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.read.metadata.property.ExcelReadHeadProperty;
import com.diboot.core.binding.annotation.BindDict;
import com.diboot.core.exception.BusinessException;
import com.diboot.core.util.BeanUtils;
import com.diboot.core.util.S;
import com.diboot.core.util.V;
import com.diboot.core.vo.Status;
import com.diboot.file.excel.BaseExcelModel;
import com.diboot.file.excel.annotation.DuplicateStrategy;
import com.diboot.file.excel.annotation.EmptyStrategy;
import com.diboot.file.excel.annotation.ExcelBindDict;
import com.diboot.file.excel.annotation.ExcelBindField;
import com.diboot.file.excel.cache.ExcelBindAnnoHandler;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/***
 * excel数据导入导出listener基类
 * @auther wangyl@dibo.ltd
 * @date 2019-10-9
 */
@Slf4j
public abstract class FixedHeadExcelListener<T extends BaseExcelModel> extends AnalysisEventListener<T> {
    //解析出的excel表头
    protected Map<Integer, String> headMap;
    // 字段名-列名的映射
    private LinkedHashMap<String, String> fieldHeadMap;
    //解析后的数据实体list
    private List<T> dataList = new ArrayList<>();
    //错误信息
    private List<String> validateErrorMsgs = null;
    // 注入request
    private Map<String, Object> requestParams;
    // 是否为预览模式
    private boolean preview = false;
    // 导入文件的uuid
    protected String uploadFileUuid;

    public FixedHeadExcelListener(){
    }

    public void setPreview(boolean isPrevieew){
        this.preview = isPrevieew;
    }
    public void setRequestParams(Map<String, Object> requestParams){
        this.requestParams = requestParams;
    }

    public void setUploadFileUuid(String uploadFileUuid){
        this.uploadFileUuid = uploadFileUuid;
    }

    /**
    * 当前一行数据解析成功后的操作
    **/
    @Override
    public void invoke(T data, AnalysisContext context) {
        // 绑定行号
        data.setRowIndex(context.readRowHolder().getRowIndex());
        dataList.add(data);
    }

    /**
    * 所有数据解析成功后的操作
    **/
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if(V.isEmpty(dataList)){
            return;
        }
        // 收集校验异常信息
        //表头和数据校验
        validateHeaderAndDataList();
        // 检查或转换字典和关联字段
        validateOrConvertDictAndRefField(context);
        //自定义数据校验
        additionalValidate(dataList, requestParams);
        // 提取校验结果
        dataList.stream().forEach(data->{
            if(V.notEmpty(data.getValidateError())){
                addErrorMsg(data.getRowIndex() + "行: " + data.getValidateError());
            }
        });
        // 有错误 抛出异常
        if(V.notEmpty(this.validateErrorMsgs)){
            throw new BusinessException(Status.FAIL_VALIDATION, S.join(this.validateErrorMsgs, "; "));
        }
        // 保存
        if(preview == false){
            // 保存数据
            saveData(dataList, requestParams);
        }
    }

    /**
    * 在转换异常、获取其他异常下回调并停止读取
    **/
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        int currentRowNum = context.readRowHolder().getRowIndex();
        //数据类型转化异常
        if (exception instanceof ExcelDataConvertException) {
            log.error("数据转换异常", exception);
            ExcelDataConvertException ex = (ExcelDataConvertException)exception;
            String errorMsg = null;
            if (ex.getCause() instanceof BusinessException) {
                errorMsg = currentRowNum+"行" + ex.getColumnIndex()+ "列: "+ ex.getCause().getMessage();
            }
            else{
                String type = ex.getExcelContentProperty().getField().getType().getSimpleName();
                String data = ex.getCellData().getStringValue();
                errorMsg = currentRowNum+"行" + ex.getColumnIndex()+ "列: 数据格式转换异常，'"+data+"' 非期望的数据类型["+type+"]";
            }
            addErrorMsg(errorMsg);
        }
        else{//其他异常
            log.error("出现未预知的异常：",exception);
            addErrorMsg("解析异常: "+exception.getMessage());
        }
    }

    /**
    * excel表头数据
    **/
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
        ExcelReadHeadProperty excelReadHeadProperty = context.currentReadHolder().excelReadHeadProperty();
        fieldHeadMap = new LinkedHashMap<>();
        for(Map.Entry<Integer, ExcelContentProperty> entry : excelReadHeadProperty.getContentPropertyMap().entrySet()){
            if(entry.getValue().getField().getAnnotation(ExcelProperty.class) != null){
                Head head = entry.getValue().getHead();
                String columnName = S.join(head.getHeadNameList());
                fieldHeadMap.put(head.getFieldName(), columnName);
            }
        }
    }

    /**
     * 校验表头, 校验数据实体list
     * */
    private void validateHeaderAndDataList() {
        // 校验数据是否合法
        dataList.stream().forEach(data->{
            String errMsg = V.validateBean(data);
            if(V.notEmpty(errMsg)){
                data.addValidateError(errMsg);
            }
        });
    }

    /**
     * 校验或转换字典name-value
     * @param context
     */
    protected void validateOrConvertDictAndRefField(AnalysisContext context){
        Map<String, Annotation> fieldName2BindAnnoMap = ExcelBindAnnoHandler.getField2BindAnnoMap(this.getExcelModelClass());
        if(fieldName2BindAnnoMap.isEmpty()){
            return;
        }
        for(Map.Entry<String, Annotation> entry: fieldName2BindAnnoMap.entrySet()){
            List nameList = (entry.getValue() instanceof ExcelBindField)? BeanUtils.collectToList(dataList, entry.getKey()) : null;
            Map<String, List> map = ExcelBindAnnoHandler.convertToNameValueMap(entry.getValue(), nameList);
            Class<T> tClass = BeanUtils.getTargetClass(dataList.get(0));
            Field field = BeanUtils.extractField(tClass, entry.getKey());
            boolean valueNotNull = (field.getAnnotation(NotNull.class) != null);
            for(T data : dataList){
                String name = BeanUtils.getStringProperty(data, entry.getKey());
                List valList = map.get(name);
                if(entry.getValue() instanceof ExcelBindField){
                    ExcelBindField excelBindField = (ExcelBindField)entry.getValue();
                    if(V.isEmpty(valList)){
                        if(excelBindField.empty().equals(EmptyStrategy.SET_0)){
                            // 非预览时 赋值
                            if(!preview){
                                BeanUtils.setProperty(data, excelBindField.setIdField(), 0);
                            }
                        }
                        else if(excelBindField.empty().equals(EmptyStrategy.WARN)){
                            data.addValidateError(name + " 值不存在");
                        }
                        else if(excelBindField.empty().equals(EmptyStrategy.IGNORE) && valueNotNull){
                            log.warn("非空字段 {} 不应设置 EmptyStrategy.IGNORE.", entry.getKey());
                        }
                    }
                    else if(valList.size() == 1){
                        // 非预览时 赋值
                        if(!preview){
                            BeanUtils.setProperty(data, excelBindField.setIdField(), valList.get(0));
                        }
                    }
                    else{
                        if(excelBindField.duplicate().equals(DuplicateStrategy.WARN)){
                            data.addValidateError(name + " 匹配到多个值");
                        }
                        else if(excelBindField.duplicate().equals(DuplicateStrategy.FIRST)){
                            // 非预览时 赋值
                            if(!preview){
                                BeanUtils.setProperty(data, excelBindField.setIdField(), valList.get(0));
                            }
                        }
                    }
                }
                else if(entry.getValue() instanceof ExcelBindDict || entry.getValue() instanceof BindDict){
                    if(V.isEmpty(valList)){
                        // 非空才报错
                        if(valueNotNull){
                            data.addValidateError(name + " 无匹配字典");
                        }
                    }
                    else{
                        // 非预览时 赋值
                        if(!preview){
                            BeanUtils.setProperty(data, entry.getKey(), valList.get(0));
                        }
                    }
                }
            }
        }
    }

    /**
     * 自定义数据检验方式，例：数据重复性校验等,返回校验日志信息
     **/
    protected abstract void additionalValidate(List<T> dataList, Map<String, Object> requestParams);

    /**
     * 保存数据
     */
    protected abstract void saveData(List<T> dataList, Map<String, Object> requestParams);

    /**
     * 返回表头
     * @return
     */
    public Map<Integer, String> getHeadMap(){
        return this.headMap;
    }

    /**
     * 获取fieldName-headName映射列表，按顺序
     * @return
     */
    public LinkedHashMap<String, String> getFieldHeadMap(){
        return this.fieldHeadMap;
    }

    /**
     * 获取FieldHeadMap顺序转换的list列表
     * @return
     */
    public List<Map<String, String>> getFieldHeaders(){
        if (V.isEmpty(this.fieldHeadMap)) {
            return Collections.emptyList();
        }
        List<Map<String, String>> fieldHeaders = new ArrayList<>();
        for (Map.Entry<String, String> fieldHead : this.fieldHeadMap.entrySet()) {
            fieldHeaders.add(new HashMap<String, String>(){{
                put("key", fieldHead.getKey());
                put("title", fieldHead.getValue());
            }});
        }
        return fieldHeaders;
    }

    /**
     * 返回数据
     * @return
     */
    public List<T> getDataList(){
        return dataList;
    }

    /***
     * 获取Excel映射的Model类
     * @return
     */
    public Class<T> getExcelModelClass(){
        return BeanUtils.getGenericityClass(this, 0);
    }

    /**
     * 校验错误信息
     * @return
     */
    public List<String> getErrorMsgs(){
        return this.validateErrorMsgs;
    }

    /**
     * 添加错误信息
     * @param errorMsg
     */
    private void addErrorMsg(String errorMsg){
        if(this.validateErrorMsgs == null){
            this.validateErrorMsgs = new ArrayList<>();
        }
        this.validateErrorMsgs.add(errorMsg);
    }
}
