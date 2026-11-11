package com.example.restservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class EmployeeApiTest {

    private static final String NEW_EMPLOYEE_JSON = """
            {
              "employee_id": "E2001",
              "first_name": "Linus",
              "last_name": "Torvalds",
              "email": "linus.torvalds@example.com",
              "title": "Staff Engineer"
            }
            """;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EmployeeController(new EmployeeManager()))
                .build();
    }

    @Test
    void getReturnsEmployeesInRequiredJsonFormat() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Employees.length()").value(3))
                .andExpect(jsonPath("$.Employees[0].employee_id").value("E1001"))
                .andExpect(jsonPath("$.Employees[0].first_name").value("Ada"))
                .andExpect(jsonPath("$.Employees[0].last_name").value("Lovelace"))
                .andExpect(jsonPath("$.Employees[0].email").value("ada.lovelace@example.com"))
                .andExpect(jsonPath("$.Employees[0].title").value("Software Engineer"))
                .andExpect(jsonPath("$.Employees[0].firstName").doesNotExist());
    }

    @Test
    void postAddsEmployeeAndReturns201() throws Exception {
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_EMPLOYEE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employee_id").value("E2001"))
                .andExpect(jsonPath("$.first_name").value("Linus"));

        mockMvc.perform(get("/employees"))
                .andExpect(jsonPath("$.Employees.length()").value(4))
                .andExpect(jsonPath("$.Employees[3].employee_id").value("E2001"));
    }

    @Test
    void postWithExistingIdReturns409() throws Exception {
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_EMPLOYEE_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_EMPLOYEE_JSON))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/employees"))
                .andExpect(jsonPath("$.Employees.length()").value(4));
    }
}