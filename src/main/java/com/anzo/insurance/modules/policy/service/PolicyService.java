package com.anzo.insurance.modules.policy.service;

import com.anzo.insurance.modules.policy.dto.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 保单服务接口
 */
public interface PolicyService {

    /**
     * 分页查询保单列表
     */
    IPage<PolicyDetailDTO> queryPolicyPage(PolicyQueryDTO queryDTO);

    /**
     * 获取保单详情
     */
    PolicyDetailDTO getPolicyDetail(String id);

    /**
     * 修改保单信息
     */
    void updatePolicy(PolicyUpdateDTO updateDTO);

    /**
     * 撤销或退保保单
     */
    void cancelPolicy(PolicyCancelDTO cancelDTO);

    /**
     * 批量操作保单
     */
    void batchOperatePolicies(BatchOperationDTO batchOperationDTO);

    /**
     * 下载保单文件
     */
    String downloadPolicyFile(String id);

    /**
     * 导出保单列表
     */
    void exportPolicies(PolicyQueryDTO queryDTO, HttpServletResponse response);

    /**
     * 生成保单
     */
    void generatePolicy(String applicationId);

    /**
     * 更新保单状态
     */
    void updatePolicyStatus(String id, Integer status, String remark);
}
