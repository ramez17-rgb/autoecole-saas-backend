package com.autoecole.saas.enrollment;

import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantRepository;
import com.autoecole.saas.user.RoleEntity;
import com.autoecole.saas.user.RoleRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import com.autoecole.saas.security.JwtService;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EnrollmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private EnrollmentRequestRepository enrollmentRequestRepository;

    @Autowired
    private JwtService jwtService;

    private Tenant activeSchool;
    private User adminUser;
    private String adminToken;

    @BeforeEach
    public void setup() {
        enrollmentRequestRepository.deleteAll();
        studentRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
        roleRepository.deleteAll();

        // 1. Setup Role
        RoleEntity adminRole = new RoleEntity("TENANT_ADMIN");
        roleRepository.save(adminRole);

        RoleEntity studentRole = new RoleEntity("ELEVE");
        roleRepository.save(studentRole);

        // 2. Setup Active School
        activeSchool = new Tenant();
        activeSchool.setName("Super Driving School");
        activeSchool.setStatus("ACTIVE");
        activeSchool.setContactEmail("contact@superdriving.com");
        activeSchool = tenantRepository.save(activeSchool);

        // 3. Setup Admin User
        adminUser = new User();
        adminUser.setEmail("admin@superdriving.com");
        adminUser.setPassword("hashedpassword123");
        adminUser.setStatus("active");
        adminUser.setTenant(activeSchool);
        adminUser.getRoles().add(adminRole);
        adminUser = userRepository.save(adminUser);

        adminToken = jwtService.generateToken(adminUser);
    }

    @Test
    public void testFullHybridStudentEnrollmentFlow() throws Exception {
        // ---------- 1. Student Registers Independently ----------
        String requestBody = "{\"email\": \"independent@student.com\", \"password\": \"secure123\", \"nom\": \"Independent\", \"prenom\": \"Student\"}";

        MvcResult registerResult = mockMvc.perform(post("/api/auth/student/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        // The response body is just the JWT string directly
        String studentToken = registerResult.getResponse().getContentAsString();
        assertNotNull(studentToken);
        assertFalse(studentToken.isEmpty());

        // Validate DB student creation
        User createdUser = userRepository.findByEmail("independent@student.com").orElseThrow();
        assertNull(createdUser.getTenant());

        Student createdStudent = studentRepository.findByUserId(createdUser.getId()).orElseThrow();
        assertNull(createdStudent.getTenant());

        // ---------- 2. Student Browses Schools ----------
        mockMvc.perform(get("/api/public/schools")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Super Driving School")))
                .andExpect(jsonPath("$[0].id", is(activeSchool.getId().toString())));

        // ---------- 3. Student Applies to School ----------
        mockMvc.perform(post("/api/enrollments/schools/" + activeSchool.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")));

        // ---------- 4. Admin Checks Pending Requests ----------
        MvcResult pendingListResult = mockMvc.perform(get("/api/admin/enrollments")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andReturn();
        
        // Extract Enrollment ID from JSON (Using Jackson roughly assuming simple array)
        ObjectMapper mapper = new ObjectMapper();
        List<?> enrollments = mapper.readValue(pendingListResult.getResponse().getContentAsString(), List.class);
        String enrollmentIdStr = ((java.util.Map<?,?>)enrollments.get(0)).get("id").toString();
        
        // ---------- 5. Admin Accepts Request ----------
        mockMvc.perform(post("/api/admin/enrollments/" + enrollmentIdStr + "/accept")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // ---------- 6. Verify Final State ----------
        User updatedUser = userRepository.findByEmail("independent@student.com").orElseThrow();
        assertNotNull(updatedUser.getTenant());
        assertEquals(activeSchool.getId(), updatedUser.getTenant().getId());

        Student updatedStudent = studentRepository.findByUserId(createdUser.getId()).orElseThrow();
        assertNotNull(updatedStudent.getTenant());
        assertEquals(activeSchool.getId(), updatedStudent.getTenant().getId());
    }
}
