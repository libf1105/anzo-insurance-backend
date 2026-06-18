package com.anzo.insurance.modules.insurance.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.common.security.SecurityUtil;
import com.anzo.insurance.modules.customer.entity.Customer;
import com.anzo.insurance.modules.customer.repository.CustomerMapper;
import com.anzo.insurance.modules.insurance.dto.*;
import com.anzo.insurance.modules.insurance.entity.ApplicationDraft;
import com.anzo.insurance.modules.insurance.entity.ApplicationTemplate;
import com.anzo.insurance.modules.insurance.entity.InsuranceApplication;
import com.anzo.insurance.modules.insurance.repository.ApplicationDraftMapper;
import com.anzo.insurance.modules.insurance.repository.ApplicationTemplateMapper;
import com.anzo.insurance.modules.insurance.repository.InsuranceApplicationMapper;
import com.anzo.insurance.modules.insurance.service.InsuranceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 投保业务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl extends ServiceImpl<InsuranceApplicationMapper, InsuranceApplication> implements InsuranceService {
    
    private final ApplicationDraftMapper applicationDraftMapper;
    private final ApplicationTemplateMapper applicationTemplateMapper;
    private final CustomerMapper customerMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InsuranceApplication saveStep1(ApplicationStep1DTO dto) {
        String enterpriseId = getCurrentEnterpriseId();
        
        InsuranceApplication application;
        if (dto.getDraftId() != null && !dto.getDraftId().isBlank()) {
            application = getById(dto.getDraftId());
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
        application.setApplicantId(dto.getApplicantId());
        application.setInsuredId(Boolean.TRUE.equals(dto.getInsuredSameAsApplicant()) ? dto.getApplicantId() : dto.getInsuredId());
        
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
        application.setTransportDetailsJson(serializeTransportDetails(dto));
        
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
        if (Boolean.TRUE.equals(application.getDeleted())) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保申请不存在");
        }
        
        String enterpriseId = getCurrentEnterpriseId();
        if (!application.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权查看");
        }

        enrichApplication(application);
        return application;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApplication(String applicationId) {
        InsuranceApplication application = getApplication(applicationId);
        if (!InsuranceApplication.Status.DRAFT.getValue().equals(application.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "只有草稿状态的投保申请可以删除");
        }

        application.setDeleted(true);
        application.setUpdatedBy(SecurityUtil.getCurrentUserId());
        updateById(application);
    }
    
    @Override
    public Page<InsuranceApplication> getApplications(ApplicationQueryDTO query) {
        String enterpriseId = getCurrentEnterpriseId();
        
        Page<InsuranceApplication> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<InsuranceApplication> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(InsuranceApplication::getEnterpriseId, enterpriseId)
                .eq(InsuranceApplication::getDeleted, false);
        
        if (query.getStatus() != null) {
            wrapper.eq(InsuranceApplication::getStatus, query.getStatus());
        }
        if (query.getTradeDirection() != null) {
            wrapper.eq(InsuranceApplication::getTradeDirection, query.getTradeDirection());
        }
        if (query.getTransportType() != null) {
            wrapper.eq(InsuranceApplication::getTransportType, query.getTransportType());
        }
        if (query.getInsuranceProduct() != null) {
            wrapper.eq(InsuranceApplication::getInsuranceProduct, query.getInsuranceProduct());
        }
        if (query.getApplicantId() != null) {
            wrapper.eq(InsuranceApplication::getApplicantId, query.getApplicantId());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item
                    .like(InsuranceApplication::getApplicationNo, keyword)
                    .or()
                    .like(InsuranceApplication::getCargoName, keyword)
                    .or()
                    .like(InsuranceApplication::getApplicantName, keyword));
        }
        
        if (query.getStartDate() != null) {
            wrapper.ge(InsuranceApplication::getCreatedAt, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.le(InsuranceApplication::getCreatedAt, query.getEndDate().atTime(23, 59, 59));
        }
        
        applySort(wrapper, query.getSortBy(), query.getSortOrder());
        
        Page<InsuranceApplication> result = page(page, wrapper);
        for (InsuranceApplication record : result.getRecords()) {
            enrichApplication(record);
        }
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InsuranceApplication cancelApplication(String applicationId, String reason) {
        InsuranceApplication application = getApplication(applicationId);
        
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
        String enterpriseId = getCurrentEnterpriseId();
        String userId = SecurityUtil.getCurrentUserId();

        ApplicationDraft draft = dto.getId() == null ? null : applicationDraftMapper.selectById(dto.getId());
        if (draft == null) {
            draft = new ApplicationDraft();
            draft.setEnterpriseId(enterpriseId);
            draft.setUserId(userId);
        } else {
            validateDraftOwnership(draft, enterpriseId, userId);
        }

        draft.setCurrentStep(dto.getCurrentStep() == null ? 1 : dto.getCurrentStep());
        draft.setStep1Data(writeJson(dto.getStep1Data()));
        draft.setStep2Data(writeJson(dto.getStep2Data()));
        draft.setStep3Data(writeJson(dto.getStep3Data()));
        draft.setExpiredAt(LocalDateTime.now().plusDays(7));

        if (draft.getId() == null) {
            applicationDraftMapper.insert(draft);
        } else {
            applicationDraftMapper.updateById(draft);
        }
        return toDraftDTO(draft);
    }
    
    @Override
    public List<InsuranceDraftDTO> getDrafts() {
        String enterpriseId = getCurrentEnterpriseId();
        String userId = SecurityUtil.getCurrentUserId();
        LambdaQueryWrapper<ApplicationDraft> wrapper = new LambdaQueryWrapper<ApplicationDraft>()
                .eq(ApplicationDraft::getEnterpriseId, enterpriseId)
                .eq(ApplicationDraft::getUserId, userId)
                .and(w -> w.isNull(ApplicationDraft::getExpiredAt).or().gt(ApplicationDraft::getExpiredAt, LocalDateTime.now()))
                .orderByDesc(ApplicationDraft::getUpdatedAt);
        List<ApplicationDraft> drafts = applicationDraftMapper.selectList(wrapper);
        List<InsuranceDraftDTO> result = new ArrayList<>();
        for (ApplicationDraft draft : drafts) {
            result.add(toDraftDTO(draft));
        }
        return result;
    }

    @Override
    public InsuranceDraftDTO getDraft(String draftId) {
        String enterpriseId = getCurrentEnterpriseId();
        String userId = SecurityUtil.getCurrentUserId();
        ApplicationDraft draft = applicationDraftMapper.selectById(draftId);
        if (draft == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保草稿不存在");
        }
        validateDraftOwnership(draft, enterpriseId, userId);
        return toDraftDTO(draft);
    }
    
    @Override
    public boolean deleteDraft(String draftId) {
        String enterpriseId = getCurrentEnterpriseId();
        String userId = SecurityUtil.getCurrentUserId();
        ApplicationDraft draft = applicationDraftMapper.selectById(draftId);
        if (draft == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保草稿不存在");
        }
        validateDraftOwnership(draft, enterpriseId, userId);
        return applicationDraftMapper.deleteById(draftId) > 0;
    }
    
    @Override
    public InsuranceTemplateDTO createTemplate(InsuranceTemplateDTO dto) {
        String enterpriseId = getCurrentEnterpriseId();
        ApplicationTemplate template = dto.getId() == null ? null : applicationTemplateMapper.selectById(dto.getId());
        if (template == null) {
            template = new ApplicationTemplate();
            template.setEnterpriseId(enterpriseId);
        } else {
            validateTemplateOwnership(template, enterpriseId);
        }

        template.setName(dto.getName());
        template.setTradeDirection(dto.getTradeDirection());
        template.setTransportType(dto.getTransportType());
        template.setInsuranceProduct(dto.getInsuranceProduct());
        template.setInsurerId(dto.getInsurerId());
        template.setApplicantId(dto.getApplicantId());
        template.setInsuredId(dto.getInsuredId());
        template.setDepartureCountry(dto.getDepartureCountry());
        template.setDepartureCity(dto.getDepartureCity());
        template.setArrivalCountry(dto.getArrivalCountry());
        template.setArrivalCity(dto.getArrivalCity());
        template.setCargoCategory(dto.getCargoCategory());
        template.setPackingType(dto.getPackingType());
        template.setAdditionRatio(dto.getAdditionRatio());
        template.setSpecialTerms(dto.getSpecialTerms());

        if (template.getId() == null) {
            applicationTemplateMapper.insert(template);
        } else {
            applicationTemplateMapper.updateById(template);
        }
        return toTemplateDTO(template);
    }
    
    @Override
    public List<InsuranceTemplateDTO> getTemplates() {
        String enterpriseId = getCurrentEnterpriseId();
        LambdaQueryWrapper<ApplicationTemplate> wrapper = new LambdaQueryWrapper<ApplicationTemplate>()
                .eq(ApplicationTemplate::getEnterpriseId, enterpriseId)
                .orderByDesc(ApplicationTemplate::getUpdatedAt);
        List<ApplicationTemplate> templates = applicationTemplateMapper.selectList(wrapper);
        List<InsuranceTemplateDTO> result = new ArrayList<>();
        for (ApplicationTemplate template : templates) {
            result.add(toTemplateDTO(template));
        }
        return result;
    }

    @Override
    public InsuranceTemplateDTO getTemplate(String templateId) {
        String enterpriseId = getCurrentEnterpriseId();
        ApplicationTemplate template = applicationTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保模板不存在");
        }
        validateTemplateOwnership(template, enterpriseId);
        return toTemplateDTO(template);
    }
    
    @Override
    public boolean deleteTemplate(String templateId) {
        String enterpriseId = getCurrentEnterpriseId();
        ApplicationTemplate template = applicationTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "投保模板不存在");
        }
        validateTemplateOwnership(template, enterpriseId);
        return applicationTemplateMapper.deleteById(templateId) > 0;
    }
    
    private String getCurrentEnterpriseId() {
        String enterpriseId = SecurityUtil.getCurrentEnterpriseId();
        if (enterpriseId == null || enterpriseId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "当前用户未登录");
        }
        return enterpriseId;
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

    private String serializeTransportDetails(ApplicationStep2DTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "运输信息格式不正确");
        }
    }

    private void applySort(LambdaQueryWrapper<InsuranceApplication> wrapper, String sortBy, String sortOrder) {
        boolean asc = "ASC".equalsIgnoreCase(sortOrder);
        String field = sortBy == null ? "createdAt" : sortBy;

        switch (field) {
            case "departureDate":
                if (asc) {
                    wrapper.orderByAsc(InsuranceApplication::getDepartureDate);
                } else {
                    wrapper.orderByDesc(InsuranceApplication::getDepartureDate);
                }
                break;
            case "submittedAt":
                if (asc) {
                    wrapper.orderByAsc(InsuranceApplication::getSubmittedAt);
                } else {
                    wrapper.orderByDesc(InsuranceApplication::getSubmittedAt);
                }
                break;
            case "applicationNo":
                if (asc) {
                    wrapper.orderByAsc(InsuranceApplication::getApplicationNo);
                } else {
                    wrapper.orderByDesc(InsuranceApplication::getApplicationNo);
                }
                break;
            case "createdAt":
            default:
                if (asc) {
                    wrapper.orderByAsc(InsuranceApplication::getCreatedAt);
                } else {
                    wrapper.orderByDesc(InsuranceApplication::getCreatedAt);
                }
                break;
        }
    }

    private void enrichApplication(InsuranceApplication application) {
        if (application == null) {
            return;
        }
        if (application.getApplicantId() != null && !application.getApplicantId().isBlank()) {
            Customer applicant = customerMapper.selectById(application.getApplicantId());
            if (applicant != null && !Boolean.TRUE.equals(applicant.getDeleted())) {
                application.setApplicantName(applicant.getName());
                application.setApplicantPhone(applicant.getContactPhone());
            }
        }
        if (application.getInsuredId() != null && !application.getInsuredId().isBlank()) {
            Customer insured = customerMapper.selectById(application.getInsuredId());
            if (insured != null && !Boolean.TRUE.equals(insured.getDeleted())) {
                application.setInsuredName(insured.getName());
                application.setInsuredPhone(insured.getContactPhone());
            }
        }
    }

    private InsuranceDraftDTO toDraftDTO(ApplicationDraft draft) {
        InsuranceDraftDTO dto = new InsuranceDraftDTO();
        dto.setId(draft.getId());
        dto.setCurrentStep(draft.getCurrentStep());
        dto.setStep1Data(readMap(draft.getStep1Data()));
        dto.setStep2Data(readMap(draft.getStep2Data()));
        dto.setStep3Data(readMap(draft.getStep3Data()));
        dto.setCreatedAt(draft.getCreatedAt());
        dto.setUpdatedAt(draft.getUpdatedAt());
        dto.setExpiredAt(draft.getExpiredAt());
        return dto;
    }

    private InsuranceTemplateDTO toTemplateDTO(ApplicationTemplate template) {
        InsuranceTemplateDTO dto = new InsuranceTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setTradeDirection(template.getTradeDirection());
        dto.setTransportType(template.getTransportType());
        dto.setInsuranceProduct(template.getInsuranceProduct());
        dto.setInsurerId(template.getInsurerId());
        dto.setApplicantId(template.getApplicantId());
        dto.setInsuredId(template.getInsuredId());
        dto.setDepartureCountry(template.getDepartureCountry());
        dto.setDepartureCity(template.getDepartureCity());
        dto.setArrivalCountry(template.getArrivalCountry());
        dto.setArrivalCity(template.getArrivalCity());
        dto.setCargoCategory(template.getCargoCategory());
        dto.setPackingType(template.getPackingType());
        dto.setAdditionRatio(template.getAdditionRatio());
        dto.setSpecialTerms(template.getSpecialTerms());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        dto.setApplicantName(getCustomerName(template.getApplicantId()));
        dto.setInsuredName(getCustomerName(template.getInsuredId()));
        return dto;
    }

    private void validateDraftOwnership(ApplicationDraft draft, String enterpriseId, String userId) {
        if (!enterpriseId.equals(draft.getEnterpriseId()) || !userId.equals(draft.getUserId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作该投保草稿");
        }
    }

    private void validateTemplateOwnership(ApplicationTemplate template, String enterpriseId) {
        if (!enterpriseId.equals(template.getEnterpriseId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权操作该投保模板");
        }
    }

    private String writeJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "投保草稿数据格式不正确");
        }
    }

    private Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "投保草稿数据读取失败");
        }
    }

    private String getCustomerName(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return null;
        }
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || Boolean.TRUE.equals(customer.getDeleted())) {
            return null;
        }
        return customer.getName();
    }
}
