package com.autoecole.saas.chat;

import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Message sendMessage(UUID senderId, UUID receiverId, UUID tenantId, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setTenantId(tenantId);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);
        return messageRepository.save(message);
    }

    public List<Message> getConversation(UUID userId, UUID otherId) {
        // Mark messages from otherId as read
        List<Message> conversation = messageRepository.findConversation(userId, otherId);
        conversation.stream()
                .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });
        return conversation;
    }

    public int countUnread(UUID userId) {
        return messageRepository.findUnreadByReceiver(userId).size();
    }

    /**
     * Returns a list of contacts based on tenant directory and previous messages.
     */
    public List<Map<String, Object>> getContacts(UUID userId) {
        User currentUser = userRepository.findById(userId).orElseThrow();
        UUID tenantId = currentUser.getTenant() != null ? currentUser.getTenant().getId() : null;
        
        List<User> directory = new java.util.ArrayList<>();
        if (tenantId != null) {
            String currentUserRole = currentUser.getRoles().stream().map(r -> r.getName()).findFirst().orElse("");
            List<User> allTenantUsers = userRepository.findByTenantId(tenantId);
            
            for (User u : allTenantUsers) {
                if (u.getId().equals(userId)) continue;
                String role = u.getRoles().stream().map(r -> r.getName()).findFirst().orElse("");
                
                if (currentUserRole.equals("TENANT_ADMIN") || currentUserRole.equals("ADMIN")) {
                    directory.add(u);
                } else if (currentUserRole.equals("STUDENT")) {
                    if (role.equals("TENANT_ADMIN") || role.equals("MONITOR") || role.equals("ADMIN")) {
                        directory.add(u);
                    }
                } else if (currentUserRole.equals("MONITOR")) {
                    if (role.equals("TENANT_ADMIN") || role.equals("STUDENT") || role.equals("ADMIN")) {
                        directory.add(u);
                    }
                } else {
                    directory.add(u);
                }
            }
        }
        
        // Add users who have contacted us but might not be in the filtered directory
        List<UUID> contactIds = messageRepository.findContactIds(userId);
        for (UUID cid : contactIds) {
            if (directory.stream().noneMatch(u -> u.getId().equals(cid))) {
                userRepository.findById(cid).ifPresent(directory::add);
            }
        }

        return directory.stream().map(u -> {
            Map<String, Object> info = new java.util.HashMap<>();
            info.put("userId", u.getId());
            String name = (u.getFirstName() != null ? u.getFirstName() : "")
                    + " " + (u.getLastName() != null ? u.getLastName() : "");
            name = name.trim();
            info.put("name", name.isEmpty() ? u.getEmail() : name);
            info.put("email", u.getEmail());
            info.put("role", u.getRoles().stream().map(r -> r.getName()).findFirst().orElse(""));
            
            // Last message
            List<Message> conv = messageRepository.findConversation(userId, u.getId());
            if (!conv.isEmpty()) {
                Message last = conv.get(conv.size() - 1);
                info.put("lastMessage", last.getContent());
                info.put("lastAt", last.getSentAt());
            }
            
            // Unread count
            long unread = messageRepository.findUnreadByReceiver(userId).stream()
                    .filter(m -> m.getSenderId().equals(u.getId())).count();
            info.put("unread", unread);
            
            return info;
        })
        .sorted((a, b) -> {
            long unreadA = (long) a.get("unread");
            long unreadB = (long) b.get("unread");
            if (unreadA != unreadB) return Long.compare(unreadB, unreadA);
            
            Object timeA = a.get("lastAt");
            Object timeB = b.get("lastAt");
            if (timeA != null && timeB != null) {
                return ((LocalDateTime) timeB).compareTo((LocalDateTime) timeA);
            }
            if (timeA != null) return -1;
            if (timeB != null) return 1;
            
            return ((String) a.get("name")).compareToIgnoreCase((String) b.get("name"));
        })
        .collect(Collectors.toList());
    }

    public Map<String, Object> findUserByEmail(String email) {
        return userRepository.findByEmail(email).map(u -> {
            Map<String, Object> info = new java.util.HashMap<>();
            info.put("userId", u.getId());
            String name = (u.getFirstName() != null ? u.getFirstName() : "")
                    + " " + (u.getLastName() != null ? u.getLastName() : "");
            name = name.trim();
            info.put("name", name.isEmpty() ? u.getEmail() : name);
            info.put("email", u.getEmail());
            info.put("role", u.getRoles().stream().map(r -> r.getName()).findFirst().orElse(""));
            info.put("unread", 0);
            return info;
        }).orElse(null);
    }
}
