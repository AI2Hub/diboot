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
package com.diboot.file.controller;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.diboot.core.exception.BusinessException;
import com.diboot.core.util.S;
import com.diboot.core.util.V;
import com.diboot.core.vo.JsonResult;
import com.diboot.core.vo.Status;
import com.diboot.file.entity.UploadFile;
import com.diboot.file.excel.BaseExcelModel;
import com.diboot.file.excel.listener.ReadExcelListener;
import com.diboot.file.util.ExcelHelper;
import com.diboot.file.util.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Excel导入基类Controller
 *
 * @author Mazc@dibo.ltd
 * @version 2.0
 * @date 2020/02/20
 */
@Slf4j
public abstract class BaseExcelFileController extends BaseFileController {

    /**
     * 获取对应的ExcelDataListener
     */
    protected abstract ReadExcelListener<?> getExcelDataListener();

    /**
     * excel数据预览
     *
     * @param file        excel文件
     * @param entityClass 对应实体class
     * @param params      请求参数
     * @return
     * @throws Exception
     */
    public JsonResult<Map<String, Object>> excelPreview(MultipartFile file, Class<?> entityClass, Map<String, Object> params) throws Exception {
        checkIsExcel(file);
        // 保存文件
        UploadFile uploadFile = super.saveFile(file, entityClass);
        uploadFile.setDescription(params.compute("description", (k, v) -> S.defaultIfEmpty(S.valueOf(v), "Excel预览数据")).toString());

        ReadExcelListener<?> listener = getExcelDataListener();
        listener.setUploadFileUuid(uploadFile.getUuid());
        listener.setRequestParams(params);
        // 预览
        listener.setPreview(true);
        // 读取excel
        readExcelFile(file.getInputStream(), uploadFile.getFileType(), listener);

        uploadFile.setDataCount(listener.getTotalCount());
        // 保存文件上传记录
        uploadFileService.createEntity(uploadFile);

        Map<String, Object> dataMap = new HashMap<>(8);
        dataMap.put("uuid", uploadFile.getUuid());
        dataMap.put("tableHead", listener.getTableHead());
        dataMap.put("dataList", listener.getPreviewDataList());
        dataMap.put("totalCount", listener.getTotalCount());
        if (listener.getErrorCount() > 0) {
            dataMap.put("errorCount", listener.getErrorCount());
            dataMap.put("errorMsgs", listener.getErrorMsgs());
        }
        return JsonResult.OK(dataMap);
    }

    /**
     * 预览后提交保存
     *
     * @param params 请求参数；必需包含预览返回的文件uuid
     * @return
     * @throws Exception
     */
    public JsonResult<Map<String, Object>> excelPreviewSave(Map<String, Object> params) throws Exception {
        String uuid = params.get("uuid").toString();
        if (V.isEmpty(uuid)) {
            throw new BusinessException("未知的预览保存");
        }
        UploadFile uploadFile = uploadFileService.getEntity(uuid);
        uploadFile.setDescription(params.compute("description", (k, v) -> S.defaultIfEmpty(S.valueOf(v), "Excel预览后导入数据")).toString());
        return importData(uploadFile, fileStorageService.getFile(uploadFile.getStoragePath()), params);
    }


    /**
     * 直接上传excel
     *
     * @param file        excel文件
     * @param entityClass 对应实体class
     * @param params      请求参数
     * @return
     * @throws Exception
     */
    public JsonResult<Map<String, Object>> uploadExcelFile(MultipartFile file, Class<?> entityClass, Map<String, Object> params) throws Exception {
        checkIsExcel(file);
        // 保存文件
        UploadFile uploadFile = super.saveFile(file, entityClass);
        uploadFile.setDescription(params.compute("description", (k, v) -> S.defaultIfEmpty(S.valueOf(v), "Excel导入数据")).toString());
        uploadFileService.createEntity(uploadFile);
        return importData(uploadFile, file.getInputStream(), params);
    }

    /**
     * 导入数据
     *
     * @param uploadFile  上传文件对象
     * @param inputStream excel文件输入流
     * @param params      请求参数
     * @return
     * @throws Exception
     */
    private JsonResult<Map<String, Object>> importData(UploadFile uploadFile, InputStream inputStream,
                                                       Map<String, Object> params) throws Exception {
        ReadExcelListener<?> listener = getExcelDataListener();
        listener.setUploadFileUuid(uploadFile.getUuid());
        listener.setRequestParams(params);
        // 读excel
        readExcelFile(inputStream, uploadFile.getFileType(), listener);
        uploadFile.setDataCount(listener.getProperCount());
        uploadFileService.updateEntity(uploadFile);

        String errorDataFilePath = listener.getErrorDataFilePath();
        if (errorDataFilePath == null) {
            return JsonResult.OK();
        }
        String errorDataFileName = uploadFile.getFileName().replaceFirst("\\.\\w+$", "_错误数据.xlsx");
        UploadFile errorFile;
        if (FileHelper.isLocalStorage()) {
            String errorDataFileUidName = S.substringAfterLast(errorDataFilePath, "/");
            String errorDataFileUid = S.substringBefore(errorDataFileUidName, ".");
            errorFile = new UploadFile()
                    .setUuid(errorDataFileUid)
                    .setFileType("excel")
                    .setFileName(errorDataFileName)
                    .setStoragePath(errorDataFilePath)
                    .setAccessUrl(buildAccessUrl(errorDataFileUidName));
        } else {
            errorFile = super.saveFile(new FileInputStream(errorDataFilePath), errorDataFileName);
            FileHelper.deleteFile(errorDataFilePath);
        }
        errorFile.setDataCount(listener.getErrorCount())
                .setRelObjType(uploadFile.getRelObjType())
                .setDescription(uploadFile.getFileName() + " - 错误数据");
        // 创建异常数据记录
        uploadFileService.createEntity(errorFile);
        return JsonResult.OK(new HashMap<String, Object>() {{
            put("totalCount", listener.getTotalCount());
            put("errorUrl", errorFile.getAccessUrl());
            put("errorCount", listener.getErrorCount());
            put("errorMsgs", listener.getErrorMsgs());
        }});
    }

    /**
     * 读取excel方法
     *
     * @param inputStream
     * @param fileExt     文件扩展名（不包含`.`）
     * @param listener
     */
    protected <T extends BaseExcelModel> void readExcelFile(InputStream inputStream, String fileExt, ReadExcelListener<T> listener) throws Exception {
        try {
            ExcelHelper.read(inputStream, "csv".equalsIgnoreCase(fileExt) ? ExcelTypeEnum.CSV : null, listener, listener.getExcelModelClass());
        } catch (Exception e) {
            log.warn("解析excel文件失败", e);
            if (e instanceof BusinessException) {
                throw e;
            } else if (V.notEmpty(e.getMessage())) {
                throw new Exception(e.getMessage());
            }
            throw e;
        }
    }

    /**
     * 检查是否为合法的excel文件
     *
     * @param file
     * @throws Exception
     */
    private void checkIsExcel(MultipartFile file) {
        if (V.isEmpty(file)) {
            throw new BusinessException(Status.FAIL_INVALID_PARAM, "未获取待处理的excel文件！");
        }
        String fileName = file.getOriginalFilename();
        if (V.isEmpty(fileName) || !FileHelper.isExcel(fileName)) {
            log.debug("非Excel类型: {}", fileName);
            throw new BusinessException(Status.FAIL_VALIDATION, "请上传合法的Excel格式文件！");
        }
        if (file.isEmpty()) {
            log.debug("空文件：{}", fileName);
            throw new BusinessException(Status.FAIL_VALIDATION, "该文件无内容！");
        }
    }
}
