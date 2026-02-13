package com.workintech.s19d2;

import com.workintech.s19d2.dto.RegisterResponse;
import com.workintech.s19d2.dto.RegistrationMember;
import com.workintech.s19d2.entity.Account;
import com.workintech.s19d2.entity.Member;
import com.workintech.s19d2.entity.Role;
import com.workintech.s19d2.repository.AccountRepository;
import com.workintech.s19d2.repository.MemberRepository;
import com.workintech.s19d2.repository.RoleRepository;
import com.workintech.s19d2.service.AccountServiceImpl;
import com.workintech.s19d2.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@ExtendWith(ResultAnalyzer.class)
class MainTest {

    private Member member;
    private Role roleUser;
    private Role roleAdmin;
    private Role role;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AccountServiceImpl accountService;
    private Account account;
    private AuthenticationService authenticationService;

    @Mock
    private AccountRepository mockAccountRepository;

    @Mock
    private MemberRepository mockMemberRepository;

    @Mock
    private RoleRepository mockRoleRepository;

    @BeforeEach
    void setUp() {
        member = new Member();
        roleUser = new Role();
        roleAdmin = new Role();

        roleUser.setId(1L);
        roleUser.setAuthority("USER");
        roleAdmin.setId(2L);
        roleAdmin.setAuthority("ADMIN");

        member.setEmail("test@example.com");
        member.setPassword("password");
        member.setRoles(Arrays.asList(roleUser, roleAdmin));

        role = new Role();
        role.setId(1L);
        role.setAuthority("USER");

        account = new Account();
        account.setId(1L);
        account.setName("Test Account");

        accountService = new AccountServiceImpl(mockAccountRepository);
        authenticationService = new AuthenticationService(mockMemberRepository, mockRoleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Member Setters and Getters")
    void memberSettersAndGetters() {
        assertEquals("test@example.com", member.getEmail(), "The email should match the one set.");
        assertEquals("password", member.getPassword(), "The password should match the one set.");
    }

    @Test
    @DisplayName("Member Authorities")
    void memberAuthorities() {
        Collection<? extends GrantedAuthority> authorities = member.getAuthorities();

        assertNotNull(authorities, "Authorities collection should not be null.");
        assertEquals(2, authorities.size(), "There should be two roles assigned to the member.");
        assertTrue(authorities.containsAll(Arrays.asList(roleUser, roleAdmin)), "The authorities should include USER and ADMIN roles.");
    }

    @Test
    @DisplayName("UserDetails Implementations")
    void userDetailsImplementations() {
        assertEquals(member.getEmail(), member.getUsername(), "The username should be the same as the email.");
        assertTrue(member.isAccountNonExpired(), "The account should not be expired.");
        assertTrue(member.isAccountNonLocked(), "The account should not be locked.");
        assertTrue(member.isCredentialsNonExpired(), "The credentials should not be expired.");
        assertTrue(member.isEnabled(), "The account should be enabled.");
    }

    @Test
    void testAccountSettersAndGetters() {
        Long expectedId = 1L;
        String expectedName = "Test Account";

        Account account = new Account();
        account.setId(expectedId);
        account.setName(expectedName);

        assertEquals(expectedId, account.getId(), "The ID returned was not the same value set.");
        assertEquals(expectedName, account.getName(), "The name returned was not the same value set.");
    }

    @Test
    @DisplayName("Role Setters and Getters")
    void roleSettersAndGetters() {
        assertEquals(1L, role.getId(), "The id should match the one set.");
        assertEquals("USER", role.getAuthority(), "The authority should match the one set.");
    }

    @Test
    @DisplayName("GrantedAuthority Implementation")
    void grantedAuthorityImplementation() {
        String authority = role.getAuthority();

        assertNotNull(authority, "Authority should not be null.");
        assertEquals("USER", authority, "The authority should return 'USER'.");
    }

    @Test
    @DisplayName("AccountRepository should be instance of JpaRepository")
    void accountRepositoryInstanceCheck() {
        assertTrue(mockAccountRepository instanceof JpaRepository, "AccountRepository should be an instance of JpaRepository");
    }

    @Test
    @DisplayName("MemberRepository should be instance of JpaRepository")
    void memberRepositoryInstanceCheck() {
        assertTrue(mockMemberRepository instanceof JpaRepository, "MemberRepository should be an instance of JpaRepository");
    }

    @Test
    @DisplayName("RoleRepository should be instance of JpaRepository")
    void roleRepositoryInstanceCheck() {
        assertTrue(mockRoleRepository instanceof JpaRepository, "RoleRepository should be an instance of JpaRepository");
    }

    @Test
    @DisplayName("RegisterResponse Data Storage")
    void registerResponseDataStorage() {
        String email = "test@example.com";
        String message = "Registration successful";

        RegisterResponse response = new RegisterResponse(email, message);

        assertEquals(email, response.email(), "Email should match the one provided");
        assertEquals(message, response.message(), "Message should match the one provided");
    }

    @Test
    @DisplayName("RegistrationMember Data Storage")
    void registrationMemberDataStorage() {
        String email = "user@example.com";
        String password = "securePassword123";

        RegistrationMember registrationMember = new RegistrationMember(email, password);

        assertEquals(email, registrationMember.email(), "Email should match the one provided");
        assertEquals(password, registrationMember.password(), "Password should match the one provided");
    }

    @Test
    @DisplayName("Find All Accounts")
    void findAll() {
        given(mockAccountRepository.findAll()).willReturn(Arrays.asList(account));

        List<Account> accounts = accountService.findAll();

        assertThat(accounts).isNotNull();
        assertThat(accounts.size()).isEqualTo(1);
        assertThat(accounts.get(0)).isEqualTo(account);
    }

    @Test
    @DisplayName("Save Account")
    void save() {
        when(mockAccountRepository.save(account)).thenReturn(account);

        Account savedAccount = accountService.save(account);

        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getId()).isEqualTo(account.getId());
        verify(mockAccountRepository).save(account);
    }

    @Test
    @DisplayName("Register New Member Successfully")
    void registerNewMemberSuccessfully() {
        given(mockMemberRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setAuthority("USER");
        given(mockRoleRepository.findByAuthority("USER")).willReturn(Optional.of(userRole));

        Member savedMember = new Member();
        savedMember.setEmail("test@example.com");
        savedMember.setPassword("encodedPassword");
        savedMember.setRoles(List.of(userRole));

        given(mockMemberRepository.save(any(Member.class))).willReturn(savedMember);

        Member registeredMember = authenticationService.register("test@example.com", "password");

        assertThat(registeredMember.getEmail()).isEqualTo("test@example.com");
        assertThat(registeredMember.getPassword()).isEqualTo("encodedPassword");
        verify(mockMemberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("Register Member Throws Exception When Email Exists")
    void registerMemberThrowsExceptionWhenEmailExists() {
        given(mockMemberRepository.findByEmail(anyString())).willReturn(Optional.of(member));

        assertThatThrownBy(() -> authenticationService.register("test@example.com", "password"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User with given email already exist");
    }
}