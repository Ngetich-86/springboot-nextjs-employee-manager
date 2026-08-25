package com.pesira.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesira.backend.AbstractIntegrationTest;
import com.pesira.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = loginAndExtractToken("admin@example.com", "admin123");
        userToken = loginAndExtractToken("user@example.com", "user123");
    }

    @Test
    void createEmployeeRequiresAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmployeeJson("jane.doe@example.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void adminCanCreateReadUpdateDeleteEmployee() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmployeeJson("employee@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("employee@example.com"))
                .andReturn();

        Long employeeId = extractId(createResult);

        mockMvc.perform(get("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Jane"));

        mockMvc.perform(put("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Janet",
                                  "lastName": "Doe",
                                  "email": "employee@example.com",
                                  "department": "Engineering",
                                  "position": "Senior Engineer",
                                  "salary": 95000.00,
                                  "hireDate": "2024-03-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Janet"));

        mockMvc.perform(delete("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void listEmployeesSupportsPaginationSortingAndSearch() throws Exception {
        createEmployee("alpha@example.com", "Alpha", "One");
        createEmployee("beta@example.com", "Beta", "Two");

        mockMvc.perform(get("/api/v1/employees")
                        .header("Authorization", "Bearer " + userToken)
                        .param("search", "Alpha")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "lastName")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].lastName").value("One"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void exportEndpointsReturnDownloadableFiles() throws Exception {
        createEmployee("export@example.com", "Export", "User");

        mockMvc.perform(get("/api/v1/employees/export/csv")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=employees.csv"));

        mockMvc.perform(get("/api/v1/employees/export/json")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=employees.json"));
    }

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void adminCanPromoteUserRole() throws Exception {
        Long userId = userRepository.findByEmail("user@example.com")
                .orElseThrow()
                .getId();

        mockMvc.perform(patch("/api/v1/users/" + userId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        mockMvc.perform(patch("/api/v1/users/" + userId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "USER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    private void createEmployee(String email, String firstName, String lastName) throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "firstName": "%s",
                                  "lastName": "%s",
                                  "email": "%s",
                                  "department": "Engineering",
                                  "position": "Engineer",
                                  "salary": 80000.00,
                                  "hireDate": "2024-01-15"
                                }
                                """, firstName, lastName, email)))
                .andExpect(status().isCreated());
    }

    private String validEmployeeJson(String email) {
        return String.format("""
                {
                  "firstName": "Jane",
                  "lastName": "Doe",
                  "email": "%s",
                  "department": "Engineering",
                  "position": "Engineer",
                  "salary": 85000.00,
                  "hireDate": "2024-01-15"
                }
                """, email);
    }

    private String loginAndExtractToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """, email, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = root.path("data").path("accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }
}
