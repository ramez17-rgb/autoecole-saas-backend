package com.autoecole.saas.auth;

import com.autoecole.saas.security.JwtService;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.student.StudentStatus;
import com.autoecole.saas.user.RoleEntity;
import com.autoecole.saas.user.RoleRepository;
import com.autoecole.saas.user.User;
import com.autoecole.saas.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/student")
public class StudentAuthController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public StudentAuthController(
            UserRepository userRepository,
            StudentRepository studentRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Transactional
    public String register(@RequestBody StudentRegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        
        RoleEntity role = roleRepository.findByName("ELEVE")
                .orElseGet(() -> roleRepository.save(new RoleEntity("ELEVE")));
        user.getRoles().add(role);
        
        user.setStatus("active");
        // No tenant assigned for independent student
        user.setTenant(null); 
        userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setTenant(null); // No tenant yet
        student.setLevel("DEBUTANT");
        student.setNom(request.nom());
        student.setPrenom(request.prenom());
        student.setStatus(StudentStatus.active);
        studentRepository.save(student);

        return jwtService.generateToken(user);
    }

    public record StudentRegisterRequest(String email, String password, String nom, String prenom) {}
}
