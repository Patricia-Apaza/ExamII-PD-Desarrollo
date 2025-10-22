package pe.edu.upeu.sysasistencia.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pe.edu.upeu.sysasistencia.dtos.ProgramaEstudioDTO;
import pe.edu.upeu.sysasistencia.dtos.UsuarioDTO;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
public class ProgramaEstudioControllerIntegrateTest {

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private String token;
    private String idCreado;

    @BeforeEach
    public void setUp() {
        RestAssured.port = this.port;

        UsuarioDTO.UsuarioCrearDto udto = new UsuarioDTO.UsuarioCrearDto(
                "admin@upeu.edu.pe",
                "Admin123*".toCharArray(),
                "ADMIN",
                "Activo"
        );

        try {
            token = given()
                    .contentType(ContentType.JSON)
                    .body(new UsuarioDTO.CredencialesDto(
                            "admin@upeu.edu.pe",
                            "Admin123*".toCharArray()
                    ))
                    .when().post("/users/login")
                    .andReturn().jsonPath().getString("token");
        } catch (Exception e) {
            if (token == null) {
                token = given()
                        .contentType(ContentType.JSON)
                        .body(udto)
                        .when().post("/users/register")
                        .andReturn().jsonPath().getString("token");
            }
            System.out.println("Token obtenido: " + token);
        }

        idCreado = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/programas/buscarmaxid")
                .then()
                .statusCode(200)
                .extract().body().asString();

        System.out.println("ID máximo actual: " + idCreado);
    }

    @Order(1)
    @Test
    public void testCrearPrograma() throws Exception {
        ProgramaEstudioDTO dto = new ProgramaEstudioDTO();
        dto.setNombre("Ingeniería Mecánica");
        dto.setFacultadId(1L);
        dto.setDescripcion("Programa de ingeniería mecánica");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(objectMapper.writeValueAsString(dto))
                .when()
                .post("/programas")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("nombre", equalTo("Ingeniería Mecánica"));
    }

    @Order(2)
    @Test
    public void testListarProgramas() throws Exception {
        given()
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/programas")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON);
    }

    @Order(3)
    @Test
    void testFindById() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/programas/{id}", idCreado)
                .then()
                .statusCode(200)
                .body("idPrograma", equalTo(Integer.parseInt(idCreado)));
    }

    @Order(4)
    @Test
    void testUpdate() {
        ProgramaEstudioDTO updated = new ProgramaEstudioDTO();
        updated.setNombre("Programa Actualizado");
        updated.setFacultadId(1L);
        updated.setDescripcion("Descripción actualizada del programa");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(updated)
                .when()
                .put("/programas/{id}", idCreado)
                .then()
                .statusCode(200)
                .body("idPrograma", equalTo(Integer.parseInt(idCreado)))
                .body("nombre", equalTo("Programa Actualizado"));
    }

    @Order(5)
    @Test
    void testGetProgramaMaxId() {
        String idMaximo = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/programas/buscarmaxid")
                .then()
                .statusCode(200)
                .extract().body().asString();

        Assertions.assertNotNull(idMaximo);
        System.out.println("ID máximo encontrado: " + idMaximo);
    }

    @Order(6)
    @Test
    void testDelete() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/programas/{id}", idCreado)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("true"));
    }
}
