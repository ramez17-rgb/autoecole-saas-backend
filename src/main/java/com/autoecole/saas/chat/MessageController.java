package com.autoecole.saas.chat;

import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** Send a message */
    @PostMapping
    public ResponseEntity<Message> send(
            @AuthenticationPrincipal User currentUser,
            @RequestBody SendMessageRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        Message msg = messageService.sendMessage(
                currentUser.getId(),
                request.receiverId(),
                tenantId,
                request.content());
        return ResponseEntity.ok(msg);
    }

    /** Get conversation with a specific user */
    @GetMapping("/conversation")
    public ResponseEntity<List<Message>> conversation(
            @AuthenticationPrincipal User currentUser,
            @RequestParam UUID with) {
        return ResponseEntity.ok(messageService.getConversation(currentUser.getId(), with));
    }

    /** Get contacts list (users who messaged me or I messaged) */
    @GetMapping("/contacts")
    public ResponseEntity<List<Map<String, Object>>> contacts(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.getContacts(currentUser.getId()));
    }

    /** Unread count */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> unreadCount(
            @AuthenticationPrincipal User currentUser) {
        int count = messageService.countUnread(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    /** Find user by email */
    @GetMapping("/find-user")
    public ResponseEntity<?> findUserByEmail(@RequestParam String email) {
        Map<String, Object> user = messageService.findUserByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    public record SendMessageRequest(UUID receiverId, String content) {}
}
