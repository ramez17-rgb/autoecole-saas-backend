package com.autoecole.saas.user;

import com.autoecole.saas.joinrequest.JoinRequestStatus;
import com.autoecole.saas.joinrequest.SchoolJoinRequest;
import com.autoecole.saas.joinrequest.SchoolJoinRequestRepository;
import com.autoecole.saas.monitor.MonitorRepository;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final StudentRepository studentRepository;
    private final MonitorRepository monitorRepository;
    private final SchoolJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${app.uploads.dir:uploads/profile-pictures}")
    private String uploadsDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ProfileController(StudentRepository studentRepository,
                             MonitorRepository monitorRepository,
                             SchoolJoinRequestRepository joinRequestRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.monitorRepository = monitorRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public Map<String, Object> getMyProfile(@AuthenticationPrincipal User user) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getId());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("telephone", user.getTelephone());
        profile.put("profilePictureUrl", user.getProfilePictureUrl());

        // Determine role(s)
        String role = user.getRoles().stream()
                .map(r -> r.getName())
                .findFirst()
                .orElse("UNKNOWN");
        profile.put("role", role);

        UUID tenantId = TenantContext.getTenantId();

        // If user is a student, find their studentId and tenant info
        if ("STUDENT".equals(role) || "ELEVE".equals(role)) {
            Optional<Student> studentOpt;
            if (tenantId != null) {
                studentOpt = studentRepository.findByTenantId(tenantId).stream()
                        .filter(s -> s.getUser() != null && s.getUser().getId().equals(user.getId()))
                        .findFirst();
            } else {
                // Fallback: find student by user across all tenants
                studentOpt = studentRepository.findByUserId(user.getId());
            }
            studentOpt.ifPresent(s -> {
                profile.put("studentId", s.getId());
                // Expose tenantId so frontend knows if student is assigned to a school
                if (s.getTenant() != null) {
                    profile.put("tenantId", s.getTenant().getId());
                    profile.put("schoolName", s.getTenant().getName());
                } else {
                    // No tenant assigned — check for pending/rejected join request
                    List<SchoolJoinRequest> requests = joinRequestRepository.findByStudentId(s.getId());
                    boolean hasPending = requests.stream()
                            .anyMatch(r -> r.getStatus() == JoinRequestStatus.PENDING);
                    if (hasPending) {
                        profile.put("joinRequestStatus", "PENDING");
                    } else {
                        boolean hasRejected = requests.stream()
                                .anyMatch(r -> r.getStatus() == JoinRequestStatus.REJECTED);
                        if (hasRejected) {
                            profile.put("joinRequestStatus", "REJECTED");
                        }
                    }
                }
            });
        }

        // If user is a monitor, find their monitorId
        if ("MONITOR".equals(role) || "MONITEUR".equals(role)) {
            if (tenantId != null) {
                monitorRepository.findByTenantId(tenantId).stream()
                        .filter(m -> m.getUser().getId().equals(user.getId()))
                        .findFirst()
                        .ifPresent(m -> profile.put("monitorId", m.getId()));
            } else {
                monitorRepository.findAll().stream()
                        .filter(m -> m.getUser().getId().equals(user.getId()))
                        .findFirst()
                        .ifPresent(m -> profile.put("monitorId", m.getId()));
            }
        }

        return profile;
    }

    /**
     * Update the current user's profile (name, email, telephone).
     * Optionally change password if currentPassword + newPassword are provided.
     */
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest request) {

        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.firstName() != null && !request.firstName().isBlank()) {
            dbUser.setFirstName(request.firstName());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            dbUser.setLastName(request.lastName());
        }
        if (request.telephone() != null) {
            dbUser.setTelephone(request.telephone());
        }
        // Email update (check uniqueness if changed)
        if (request.email() != null && !request.email().isBlank() && !request.email().equals(dbUser.getEmail())) {
            if (userRepository.findByEmail(request.email()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cet email est déjà utilisé."));
            }
            dbUser.setEmail(request.email());
        }
        // Password change
        if (request.currentPassword() != null && !request.currentPassword().isBlank()
                && request.newPassword() != null && !request.newPassword().isBlank()) {
            if (!passwordEncoder.matches(request.currentPassword(), dbUser.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mot de passe actuel incorrect."));
            }
            dbUser.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        userRepository.save(dbUser);

        Map<String, Object> updated = new HashMap<>();
        updated.put("userId", dbUser.getId());
        updated.put("email", dbUser.getEmail());
        updated.put("firstName", dbUser.getFirstName());
        updated.put("lastName", dbUser.getLastName());
        updated.put("telephone", dbUser.getTelephone());
        updated.put("profilePictureUrl", dbUser.getProfilePictureUrl());
        updated.put("message", "Profil mis à jour avec succès.");
        return ResponseEntity.ok(updated);
    }

    /**
     * Upload a profile picture for the currently authenticated user.
     * Validates file type via content-type header AND magic bytes.
     * Stores file on disk with a UUID-based name to prevent path traversal.
     */
    @PostMapping(value = "/me/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadProfilePicture(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier est vide."));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier dépasse 5 Mo."));
        }

        // Validate content type header
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Format non autorisé. Utilisez JPEG, PNG ou WebP."));
        }

        // Validate extension
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String extension = "";
        int dotIdx = originalFilename.lastIndexOf('.');
        if (dotIdx >= 0) {
            extension = originalFilename.substring(dotIdx + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Extension non autorisée."));
        }

        // Validate magic bytes (first few bytes of file)
        try {
            byte[] header = file.getInputStream().readNBytes(12);
            if (!isValidImageMagicBytes(header, contentType)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le contenu du fichier ne correspond pas au format déclaré."));
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur de lecture du fichier."));
        }

        // Create upload directory if needed
        Path uploadPath = Paths.get(uploadsDir);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur de création du répertoire de stockage."));
        }

        // Generate safe filename
        String safeFilename = UUID.randomUUID().toString() + "." + extension;
        Path filePath = uploadPath.resolve(safeFilename);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la sauvegarde du fichier."));
        }

        // Delete old profile picture if it exists and is a local file
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String oldUrl = dbUser.getProfilePictureUrl();
        if (oldUrl != null && oldUrl.contains("/api/profile/pictures/")) {
            String oldFilename = oldUrl.substring(oldUrl.lastIndexOf('/') + 1);
            try {
                Path oldPath = uploadPath.resolve(oldFilename);
                Files.deleteIfExists(oldPath);
            } catch (IOException ignored) {}
        }

        // Build URL
        String pictureUrl = baseUrl + "/api/profile/pictures/" + safeFilename;
        dbUser.setProfilePictureUrl(pictureUrl);
        userRepository.save(dbUser);

        return ResponseEntity.ok(Map.of(
                "message", "Photo de profil mise à jour avec succès.",
                "profilePictureUrl", pictureUrl
        ));
    }

    /**
     * Serve a profile picture file.
     */
    @GetMapping("/pictures/{filename:.+}")
    public ResponseEntity<Resource> serveProfilePicture(@PathVariable String filename) {
        // Prevent path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = Paths.get(uploadsDir).resolve(filename);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validate file content by magic bytes.
     */
    private boolean isValidImageMagicBytes(byte[] header, String contentType) {
        if (header.length < 4) return false;
        // JPEG: FF D8 FF
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return contentType.contains("jpeg") || contentType.contains("jpg");
        }
        // PNG: 89 50 4E 47
        if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50
                && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
            return contentType.contains("png");
        }
        // WebP: RIFF....WEBP
        if (header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return contentType.contains("webp");
        }
        return false;
    }

    public record UpdateProfileRequest(
            String firstName,
            String lastName,
            String email,
            String telephone,
            String currentPassword,
            String newPassword) {}
}
