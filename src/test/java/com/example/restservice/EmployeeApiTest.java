package com.example.restservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
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

    private static final String UPDATED_ADA_JSON = """
        {
          "employee_id": "E1001",
          "first_name": "Ada",
          "last_name": "Lovelace",
          "email": "ada.lovelace@example.com",
          "title": "Principal Engineer"
        }
        """;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private ResultActions postJson(String json) throws Exception {
        return mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions putJson(String employeeId, String json) throws Exception {
        return mockMvc.perform(put("/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
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
                .andExpect(header().string("Location", "/employees/E2001"))
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

    @Test
    void getByIdReturnsSingleEmployee() throws Exception {
        mockMvc.perform(get("/employees/E1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_id").value("E1001"))
                .andExpect(jsonPath("$.first_name").value("Ada"));
    }

    @Test
    void getByIdReturns404ForUnknownId() throws Exception {
        mockMvc.perform(get("/employees/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postWithMissingFirstNameReturns400() throws Exception {
        postJson("""
            {
              "employee_id": "E3001",
              "last_name": "Doe",
              "email": "jane.doe@example.com",
              "title": "Engineer"
            }
            """)
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/employees"))
                .andExpect(jsonPath("$.Employees.length()").value(3));
    }

    @Test
    void postWithInvalidEmailReturns400() throws Exception {
        postJson("""
            {
              "employee_id": "E3001",
              "first_name": "Jane",
              "last_name": "Doe",
              "email": "not-an-email",
              "title": "Engineer"
            }
            """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void postWithMalformedJsonReturns400() throws Exception {
        postJson("{ this is not json")
                .andExpect(status().isBadRequest());
    }

    @Test
    void putUpdatesExistingEmployee() throws Exception {
        putJson("E1001", UPDATED_ADA_JSON)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Principal Engineer"));

        mockMvc.perform(get("/employees/E1001"))
                .andExpect(jsonPath("$.title").value("Principal Engineer"));
    }

    @Test
    void putReturns404ForUnknownId() throws Exception {
        putJson("E2001", NEW_EMPLOYEE_JSON)
                .andExpect(status().isNotFound());
    }

    @Test
    void putReturns400WhenUrlAndBodyIdsDiffer() throws Exception {
        putJson("E1002", UPDATED_ADA_JSON)
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/employees/E1002"))
                .andExpect(jsonPath("$.first_name").value("Alan"));
    }

    @Test
    void deleteHidesEmployeeFromSubsequentRequests() throws Exception {
        mockMvc.perform(delete("/employees/E1001"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/employees"))
                .andExpect(jsonPath("$.Employees.length()").value(2));
        mockMvc.perform(get("/employees/E1001"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/employees/E1001"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns404ForUnknownId() throws Exception {
        mockMvc.perform(delete("/employees/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postWithDeletedEmployeeIdReturns409() throws Exception {
        postJson(NEW_EMPLOYEE_JSON).andExpect(status().isCreated());
        mockMvc.perform(delete("/employees/E2001"))
                .andExpect(status().isNoContent());

        postJson(NEW_EMPLOYEE_JSON).andExpect(status().isConflict());
    }

    @Test
    void activeFlagIsNotExposedInJson() throws Exception {
        mockMvc.perform(get("/employees/E1001"))
                .andExpect(jsonPath("$.active").doesNotExist());
    }
}