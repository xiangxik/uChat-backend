package com.uchat.backend.chat.persistence;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatHistoryTurnRepository extends JpaRepository<ChatHistoryTurnEntity, Long> {

    List<ChatHistoryTurnEntity> findByPrincipalNameAndConversationIdOrderByCreatedAtDescIdDesc(
            String principalName,
            String conversationId,
            Pageable pageable
    );
}