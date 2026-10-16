package com.storemaster;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StoreMasterIntegrationTest {

    private static final String AUTH = "Authorization";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;


    @Test
    void protectedEndpointsRejectAnonymousRequests() throws Exception {
        mvc.perform(get("/api/stores").param("province", "Aceh")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/branches")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/whitelist")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/stores").param("province", "Aceh").header(AUTH, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchReturnsProvinceStoresPlusWhitelistedStoresFromOtherProvinces() throws Exception {
        long aceh = insertBranch("Aceh", "BR-A");
        long bali = insertBranch("Bali", "BR-B");
        insertStore(aceh, "ST-A1");
        insertStore(bali, "ST-B1");
        long b2 = insertStore(bali, "ST-B2");
        String token = login();

        mvc.perform(post("/api/whitelist").header(AUTH, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeId\":" + b2 + ",\"note\":\"flagship\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/stores").param("province", "aceh").header(AUTH, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[*].code", containsInAnyOrder("ST-A1", "ST-B2")));

        mvc.perform(get("/api/stores").param("province", "Aceh").param("size", "1").header(AUTH, bearer(token)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    void searchRequiresProvinceParameter() throws Exception {
        mvc.perform(get("/api/stores").header(AUTH, bearer(login()))).andExpect(status().isBadRequest());
    }

    @Test
    void deletedBranchAndItsStoresDisappearFromAllResponses() throws Exception {
        long aceh = insertBranch("Aceh", "BR-A");
        insertStore(aceh, "ST-A1");
        String token = login();

        mvc.perform(delete("/api/branches/" + aceh).header(AUTH, bearer(token))).andExpect(status().isNoContent());

        mvc.perform(get("/api/stores").param("province", "Aceh").header(AUTH, bearer(token)))
                .andExpect(jsonPath("$.content", hasSize(0)));
        mvc.perform(get("/api/branches/" + aceh).header(AUTH, bearer(token))).andExpect(status().isNotFound());
    }

    @Test
    void updatingBranchWithStaleVersionIsRejected() throws Exception {
        long branch = insertBranch("Aceh", "BR-A");
        long provinceId = provinceId("Aceh");
        String token = login();
        String body = "{\"code\":\"BR-A2\",\"name\":\"Baru\",\"provinceId\":" + provinceId + ",\"version\":0}";

        mvc.perform(put("/api/branches/" + branch).header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("BR-A2")))
                .andExpect(jsonPath("$.version", is(1)));

        mvc.perform(put("/api/branches/" + branch).header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void whitelistSupportsCreateUpdateDeleteAndRejectsDuplicates() throws Exception {
        long bali = insertBranch("Bali", "BR-B");
        long store = insertStore(bali, "ST-B1");
        String token = login();

        String created = mvc.perform(post("/api/whitelist").header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeId\":" + store + ",\"note\":\"a\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(created).get("id").asLong();

        mvc.perform(post("/api/whitelist").header(AUTH, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeId\":" + store + "}"))
                .andExpect(status().isConflict());

        mvc.perform(put("/api/whitelist/" + id).header(AUTH, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeId\":" + store + ",\"note\":\"b\",\"version\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note", is("b")));

        mvc.perform(delete("/api/whitelist/" + id).header(AUTH, bearer(token))).andExpect(status().isNoContent());

        mvc.perform(get("/api/stores").param("province", "Aceh").header(AUTH, bearer(token)))
                .andExpect(jsonPath("$.content", hasSize(0)));
        mvc.perform(get("/api/whitelist").header(AUTH, bearer(token)))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void changesAreAuditedAndAuditLogIsAppendOnly() throws Exception {
        long branch = insertBranch("Aceh", "BR-A");
        long provinceId = provinceId("Aceh");
        String token = login();

        mvc.perform(put("/api/branches/" + branch).header(AUTH, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"BR-A\",\"name\":\"Renamed\",\"provinceId\":" + provinceId + ",\"version\":0}"));

        Integer logged = jdbc.queryForObject("""
                select count(*) from audit_log
                where entity_type = 'BRANCH' and entity_id = ? and action = 'UPDATE' and username = 'admin'""",
                Integer.class, branch);
        assertEquals(1, logged);

        mvc.perform(get("/api/audit-logs").param("entityType", "branch").param("entityId", String.valueOf(branch))
                        .header(AUTH, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        assertThrows(DataAccessException.class, () -> jdbc.update("update audit_log set username = 'hacker'"));
    }

    private String login() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("accessToken").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private long provinceId(String name) {
        return jdbc.queryForObject("select id from province where name = ?", Long.class, name);
    }

    private long insertBranch(String province, String code) {
        jdbc.update("""
                insert into branch (province_id, code, name, created_at, created_by, version)
                values ((select id from province where name = ?), ?, ?, UTC_TIMESTAMP(6), 'test', 0)""",
                province, code, "Cabang " + code);
        return jdbc.queryForObject("select id from branch where code = ?", Long.class, code);
    }

    private long insertStore(long branchId, String code) {
        jdbc.update("""
                insert into store (branch_id, code, name, created_at, created_by, version)
                values (?, ?, ?, UTC_TIMESTAMP(6), 'test', 0)""", branchId, code, "Toko " + code);
        return jdbc.queryForObject("select id from store where code = ?", Long.class, code);
    }
}
