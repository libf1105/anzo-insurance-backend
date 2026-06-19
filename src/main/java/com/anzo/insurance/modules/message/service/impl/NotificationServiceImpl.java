package com.anzo.insurance.modules.message.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.modules.auth.entity.Enterprise;
import com.anzo.insurance.modules.auth.entity.User;
import com.anzo.insurance.modules.auth.repository.EnterpriseRepository;
import com.anzo.insurance.modules.auth.repository.UserRepository;
import com.anzo.insurance.modules.message.dto.NotificationDTO;
import com.anzo.insurance.modules.message.dto.NotificationQueryDTO;
import com.anzo.insurance.modules.message.entity.Notification;
import com.anzo.insurance.modules.message.repository.NotificationRepository;
import com.anzo.insurance.modules.message.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 获取企业信息
        Enterprise enterprise = getEnterprise(notificationDTO.getEnterpriseId(), currentUser.getEnterpriseId());
        
        // 创建通知实体
        Notification notification = new Notification();
        BeanUtil.copyProperties(notificationDTO, notification, true);
        
        // 设置必要字段
        notification.setSenderId(currentUser.getId());
        notification.setEnterpriseId(enterprise.getId());
        
        if (notification.getStatus() == null) {
            notification.setStatus(Notification.Status.UNREAD.name());
        }
        
        if (notification.getPriority() == null) {
            notification.setPriority(Notification.Priority.NORMAL.name());
        }
        
        // 设置过期时间（默认为30天后）
        if (notification.getExpiresAt() == null) {
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));
        }
        
        // 保存通知
        notificationRepository.insert(notification);
        
        // 如果需要发送给特定用户
        if (notificationDTO.getUserIds() != null && !notificationDTO.getUserIds().isEmpty()) {
            // 为每个用户创建通知
            for (Long userId : notificationDTO.getUserIds()) {
                User targetUser = userRepository.selectById(userId);
                if (targetUser != null && targetUser.getEnterpriseId().equals(enterprise.getId())) {
                    Notification userNotification = new Notification();
                    BeanUtil.copyProperties(notification, userNotification, true);
                    userNotification.setId(null); // 生成新ID
                    userNotification.setUserId(userId);
                    notificationRepository.insert(userNotification);
                }
            }
            
            // 删除原始通知（非特定用户的通知）
            notificationRepository.deleteById(notification.getId());
        }
        
        log.info("通知创建成功: id={}, title={}, type={}, enterpriseId={}", 
                notification.getId(), notification.getTitle(), notification.getType(), enterprise.getId());
        
        return convertToDTO(notification, enterprise, currentUser);
    }

    @Override
    public NotificationDTO getNotificationById(Long notificationId) {
        Notification notification = notificationRepository.selectById(notificationId);
        if (notification == null || notification.getDeleted()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "通知不存在");
        }
        
        // 检查权限
        checkNotificationAccessPermission(notification);
        
        return convertToDTO(notification);
    }

    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        checkUserAccessPermission(userId);
        
        List<Notification> notifications = notificationRepository.findByUserId(userId, 100);
        return notifications.stream()
                .filter(notification -> !notification.getDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getEnterpriseNotifications(Long enterpriseId) {
        checkEnterpriseAccessPermission(enterpriseId);
        
        List<Notification> notifications = notificationRepository.findByEnterpriseId(enterpriseId);
        return notifications.stream()
                .filter(notification -> !notification.getDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<NotificationDTO> queryNotifications(NotificationQueryDTO queryDTO, Pageable pageable) {
        // 构建查询条件
        LambdaQueryWrapper<Notification> queryWrapper = buildQueryWrapper(queryDTO);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Notification> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                        pageable.getPageNumber() + 1, pageable.getPageSize());
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Notification> result = 
                notificationRepository.selectPage(page, queryWrapper);
        
        List<NotificationDTO> content = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.selectById(notificationId);
        if (notification == null || notification.getDeleted()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "通知不存在");
        }
        
        // 检查权限
        checkNotificationAccessPermission(notification);
        
        if (Notification.Status.UNREAD.name().equals(notification.getStatus())) {
            notificationRepository.markAsRead(notificationId, LocalDateTime.now());
            log.debug("通知标记为已读: notificationId={}", notificationId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        checkUserAccessPermission(userId);
        
        int updated = notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        log.info("批量标记通知为已读: userId={}, count={}", userId, updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsReadByEnterprise(Long enterpriseId) {
        checkEnterpriseAccessPermission(enterpriseId);
        
        int updated = notificationRepository.markAllAsReadByEnterpriseId(enterpriseId, LocalDateTime.now());
        log.info("批量标记企业通知为已读: enterpriseId={}, count={}", enterpriseId, updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.selectById(notificationId);
        if (notification == null || notification.getDeleted()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "通知不存在");
        }
        
        // 检查删除权限
        checkNotificationDeletePermission(notification);
        
        notification.setDeleted(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.updateById(notification);
        
        log.info("通知已删除: notificationId={}", notificationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteNotifications(List<Long> notificationIds) {
        for (Long notificationId : notificationIds) {
            try {
                deleteNotification(notificationId);
            } catch (Exception e) {
                log.error("批量删除通知失败: notificationId={}, error={}", notificationId, e.getMessage());
            }
        }
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        checkUserAccessPermission(userId);
        
        return notificationRepository.getUnreadCountByUserId(userId, LocalDateTime.now());
    }

    @Override
    public Integer getEnterpriseUnreadCount(Long enterpriseId) {
        checkEnterpriseAccessPermission(enterpriseId);
        
        return notificationRepository.getUnreadCountByEnterpriseId(enterpriseId, LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemNotification(String title, String content, Long enterpriseId, List<Long> userIds) {
        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 获取企业信息
        Enterprise enterprise = getEnterprise(enterpriseId, currentUser.getEnterpriseId());
        
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setTitle(title);
        notificationDTO.setContent(content);
        notificationDTO.setType(Notification.Type.SYSTEM.name());
        notificationDTO.setPriority(Notification.Priority.NORMAL.name());
        notificationDTO.setUserIds(userIds);
        notificationDTO.setEnterpriseId(enterprise.getId());
        
        createNotification(notificationDTO);
        
        log.info("系统通知已发送: title={}, enterpriseId={}, userIds={}", 
                title, enterprise.getId(), userIds != null ? userIds.size() : "all");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendBalanceNotification(Long enterpriseId, Long userId, String changeType, 
                                       String amount, String balanceAfter, String remark) {
        String title = "余额变动通知";
        String content = String.format("您的账户发生%s：%s元，当前余额：%s元。%s", 
                getChangeTypeText(changeType), amount, balanceAfter, remark != null ? "备注：" + remark : "");
        
        List<Long> userIds = userId != null ? List.of(userId) : null;
        
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setTitle(title);
        notificationDTO.setContent(content);
        notificationDTO.setType(Notification.Type.BALANCE.name());
        notificationDTO.setPriority(Notification.Priority.NORMAL.name());
        notificationDTO.setUserIds(userIds);
        notificationDTO.setEnterpriseId(enterpriseId);
        notificationDTO.setExtraData(String.format("{\"changeType\":\"%s\",\"amount\":\"%s\",\"balanceAfter\":\"%s\"}", 
                changeType, amount, balanceAfter));
        
        createNotification(notificationDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendReviewNotification(Long enterpriseId, Long userId, String reviewType,
                                      String reviewResult, String targetName, String remark) {
        String title = "审核结果通知";
        String resultText = "通过".equals(reviewResult) ? "已通过" : "未通过";
        String content = String.format("您的%s审核%s。审核对象：%s。%s", 
                reviewType, resultText, targetName, remark != null ? "备注：" + remark : "");
        
        List<Long> userIds = userId != null ? List.of(userId) : null;
        
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setTitle(title);
        notificationDTO.setContent(content);
        notificationDTO.setType(Notification.Type.REVIEW.name());
        notificationDTO.setPriority(Notification.Priority.NORMAL.name());
        notificationDTO.setUserIds(userIds);
        notificationDTO.setEnterpriseId(enterpriseId);
        notificationDTO.setExtraData(String.format("{\"reviewType\":\"%s\",\"reviewResult\":\"%s\",\"targetName\":\"%s\"}", 
                reviewType, reviewResult, targetName));
        
        createNotification(notificationDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendWarningNotification(Long enterpriseId, Long userId, String warningType,
                                       String warningContent, String actionRequired) {
        String title = "系统预警通知";
        String content = String.format("【%s】%s。需要采取的措施：%s", 
                warningType, warningContent, actionRequired);
        
        List<Long> userIds = userId != null ? List.of(userId) : null;
        
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setTitle(title);
        notificationDTO.setContent(content);
        notificationDTO.setType(Notification.Type.WARNING.name());
        notificationDTO.setPriority(Notification.Priority.HIGH.name());
        notificationDTO.setUserIds(userIds);
        notificationDTO.setEnterpriseId(enterpriseId);
        notificationDTO.setExtraData(String.format("{\"warningType\":\"%s\",\"actionRequired\":\"%s\"}", 
                warningType, actionRequired));
        
        createNotification(notificationDTO);
    }

    @Override
    public List<NotificationDTO> getLatestNotifications(Long userId, int limit) {
        checkUserAccessPermission(userId);
        
        List<Notification> notifications = notificationRepository.findLatestByUserId(userId, limit, LocalDateTime.now());
        return notifications.stream()
                .filter(notification -> !notification.getDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getEnterpriseLatestNotifications(Long enterpriseId, int limit) {
        checkEnterpriseAccessPermission(enterpriseId);
        
        List<Notification> notifications = notificationRepository.findByEnterpriseIdWithPriority(enterpriseId, LocalDateTime.now());
        return notifications.stream()
                .filter(notification -> !notification.getDeleted())
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanupExpiredNotifications() {
        int deleted = notificationRepository.deleteExpiredNotifications(LocalDateTime.now());
        log.info("清理过期通知完成: deleted={}", deleted);
    }

    @Override
    public NotificationStatistics getNotificationStatistics(Long userId) {
        checkUserAccessPermission(userId);
        
        NotificationRepository.NotificationStatistics stats = 
                notificationRepository.getNotificationStatistics(userId, LocalDateTime.now());
        
        // 计算今日、本周、本月数量
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.with(LocalTime.MIN);
        LocalDateTime weekStart = todayStart.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDateTime monthStart = todayStart.withDayOfMonth(1);
        
        LambdaQueryWrapper<Notification> todayQuery = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .ge(Notification::getCreatedAt, todayStart)
                .eq(Notification::getDeleted, false);
        
        LambdaQueryWrapper<Notification> weekQuery = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .ge(Notification::getCreatedAt, weekStart)
                .eq(Notification::getDeleted, false);
        
        LambdaQueryWrapper<Notification> monthQuery = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .ge(Notification::getCreatedAt, monthStart)
                .eq(Notification::getDeleted, false);
        
        Integer todayCount = notificationRepository.selectCount(todayQuery).intValue();
        Integer weekCount = notificationRepository.selectCount(weekQuery).intValue();
        Integer monthCount = notificationRepository.selectCount(monthQuery).intValue();
        
        return new NotificationStatistics() {
            @Override
            public Integer getUnreadCount() {
                return stats != null ? stats.getUnreadCount() : 0;
            }
            
            @Override
            public Integer getTotalCount() {
                return stats != null ? stats.getTotalCount() : 0;
            }
            
            @Override
            public Integer getSystemCount() {
                return stats != null ? stats.getSystemCount() : 0;
            }
            
            @Override
            public Integer getReviewCount() {
                return stats != null ? stats.getReviewCount() : 0;
            }
            
            @Override
            public Integer getBalanceCount() {
                return stats != null ? stats.getBalanceCount() : 0;
            }
            
            @Override
            public Integer getWarningCount() {
                return stats != null ? stats.getWarningCount() : 0;
            }
            
            @Override
            public Integer getInfoCount() {
                return stats != null ? getTotalCount() - getSystemCount() - getReviewCount() - getBalanceCount() - getWarningCount() : 0;
            }
            
            @Override
            public Integer getTodayCount() {
                return todayCount != null ? todayCount : 0;
            }
            
            @Override
            public Integer getThisWeekCount() {
                return weekCount != null ? weekCount : 0;
            }
            
            @Override
            public Integer getThisMonthCount() {
                return monthCount != null ? monthCount : 0;
            }
        };
    }

    @Override
    public NotificationStatistics getEnterpriseNotificationStatistics(Long enterpriseId) {
        checkEnterpriseAccessPermission(enterpriseId);
        
        // 简化实现：获取企业所有用户的统计信息总和
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<User>()
                .eq(User::getEnterpriseId, enterpriseId)
                .eq(User::getDeleted, false);
        
        List<User> users = userRepository.selectList(userQuery);
        
        int totalUnread = 0;
        int totalCount = 0;
        int todayCount = 0;
        int weekCount = 0;
        int monthCount = 0;
        
        for (User user : users) {
            NotificationStatistics stats = getNotificationStatistics(user.getId());
            totalUnread += stats.getUnreadCount();
            totalCount += stats.getTotalCount();
            todayCount += stats.getTodayCount();
            weekCount += stats.getThisWeekCount();
            monthCount += stats.getThisMonthCount();
        }

        final int finalTotalUnread = totalUnread;
        final int finalTotalCount = totalCount;
        final int finalTodayCount = todayCount;
        final int finalWeekCount = weekCount;
        final int finalMonthCount = monthCount;
        
        return new NotificationStatistics() {
            @Override
            public Integer getUnreadCount() {
                return finalTotalUnread;
            }
            
            @Override
            public Integer getTotalCount() {
                return finalTotalCount;
            }
            
            @Override
            public Integer getSystemCount() {
                // 简化实现，实际需要从数据库查询
                return 0;
            }
            
            @Override
            public Integer getReviewCount() {
                // 简化实现，实际需要从数据库查询
                return 0;
            }
            
            @Override
            public Integer getBalanceCount() {
                // 简化实现，实际需要从数据库查询
                return 0;
            }
            
            @Override
            public Integer getWarningCount() {
                // 简化实现，实际需要从数据库查询
                return 0;
            }
            
            @Override
            public Integer getInfoCount() {
                // 简化实现
                return finalTotalCount;
            }
            
            @Override
            public Integer getTodayCount() {
                return finalTodayCount;
            }
            
            @Override
            public Integer getThisWeekCount() {
                return finalWeekCount;
            }
            
            @Override
            public Integer getThisMonthCount() {
                return finalMonthCount;
            }
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendPolicyNotification(Long enterpriseId, Long userId, Long policyId,
                                      String notificationType, String content) {
        String title = "保单通知";
        
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setTitle(title);
        notificationDTO.setContent(content);
        notificationDTO.setType(Notification.Type.INFO.name());
        notificationDTO.setPriority(Notification.Priority.NORMAL.name());
        notificationDTO.setUserIds(userId != null ? List.of(userId) : null);
        notificationDTO.setEnterpriseId(enterpriseId);
        notificationDTO.setRelatedId(policyId);
        notificationDTO.setRelatedType(Notification.RelatedType.POLICY.name());
        notificationDTO.setExtraData(String.format("{\"policyId\":\"%s\",\"notificationType\":\"%s\"}", 
                policyId, notificationType));
        
        createNotification(notificationDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendClaimNotification(Long enterpriseId, Long userId, Long claimId,
                                     String notificationType, String content) {
        String title = "理赔通知";
        
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setTitle(title);
        notificationDTO.setContent(content);
        notificationDTO.setType(Notification.Type.INFO.name());
        notificationDTO.setPriority(Notification.Priority.NORMAL.name());
        notificationDTO.setUserIds(userId != null ? List.of(userId) : null);
        notificationDTO.setEnterpriseId(enterpriseId);
        notificationDTO.setRelatedId(claimId);
        notificationDTO.setRelatedType(Notification.RelatedType.CLAIM.name());
        notificationDTO.setExtraData(String.format("{\"claimId\":\"%s\",\"notificationType\":\"%s\"}", 
                claimId, notificationType));
        
        createNotification(notificationDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEnterpriseStatusNotification(Long enterpriseId, String oldStatus, 
                                                String newStatus, String remark) {
        String title = "企业状态变更通知";
        String content = String.format("您的企业状态已从【%s】变更为【%s】。%s", 
                oldStatus, newStatus, remark != null ? "备注：" + remark : "");
        
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setTitle(title);
        notificationDTO.setContent(content);
        notificationDTO.setType(Notification.Type.SYSTEM.name());
        notificationDTO.setPriority(Notification.Priority.NORMAL.name());
        notificationDTO.setUserIds(null); // 发送给企业所有用户
        notificationDTO.setEnterpriseId(enterpriseId);
        notificationDTO.setExtraData(String.format("{\"oldStatus\":\"%s\",\"newStatus\":\"%s\"}", 
                oldStatus, newStatus));
        
        createNotification(notificationDTO);
    }

    // ============ 私有方法 ============

    private Enterprise getEnterprise(Long requestEnterpriseId, Long currentUserEnterpriseId) {
        if (requestEnterpriseId != null) {
            Enterprise enterprise = enterpriseRepository.selectById(requestEnterpriseId);
            if (enterprise == null) {
                throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
            }
            return enterprise;
        } else {
            Enterprise enterprise = enterpriseRepository.selectById(currentUserEnterpriseId);
            if (enterprise == null) {
                throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "用户未关联企业");
            }
            return enterprise;
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return convertToDTO(notification, null, null);
    }

    private NotificationDTO convertToDTO(Notification notification, Enterprise enterprise, User sender) {
        NotificationDTO dto = new NotificationDTO();
        BeanUtil.copyProperties(notification, dto, true);
        
        // 设置描述性字段
        dto.setTypeDescription(notification.getTypeDescription());
        dto.setStatusDescription(notification.getStatusDescription());
        dto.setPriorityDescription(notification.getPriorityDescription());
        dto.setRelatedTypeDescription(notification.getRelatedTypeDescription());
        
        // 设置辅助字段
        dto.setIsUnread(notification.isUnread());
        dto.setIsUrgent(notification.isUrgent());
        dto.setIsHighPriority(notification.isHighPriority());
        dto.setSummary(notification.getSummary());
        dto.setExpired(notification.isExpired());
        dto.setValid(notification.isValid());
        
        // 设置显示信息
        if (enterprise != null) {
            dto.setEnterpriseName(enterprise.getName());
        }
        
        if (sender != null) {
            dto.setSenderName(sender.getRealName());
        }
        
        if (notification.getUserId() != null) {
            User receiver = userRepository.selectById(notification.getUserId());
            if (receiver != null) {
                dto.setReceiverName(receiver.getRealName());
            }
        }
        
        return dto;
    }

    private LambdaQueryWrapper<Notification> buildQueryWrapper(NotificationQueryDTO queryDTO) {
        LambdaQueryWrapper<Notification> queryWrapper = Wrappers.lambdaQuery(Notification.class);
        
        if (StrUtil.isNotBlank(queryDTO.getType())) {
            queryWrapper.eq(Notification::getType, queryDTO.getType());
        }
        
        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            queryWrapper.eq(Notification::getStatus, queryDTO.getStatus());
        }
        
        if (StrUtil.isNotBlank(queryDTO.getPriority())) {
            queryWrapper.eq(Notification::getPriority, queryDTO.getPriority());
        }
        
        if (StrUtil.isNotBlank(queryDTO.getRelatedType())) {
            queryWrapper.eq(Notification::getRelatedType, queryDTO.getRelatedType());
        }
        
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Notification::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Notification::getContent, queryDTO.getKeyword()));
        }
        
        if (queryDTO.getStartTime() != null) {
            queryWrapper.ge(Notification::getCreatedAt, queryDTO.getStartTime());
        }
        
        if (queryDTO.getEndTime() != null) {
            queryWrapper.le(Notification::getCreatedAt, queryDTO.getEndTime());
        }
        
        if (Boolean.TRUE.equals(queryDTO.getOnlyValid())) {
            queryWrapper.and(wrapper -> wrapper
                    .isNull(Notification::getExpiresAt)
                    .or()
                    .gt(Notification::getExpiresAt, LocalDateTime.now()));
        }
        
        if (!Boolean.TRUE.equals(queryDTO.getIncludeDeleted())) {
            queryWrapper.eq(Notification::getDeleted, false);
        }
        
        if (queryDTO.getEnterpriseId() != null) {
            queryWrapper.eq(Notification::getEnterpriseId, queryDTO.getEnterpriseId());
        }
        
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(Notification::getUserId, queryDTO.getUserId());
        }
        
        // 排序
        if ("createdAt".equals(queryDTO.getSortBy())) {
            if ("ASC".equalsIgnoreCase(queryDTO.getSortDirection())) {
                queryWrapper.orderByAsc(Notification::getCreatedAt);
            } else {
                queryWrapper.orderByDesc(Notification::getCreatedAt);
            }
        } else if ("priority".equals(queryDTO.getSortBy())) {
            // 优先级排序需要特殊处理
            queryWrapper.orderByDesc(Notification::getPriority);
            queryWrapper.orderByDesc(Notification::getCreatedAt);
        }
        
        return queryWrapper;
    }

    private void checkNotificationAccessPermission(Notification notification) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 管理员可以访问所有通知
        if (user.getRole().startsWith("SUPER_ADMIN") || user.getRole().startsWith("ADMIN")) {
            return;
        }
        
        // 用户只能访问自己的通知或自己企业的通知
        if (notification.getUserId() != null) {
            if (!notification.getUserId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权访问该通知");
            }
        } else if (notification.getEnterpriseId() != null) {
            if (!notification.getEnterpriseId().equals(user.getEnterpriseId())) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权访问该通知");
            }
        }
    }

    private void checkNotificationDeletePermission(Notification notification) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 管理员可以删除所有通知
        if (user.getRole().startsWith("SUPER_ADMIN") || user.getRole().startsWith("ADMIN")) {
            return;
        }
        
        // 用户只能删除自己的通知
        if (notification.getUserId() == null || !notification.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权删除该通知");
        }
    }

    private void checkUserAccessPermission(Long targetUserId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 管理员可以访问所有用户
        if (user.getRole().startsWith("SUPER_ADMIN") || user.getRole().startsWith("ADMIN")) {
            return;
        }
        
        // 用户只能访问自己
        if (!targetUserId.equals(user.getId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权访问该用户");
        }
    }

    private void checkEnterpriseAccessPermission(Long targetEnterpriseId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 管理员可以访问所有企业
        if (user.getRole().startsWith("SUPER_ADMIN") || user.getRole().startsWith("ADMIN")) {
            return;
        }
        
        // 用户只能访问自己企业
        if (!targetEnterpriseId.equals(user.getEnterpriseId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED.getCode(), "无权访问该企业");
        }
    }

    private String getChangeTypeText(String changeType) {
        switch (changeType) {
            case "RECHARGE":
                return "充值";
            case "CONSUME":
                return "消费";
            case "REFUND":
                return "退款";
            case "ADJUST":
                return "调整";
            default:
                return "变动";
        }
    }
}
