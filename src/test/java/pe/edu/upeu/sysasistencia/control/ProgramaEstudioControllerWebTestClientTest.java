package pe.edu.upeu.sysasistencia.control;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysasistencia.dtos.ProgramaEstudioDTO;
import pe.edu.upeu.sysasistencia.dtos.UsuarioDTO;

import java.util.logging.Level;
import java.util.logging.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ProgramaEstudioControllerWebTestClientTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    private String token;
    Logger logger = Logger.getLogger(ProgramaEstudioControllerWebTestClientTest.class.getName());

    ProgramaEstudioDTO programaDTO;
    Long idx;

    @BeforeEach
    public void setUp() {
        System.out.println("Puerto: " + this.port);

        UsuarioDTO.UsuarioCrearDto udto = new UsuarioDTO.UsuarioCrearDto(
                "admin@upeu.edu.pe",
                "Admin123*".toCharArray(),
                "ADMIN",
                "Activo"
        );

        try {
            var response = webTestClient.post()
                    .uri("/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new UsuarioDTO.CredencialesDto(
                            "admin@upeu.edu.pe",
                            "Admin123*".toCharArray()
                    ))
                    .exchange()
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONObject jsonObj = new JSONObject(response);
            if (jsonObj.length() > 1) {
                token = jsonObj.getString("token") != null ? jsonObj.getString("token") : null;
            }
        } catch (JSONException e) {
            System.out.println("Error en login: " + e.getMessage());
            if (token == null) {
                webTestClient.post()
                        .uri("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(udto)
                        .exchange()
                        .expectStatus().isCreated()
                        .expectBody(String.class)
                        .value(tokenx -> {
                            try {
                                JSONObject jsonObjx = new JSONObject(tokenx);
                                if (jsonObjx.length() > 1) {
                                    token = jsonObjx.getString("token");
                                }
                            } catch (JSONException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        });
            }
        }
    }

    @Test
    @Order(1)
    public void testListarProgramas() {
        System.out.println("Token: " + token);
        webTestClient.get()
                .uri("http://localhost:" + this.port + "/programas")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray();
    }

    @Transactional
    @Test
    @Order(2)
    public void testGuardarPrograma() {
        // Generar nombre único con timestamp
        String nombreUnico = "Ingeniería Ambiental " + System.currentTimeMillis();

        programaDTO = new ProgramaEstudioDTO();
        programaDTO.setNombre(nombreUnico);
        programaDTO.setFacultadId(1L);
        programaDTO.setDescripcion("Programa de ingeniería ambiental");

        try {
            var datoBuscado = webTestClient.post()
                    .uri("http://localhost:" + this.port + "/programas")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(programaDTO)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONObject jsonObj = new JSONObject(datoBuscado);
            if (jsonObj.has("idPrograma")) {
                idx = jsonObj.getLong("idPrograma");
            }

            System.out.println("Programa creado: " + nombreUnico);
        } catch (JSONException e) {
            System.out.println("Error al guardar: " + e);
        }
        System.out.println("ID Creado: " + idx);
    }

    @Transactional
    @Test
    @Order(3)
    public void testActualizarPrograma() {
        ProgramaEstudioDTO programaActualizado = new ProgramaEstudioDTO();
        programaActualizado.setNombre("Ingeniería Ambiental y Recursos Naturales");
        programaActualizado.setFacultadId(1L);
        programaActualizado.setDescripcion("Descripción actualizada");

        Long datoBuscado = webTestClient.get()
                .uri("http://localhost:" + this.port + "/programas/buscarmaxid")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .returnResult()
                .getResponseBody();

        webTestClient.put()
                .uri("http://localhost:" + this.port + "/programas/{id}", datoBuscado)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(programaActualizado)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Ingeniería Ambiental y Recursos Naturales");
    }

    @Test
    @Order(4)
    public void testBuscarPrograma() {
        Long datoBuscado = webTestClient.get()
                .uri("http://localhost:" + this.port + "/programas/buscarmaxid")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .returnResult()
                .getResponseBody();

        webTestClient.get()
                .uri("http://localhost:" + this.port + "/programas/{id}", datoBuscado)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.idPrograma").isEqualTo(datoBuscado);
    }

    @Test
    @Order(5)
    public void testEliminarPrograma() {
        Long datoBuscado = webTestClient.get()
                .uri("http://localhost:" + this.port + "/programas/buscarmaxid")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .returnResult()
                .getResponseBody();

        System.out.println("Eliminar ID: " + datoBuscado);

        webTestClient.delete()
                .uri("http://localhost:" + this.port + "/programas/{id}", datoBuscado)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("true");
    }
}
