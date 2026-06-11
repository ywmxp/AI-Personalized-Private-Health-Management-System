package com.health.backend.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.health.backend.domain.Knowledge;
import com.health.backend.domain.KnowledgePush;
import com.health.backend.dto.KnowledgePushItem;
import com.health.backend.dto.PageResponse;
import com.health.backend.exception.BusinessException;
import com.health.backend.exception.ErrorCode;
import com.health.backend.repository.KnowledgePushRepository;
import com.health.backend.repository.KnowledgeRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class KnowledgeService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgePushRepository knowledgePushRepository;

    public KnowledgeService(KnowledgeRepository knowledgeRepository,
                            KnowledgePushRepository knowledgePushRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.knowledgePushRepository = knowledgePushRepository;
    }

    /**
     * 知识库列表（关键词 + 标签筛选 + 分页）
     */
    public PageResponse<Knowledge> listKnowledge(String keyword, String tag, int page, int size) {
        Specification<Knowledge> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                p = cb.and(p, cb.or(
                    cb.like(root.get("title"), "%" + keyword + "%"),
                    cb.like(root.get("content"), "%" + keyword + "%")
                ));
            }
            if (tag != null && !tag.isBlank()) {
                p = cb.and(p, cb.equal(root.get("relateTag"), tag));
            }
            return p;
        };

        Page<Knowledge> result = knowledgeRepository.findAll(spec,
            PageRequest.of(page - 1, size));

        return new PageResponse<>(result.getContent(), page, size, result.getTotalElements());
    }

    /**
     * 当前用户的推送列表
     */
    public PageResponse<KnowledgePushItem> listPushes(Long userId, int page, int size) {
        Page<KnowledgePush> result = knowledgePushRepository
            .findByUserIdOrderByPushTimeDesc(userId, PageRequest.of(page - 1, size));

        List<KnowledgePushItem> items = result.getContent().stream().map(push -> {
            Knowledge k = knowledgeRepository.findById(push.getKnowledgeId())
                .orElse(null);
            return new KnowledgePushItem(
                push.getPushId(),
                push.getKnowledgeId(),
                k != null ? k.getTitle() : "未知",
                k != null ? k.getRelateTag() : "",
                push.getPushTime().format(DT_FMT),
                push.getIsRead()
            );
        }).toList();

        return new PageResponse<>(items, page, size, result.getTotalElements());
    }

    /**
     * 标记推送为已读
     */
    public void markRead(Long pushId, Long userId) {
        KnowledgePush push = knowledgePushRepository.findByPushIdAndUserId(pushId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "推送记录不存在"));
        push.setIsRead(1);
        knowledgePushRepository.save(push);
    }
}
