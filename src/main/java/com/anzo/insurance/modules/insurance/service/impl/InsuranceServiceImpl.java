package com.anzo.insurance.modules.insurance.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.modules.insurance.dto.*;
import com.anzo.insurance.modules.insurance.entity.InsuranceApplication;
import com.anzo.insurance.modules.insurance.repository.InsuranceApplicationMapper;
import com.anzo.insurance.modules.insurance.service.InsuranceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 投保业务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl extends ServiceImpl<InsuranceApplicationMapper, InsuranceApplication> implements InsuranceService {
    
    private final InsuranceApplicationMapper insuranceApplicationMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InsuranceApplication saveStep1(ApplicationStep1DTO dto) {
        String enterpriseId = getCurrentEnterpriseId();
        
        InsuranceApplication application;
        if (dto.getApplicationId() != null) {
            application = getById(dto.getApplicationId());
            if (application == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保申请不存在");
            }
            if (!application.getEnterpriseId().equals(enterpriseId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作");
            }
        } else {
            application = new InsuranceApplication();
            application.setApplicationNo(generateApplicationNo());
            application.setEnterpriseId(enterpriseId);
            application.setStatus(InsuranceApplication.Status.DRAFT.getValue());
        }
        
        application.setTradeDirection(dto.getTradeDirection());
        application.setTransportType(dto.getTransportType());
        application.setInsuranceProduct(dto.getInsuranceProduct());
        application.setInsurerId(dto.getInsurerId());
        application.setInsurerName(dto.getInsurerName());
        application.setApplicantId(dto.getApplicantId());
        application.setInsuredId(dto.getInsuredId());
        
        saveOrUpdate(application);
        
        log.info("投保步骤1保存成功，申请ID：{}，企业ID：{}", application.getId(), enterpriseId);
        return application;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InsuranceApplication saveStep2(ApplicationStep2DTO dto) {
        InsuranceApplication application = getById(dto.getApplicationId());
        if (application == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保申请不存在");
        }
        
        String enterpriseId = getCurrentEnterpriseId();
        if (!application.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作");
        }
        
        application.setDepartureCountry(dto.getDepartureCountry());
        application.setDepartureCity(dto.getDepartureCity());
        application.setArrivalCountry(dto.getArrivalCountry());
        application.setArrivalCity(dto.getArrivalCity());
        application.setDepartureDate(dto.getDepartureDate());
        application.setArrivalDate(dto.getArrivalDate());
        application.setTransportDetailsJson(dto.getTransportDetailsJson());
        
        updateById(application);
        
        log.info("投保步骤2保存成功，申请ID：{}", application.getId());
        return application;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InsuranceApplication saveStep3(ApplicationStep3DTO dto) {
        InsuranceApplication application = getById(dto.getApplicationId());
        if (application == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保申请不存在");
        }
        
        String enterpriseId = getCurrentEnterpriseId();
        if (!application.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作");
        }
        
        application.setCargoName(dto.getCargoName());
        application.setCargoCategory(dto.getCargoCategory());
        application.setPackingType(dto.getPackingType());
        application.setPackingQuantity(dto.getPackingQuantity());
        application.setShippingMark(dto.getShippingMark());
        application.setCurrency(dto.getCurrency());
        application.setInsuranceAmount(dto.getInsuranceAmount());
        application.setInvoiceAmount(dto.getInvoiceAmount());
        application.setAdditionRatio(dto.getAdditionRatio());
        application.setDeductible(dto.getDeductible());
        application.setSpecialTerms(dto.getSpecialTerms());
        
        updateById(application);
        
        log.info("投保步骤3保存成功，申请ID：{}", application.getId());
        return application;
    }
    
    @Override
    public PremiumCalculateResponseDTO calculatePremium(PremiumCalculateDTO dto) {
        BigDecimal premium = calculatePremiumAmount(
            dto.getTradeDirection(),
            dto.getTransportType(),
            dto.getInsuranceProduct(),
            dto.getCargoCategory(),
            dto.getInsuranceAmount(),
            dto.getAdditionRatio(),
            dto.getInsurerId()
        );
        
        BigDecimal rate = calculateRate(
            dto.getTradeDirection(),
            dto.getTransportType(),
            dto.getInsuranceProduct(),
            dto.getCargoCategory(),
            dto.getInsurerId()
        );
        
        PremiumCalculateResponseDTO response = PremiumCalculateResponseDTO.builder()
            .premium(premium)
            .currency("CNY")
            .rate(rate)
            .rateDisplay(rate.multiply(BigDecimal.valueOf(10000)).setScale(2, RoundingMode.HALF_UP) + "‱")
            .deductible(BigDecimal.ZERO) // 默认免赔额为0
            .baseRate(rate)
            .additionalRates(new ArrayList<>())
            .additionRatio(BigDecimal.valueOf(1.10))
            .insurerName("默认保司")
            .insurerCode("DEFAULT")
            .validUntil(LocalDateTime.now().plusMinutes(30))
            .calculationId("CALC-" + System.currentTimeMillis())
            .build();
        
        if (dto.getApplicationId() != null) {
            InsuranceApplication application = getById(dto.getApplicationId());
            if (application != null) {
                application.setPremium(premium);
                application.setPremiumCurrency("CNY");
                application.setRate(rate);
                updateById(application);
            }
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InsuranceApplication submitApplication(String applicationId, SubmitApplicationDTO dto) {
        InsuranceApplication application = getById(applicationId);
        if (application == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保申请不存在");
        }
        
        String enterpriseId = getCurrentEnterpriseId();
        if (!application.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作");
        }
        
        validateApplicationComplete(application);
        
        if (application.getDepartureDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "启运日期不能早于今天");
        }
        
        if (application.getInsuranceAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "保险金额必须大于0");
        }
        
        application.setStatus(InsuranceApplication.Status.SUBMITTED.getValue());
        application.setSubmittedAt(LocalDateTime.now());
        
        updateById(application);
        
        log.info("投保申请提交成功，申请ID：{}，保单号：{}", application.getId(), application.getApplicationNo());
        
        return application;
    }
    
    @Override
    public InsuranceApplication getApplication(String id) {
        InsuranceApplication application = getById(id);
        if (application == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保申请不存在");
        }
        
        String enterpriseId = getCurrentEnterpriseId();
        if (!application.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看");
        }
        
        return application;
    }
    
    @Override
    public Page<InsuranceApplication> getApplications(ApplicationQueryDTO query) {
        String enterpriseId = getCurrentEnterpriseId();
        
        Page<InsuranceApplication> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<InsuranceApplication> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(InsuranceApplication::getEnterpriseId, enterpriseId);
        
        if (query.getStatus() != null) {
            wrapper.eq(InsuranceApplication::getStatus, query.getStatus());
        }
        
        if (query.getStartDate() != null) {
            wrapper.ge(InsuranceApplication::getCreatedAt, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.le(InsuranceApplication::getCreatedAt, query.getEndDate().atTime(23, 59, 59));
        }
        
        wrapper.orderByDesc(InsuranceApplication::getCreatedAt);
        
        return page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InsuranceApplication cancelApplication(String applicationId, String reason) {
        InsuranceApplication application = getById(applicationId);
        if (application == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保申请不存在");
        }
        
        String enterpriseId = getCurrentEnterpriseId();
        if (!application.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作");
        }
        
        String status = application.getStatus();
        if (!InsuranceApplication.Status.DRAFT.getValue().equals(status) 
            && !InsuranceApplication.Status.SUBMITTED.getValue().equals(status)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), 
                "只有草稿或已提交状态的投保申请可以撤销");
        }
        
        application.setStatus(InsuranceApplication.Status.CANCELLED.getValue());
        updateById(application);
        
        log.info("投保申请撤销成功，申请ID：{}，原因：{}", applicationId, reason);
        
        return application;
    }
    
    @Override
    public InsuranceDraftDTO saveDraft(InsuranceDraftDTO dto) {
        // TODO: 实现草稿保存逻辑
        log.info("保存投保草稿: {}", dto);
        return dto;
    }
    
    @Override
    public List<InsuranceDraftDTO> getDrafts() {
        // TODO: 实现草稿查询逻辑
        String enterpriseId = getCurrentEnterpriseId();
        log.info("获取企业草稿列表: {}", enterpriseId);
        return new ArrayList<>();
    }
    
    @Override
    public boolean deleteDraft(String draftId) {
        // TODO: 实现草稿删除逻辑
        log.info("删除投保草稿: {}", draftId);
        return true;
    }
    
    @Override
    public InsuranceTemplateDTO createTemplate(InsuranceTemplateDTO dto) {
        // TODO: 实现模板创建逻辑
        log.info("创建投保模板: {}", dto);
        return dto;
    }
    
    @Override
    public List<InsuranceTemplateDTO> getTemplates() {
        // TODO: 实现模板查询逻辑
        String enterpriseId = getCurrentEnterpriseId();
        log.info("获取企业模板列表: {}", enterpriseId);
        return new ArrayList<>();
    }
    
    @Override
    public boolean deleteTemplate(String templateId) {
        // TODO: 实现模板删除逻辑
        log.info("删除投保模板: {}", templateId);
        return true;
    }
    
    private String getCurrentEnterpriseId() {
        return "test-enterprise-id";
    }
    
    private String generateApplicationNo() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = String.format("%02d%02d%02d", 
            now.getYear() % 100, now.getMonthValue(), now.getDayOfMonth());
        String randomPart = String.format("%06d", (int)(Math.random() * 1000000));
        return "IN" + datePart + randomPart;
    }
    
    private void validateApplicationComplete(InsuranceApplication application) {
        if (application.getTradeDirection() == null 
            || application.getTransportType() == null 
            || application.getInsuranceProduct() == null 
            || application.getApplicantId() == null 
            || application.getInsuredId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "请完善基础信息");
        }
        
        if (application.getDepartureCountry() == null 
            || application.getDepartureCity() == null 
            || application.getArrivalCountry() == null 
            || application.getArrivalCity() == null 
            || application.getDepartureDate() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "请完善运输信息");
        }
        
        if (application.getCargoName() == null 
            || application.getCargoCategory() == null 
            || application.getInsuranceAmount() == null 
            || application.getCurrency() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "请完善货物信息");
        }
        
        if (application.getPremium() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "请先试算保费");
        }
    }
    
    private BigDecimal calculatePremiumAmount(String tradeDirection, String transportType, 
                                           String insuranceProduct, String cargoCategory,
                                           BigDecimal insuranceAmount, BigDecimal additionRatio,
                                           String insurerId) {
        BigDecimal baseRate = BigDecimal.valueOf(0.0025);
        
        if ("AIR".equals(transportType)) {
            baseRate = BigDecimal.valueOf(0.0035);
        } else if ("SEA".equals(transportType)) {
            baseRate = BigDecimal.valueOf(0.0018);
        }
        
        if ("电子产品".equals(cargoCategory) 
            || "精密仪器".equals(cargoCategory)
            || "易碎品".equals(cargoCategory)) {
            baseRate = baseRate.multiply(BigDecimal.valueOf(1.2));
        }
        
        if (additionRatio == null) {
            additionRatio = BigDecimal.valueOf(1.10);
        }
        BigDecimal adjustedAmount = insuranceAmount.multiply(additionRatio);
        
        BigDecimal premium = adjustedAmount.multiply(baseRate)
            .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal minPremium = BigDecimal.valueOf(50.00);
        if (premium.compareTo(minPremium) < 0) {
            premium = minPremium;
        }
        
        return premium;
    }
    
    private BigDecimal calculateRate(String tradeDirection, String transportType,
                                  String insuranceProduct, String cargoCategory,
                                  String insurerId) {
        BigDecimal baseRate = BigDecimal.valueOf(2.5);
        
        if ("AIR".equals(transportType)) {
            baseRate = BigDecimal.valueOf(3.5);
        } else if ("SEA".equals(transportType)) {
            baseRate = BigDecimal.valueOf(1.8);
        }
        
        if ("电子产品".equals(cargoCategory) 
            || "精密仪器".equals(cargoCategory)
            || "易碎品".equals(cargoCategory)) {
            baseRate = baseRate.multiply(BigDecimal.valueOf(1.2));
        }
        
        return baseRate.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
    }
}