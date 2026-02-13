package com.workintech.s19d2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workintech.s19d2.controller.AccountController;
import com.workintech.s19d2.controller.AuthController;
import com.workintech.s19d2.dto.RegistrationMember;
import com.workintech.s19d2.entity.Account;
import com.workintech.s19d2.entity.Member;
import com.workintech.s19d2.service.AccountService;
import com.workintech.s19d2.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(ResultAnalyzer2.class)
class ControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Account account;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        account = new Account();
        account.setId(1L);
        account.setName("Sample Account");
    }

    @Test
    @DisplayName("Find All Accounts")
    @WithMockUser(username = "admin", roles = {"ADMIN"})  // authorities yerine roles kullan
    void findAll() throws Exception {
        List<Account> accounts = Arrays.asList(account);
        given(accountService.findAll()).willReturn(accounts);

        mockMvc.perform(get("/account"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(account.getName())));
    }

    @Test
    @DisplayName("Save Account")
    @WithMockUser(username = "admin", roles = {"ADMIN"})  // authorities yerine roles kullan
    void saveAccount() throws Exception {
        given(accountService.save(account)).willReturn(account);

        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(account.getName())));
    }

    @Test
    @DisplayName("Register endpoint creates a new member")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void registerCreatesNewMember() throws Exception {
        RegistrationMember registrationMember = new RegistrationMember("test@example.com", "password123");
        Member createdMember = new Member();
        createdMember.setEmail(registrationMember.email());

        given(authenticationService.register(any(String.class), any(String.class))).willReturn(createdMember);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationMember)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.message", is("kayıt başarılı bir şekilde gerçekleşti.")));
    }

    @Test
    void accessPermittedEndpointsWithoutAuthentication() throws Exception {
        RegistrationMember registrationMember = new RegistrationMember("test@example.com", "password123");
        Member member = new Member();
        member.setId(1L);
        member.setEmail("test@example.com");
        member.setPassword("password123");

        when(authenticationService.register(any(), any())).thenReturn(member);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationMember)))
                .andExpect(status().isOk());
    }

    @Test
    void accessSecuredEndpointsWithoutAuthenticationShouldFail() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void accessSecuredEndpointsWithProperRoleShouldSucceed() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void accessSecuredEndpointsWithImproperRoleShouldFail() throws Exception {
        mockMvc.perform(post("/account"))
                .andExpect(status().isForbidden());
    }
}