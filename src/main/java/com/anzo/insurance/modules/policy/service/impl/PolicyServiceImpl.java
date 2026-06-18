package com.anzo.insurance.modules.policy.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.security.SecurityUtil;
import com.anzo.insurance.modules.policy.dto.*;
import com.anzo.insurance.modules.policy.entity.Policy;
import com.anzo.insurance.modules.policy.repository.PolicyMapper;
import com.anzo.insurance.modules.policy.service.PolicyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 保单服务实现类
 */
@Slf4j
@Service
public class PolicyServiceImpl extends ServiceImpl<PolicyMapper, Policy> implements PolicyService {

    @Resource
    private PolicyMapper policyMapper;

    @Override
    public IPage<PolicyDetailDTO> queryPolicyPage(PolicyQueryDTO queryDTO) {
        // 设置企业ID（权限控制）
        String currentEnterpriseId = SecurityUtil.getCurrentEnterpriseId();
        queryDTO.setEnterpriseId(currentEnterpriseId);
        
        // 创建查询条件
        LambdaQueryWrapper<Policy> queryWrapper = new LambdaQueryWrapper<>();
        
        // 企业权限过滤
        queryWrapper.eq(Policy::getEnterpriseId, currentEnterpriseId);
        
        // 关键字查询
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().trim().isEmpty()) {
            String keyword = queryDTO.getKeyword().trim();
            queryWrapper.and(wrapper -> 
                wrapper.like(Policy::getPolicyNo, keyword)
                      .or().like(Policy::getApplicantName, keyword)
                      .or().like(Policy::getCargoName, keyword)
            );
        }
        
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Policy::getStatus, queryDTO.getStatus());
        }
        
        // 保险公司筛选
        if (queryDTO.getInsurerId() != null) {
            queryWrapper.eq(Policy::getInsurerId, queryDTO.getInsurerId());
        }
        
        // 贸易方向筛选
        if (queryDTO.getTradeDirection() != null) {
            queryWrapper.eq(Policy::getTradeDirection, queryDTO.getTradeDirection());
        }
        
        // 运输方式筛选
        if (queryDTO.getTransportMode() != null) {
            queryWrapper.eq(Policy::getTransportMode, queryDTO.getTransportMode());
        }
        
        // 投保日期筛选
        if (queryDTO.getApplicationDateStart() != null) {
            queryWrapper.ge(Policy::getApplicationDate, queryDTO.getApplicationDateStart());
        }
        if (queryDTO.getApplicationDateEnd() != null) {
            queryWrapper.le(Policy::getApplicationDate, queryDTO.getApplicationDateEnd());
        }
        
        // 保单生效日期筛选
        if (queryDTO.getEffectiveDateStart() != null) {
            queryWrapper.ge(Policy::getEffectiveDate, queryDTO.getEffectiveDateStart());
        }
        if (queryDTO.getEffectiveDateEnd() != null) {
            queryWrapper.le(Policy::getEffectiveDate, queryDTO.getEffectiveDateEnd());
        }
        
        // 排序
        if ("asc".equalsIgnoreCase(queryDTO.getOrderDirection())) {
            queryWrapper.orderByAsc(queryDTO.getOrderBy());
        } else {
            queryWrapper.orderByDesc(queryDTO.getOrderBy());
        }
        
        // 执行分页查询
        Page<Policy> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<Policy> policyPage = policyMapper.selectPage(page, queryWrapper);
        
        // 转换为DTO
        return policyPage.convert(this::convertToDetailDTO);
    }

    @Override
    public PolicyDetailDTO getPolicyDetail(String id) {
        Policy policy = policyMapper.selectById(id);
        if (policy == null) {
            throw new BusinessException("保单不存在");
        }
        
        // 权限检查：只能查看自己企业的保单
        String currentEnterpriseId = SecurityUtil.getCurrentEnterpriseId();
        if (!policy.getEnterpriseId().equals(currentEnterpriseId)) {
            throw new BusinessException("无权查看该保单");
        }
        
        PolicyDetailDTO detailDTO = convertToDetailDTO(policy);
        
        // 设置操作权限
        setOperationPermissions(detailDTO);
        
        return detailDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePolicy(PolicyUpdateDTO updateDTO) {
        Policy policy = policyMapper.selectById(updateDTO.getId());
        if (policy == null) {
            throw new BusinessException("保单不存在");
        }
        
        // 权限检查
        String currentEnterpriseId = SecurityUtil.getCurrentEnterpriseId();
        if (!policy.getEnterpriseId().equals(currentEnterpriseId)) {
            throw new BusinessException("无权修改该保单");
        }
        
        // 检查状态是否允许修改
        if (!isStatusAllowModify(policy.getStatus())) {
            throw new BusinessException("当前状态的保单不允许修改");
        }
        
        // 更新允许修改的字段
        if (updateDTO.getDepartureDate() != null) {
            policy.setDepartureDate(updateDTO.getDepartureDate());
        }
        if (updateDTO.getExpectedArrivalDate() != null) {
            policy.setExpectedArrivalDate(updateDTO.getExpectedArrivalDate());
        }
        if (updateDTO.getTransportVehicleInfo() != null) {
            policy.setTransportVehicleInfo(updateDTO.getTransportVehicleInfo());
        }
        if (updateDTO.getDocumentNo() != null) {
            policy.setDocumentNo(updateDTO.getDocumentNo());
        }
        if (updateDTO.getPackingQuantity() != null) {
            policy.setPackingQuantity(updateDTO.getPackingQuantity());
        }
        if (updateDTO.getShippingMark() != null) {
            policy.setShippingMark(updateDTO.getShippingMark());
        }
        if (updateDTO.getInsuredAmount() != null) {
            policy.setInsuredAmount(updateDTO.getInsuredAmount());
        }
        if (updateDTO.getInvoiceAmount() != null) {
            policy.setInvoiceAmount(updateDTO.getInvoiceAmount());
        }
        if (updateDTO.getSpecialAgreements() != null) {
            policy.setSpecialAgreements(updateDTO.getSpecialAgreements());
        }
        if (updateDTO.getRemark() != null) {
            policy.setRemark(updateDTO.getRemark());
        }
        
        policy.setUpdateUserName(SecurityUtil.getCurrentUsername());
        
        // 如果状态是已提交，修改后状态改为待审核
        if (policy.getStatus() == 0) { // 已提交
            policy.setStatus(1); // 改为待审核
        }
        
        policyMapper.updateById(policy);
        
        // TODO: 记录修改日志
        log.info("保单修改成功，保单ID：{}, 修改人：{}", policy.getId(), SecurityUtil.getCurrentUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelPolicy(PolicyCancelDTO cancelDTO) {
        Policy policy = policyMapper.selectById(cancelDTO.getId());
        if (policy == null) {
            throw new BusinessException("保单不存在");
        }
        
        // 权限检查
        String currentEnterpriseId = SecurityUtil.getCurrentEnterpriseId();
        if (!policy.getEnterpriseId().equals(currentEnterpriseId)) {
            throw new BusinessException("无权操作该保单");
        }
        
        // 根据操作类型执行不同逻辑
        if ("cancel".equals(cancelDTO.getOperationType())) {
            // 撤销操作
            if (!isStatusAllowCancel(policy.getStatus())) {
                throw new BusinessException("当前状态的保单不允许撤销");
            }
            
            policy.setStatus(5); // 已撤销
            policy.setCancellationReason(cancelDTO.getReason());
            policy.setCancellationTime(LocalDateTime.now());
            policy.setUpdateUserName(SecurityUtil.getCurrentUsername());
            
        } else if ("surrender".equals(cancelDTO.getOperationType())) {
            // 退保操作
            if (!isStatusAllowSurrender(policy.getStatus())) {
                throw new BusinessException("当前状态的保单不允许退保");
            }
            
            policy.setStatus(6); // 已退保
            policy.setSurrenderReason(cancelDTO.getReason());
            policy.setSurrenderTime(LocalDateTime.now());
            policy.setUpdateUserName(SecurityUtil.getCurrentUsername());
            
            // TODO: 计算退费金额
            // policy.setRefundAmount(calculateRefundAmount(policy));
        } else {
            throw new BusinessException("不支持的操作类型");
        }
        
        policyMapper.updateById(policy);
        
        // TODO: 记录操作日志
        log.info("保单{}成功，保单ID：{}, 操作人：{}", 
                "cancel".equals(cancelDTO.getOperationType()) ? "撤销" : "退保",
                policy.getId(), SecurityUtil.getCurrentUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchOperatePolicies(BatchOperationDTO batchOperationDTO) {
        List<String> policyIds = batchOperationDTO.getPolicyIds();
        if (policyIds == null || policyIds.isEmpty()) {
            throw new BusinessException("请选择要操作的保单");
        }
        
        // 获取当前企业ID
        String currentEnterpriseId = SecurityUtil.getCurrentEnterpriseId();
        
        // 批量查询保单
        List<Policy> policies = policyMapper.selectBatchIds(policyIds);
        
        // 检查权限和状态
        for (Policy policy : policies) {
            if (!policy.getEnterpriseId().equals(currentEnterpriseId)) {
                throw new BusinessException("无权操作保单：" + policy.getPolicyNo());
            }
        }
        
        // 根据操作类型执行批量操作
        if ("cancel".equals(batchOperationDTO.getOperationType())) {
            // 批量撤销
            policies.forEach(policy -> {
                if (isStatusAllowCancel(policy.getStatus())) {
                    policy.setStatus(5); // 已撤销
                    policy.setCancellationReason(batchOperationDTO.getCancelReason());
                    policy.setCancellationTime(LocalDateTime.now());
                    policy.setUpdateUserName(SecurityUtil.getCurrentUsername());
                    policyMapper.updateById(policy);
                }
            });
        } else if ("export".equals(batchOperationDTO.getOperationType())) {
            // 批量导出 - 这里只记录日志，实际导出由Controller处理
            log.info("批量导出保单，数量：{}", policies.size());
        } else if ("download".equals(batchOperationDTO.getOperationType())) {
            // 批量下载 - 生成打包文件下载链接
            // TODO: 实现批量下载功能
            log.info("批量下载保单，数量：{}", policies.size());
        }
    }

    @Override
    public String downloadPolicyFile(String id) {
        Policy policy = policyMapper.selectById(id);
        if (policy == null) {
            throw new BusinessException("保单不存在");
        }
        
        // 权限检查
        String currentEnterpriseId = SecurityUtil.getCurrentEnterpriseId();
        if (!policy.getEnterpriseId().equals(currentEnterpriseId)) {
            throw new BusinessException("无权下载该保单");
        }
        
        // 检查是否有保单文件
        if (policy.getPolicyFileUrl() == null || policy.getPolicyFileUrl().isEmpty()) {
            throw new BusinessException("保单文件不存在");
        }
        
        // TODO: 记录下载日志
        log.info("保单文件下载，保单ID：{}, 下载人：{}", policy.getId(), SecurityUtil.getCurrentUsername());
        
        return policy.getPolicyFileUrl();
    }

    @Override
    public void generatePolicy(String applicationId) {
        // TODO: 根据投保申请生成保单
        // 需要调用保险公司的接口生成正式保单
        // 更新保单状态为已承保
    }

    @Override
    public void updatePolicyStatus(String id, Integer status, String remark) {
        // TODO: 更新保单状态（用于保司回调等场景）
    }

    /**
     * 将Policy转换为PolicyDetailDTO
     */
    private PolicyDetailDTO convertToDetailDTO(Policy policy) {
        PolicyDetailDTO detailDTO = new PolicyDetailDTO();
        BeanUtils.copyProperties(policy, detailDTO);
        
        // 设置中文名称
        detailDTO.setStatusName(policy.getStatusName());
        detailDTO.setTradeDirectionName(policy.getTradeDirectionName());
        detailDTO.setTransportModeName(policy.getTransportModeName());
        
        return detailDTO;
    }

    /**
     * 设置操作权限
     */
    private void setOperationPermissions(PolicyDetailDTO detailDTO) {
        detailDTO.setAllowModify(isStatusAllowModify(detailDTO.getStatus()));
        detailDTO.setAllowCancel(isStatusAllowCancel(detailDTO.getStatus()));
        detailDTO.setAllowDownload(detailDTO.getStatus() >= 3 && detailDTO.getPolicyFileUrl() != null); // 已承保及以上且有文件可下载
        detailDTO.setAllowSurrender(isStatusAllowSurrender(detailDTO.getStatus()));
        detailDTO.setAllowRenew(isStatusAllowRenew(detailDTO.getStatus()));
    }

    /**
     * 检查状态是否允许修改
     */
    private boolean isStatusAllowModify(Integer status) {
        // 已提交、待审核、保司审核中的保单允许修改
        return status != null && (status == 0 || status == 1 || status == 2);
    }

    /**
     * 检查状态是否允许撤销
     */
    private boolean isStatusAllowCancel(Integer status) {
        // 已提交、待审核、保司审核中、已承保的保单允许撤销
        return status != null && (status == 0 || status == 1 || status == 2 || status == 3);
    }

    /**
     * 检查状态是否允许退保
     */
    private boolean isStatusAllowSurrender(Integer status) {
        // 已生效的保单允许退保
        return status != null && status == 4;
    }

    /**
     * 检查状态是否允许续保
     */
    private boolean isStatusAllowRenew(Integer status) {
        // 已生效或已过期的保单允许续保
        return status != null && (status == 4 || status == 7);
    }
}