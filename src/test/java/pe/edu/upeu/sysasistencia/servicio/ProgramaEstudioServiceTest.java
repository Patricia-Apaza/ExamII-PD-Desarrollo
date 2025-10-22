package pe.edu.upeu.sysasistencia.servicio;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.sysasistencia.excepciones.CustomResponse;
import pe.edu.upeu.sysasistencia.excepciones.ModelNotFoundException;
import pe.edu.upeu.sysasistencia.modelo.Facultad;
import pe.edu.upeu.sysasistencia.modelo.ProgramaEstudio;
import pe.edu.upeu.sysasistencia.repositorio.IProgramaEstudioRepository;
import pe.edu.upeu.sysasistencia.servicio.impl.ProgramaEstudioServiceImp;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProgramaEstudioServiceTest {

    @Mock
    private IProgramaEstudioRepository repo;

    @InjectMocks
    private ProgramaEstudioServiceImp programaService;

    private ProgramaEstudio programa;
    private Facultad facultad;

    @BeforeEach
    public void setUp() {
        // Preparar Facultad
        facultad = Facultad.builder()
                .idFacultad(1L)
                .nombre("Facultad de Ingeniería")
                .descripcion("Facultad de ingeniería y arquitectura")
                .build();

        // Preparar ProgramaEstudio
        programa = ProgramaEstudio.builder()
                .idPrograma(1L)
                .nombre("Ingeniería de Sistemas")
                .facultad(facultad)
                .descripcion("Programa de sistemas")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Guardar Programa de Estudio")
    public void testSavePrograma() {
        // Given
        given(repo.save(programa)).willReturn(programa);

        // When
        ProgramaEstudio guardado = programaService.save(programa);

        // Then
        Assertions.assertThat(guardado).isNotNull();
        Assertions.assertThat(guardado.getNombre()).isEqualTo(programa.getNombre());
        Assertions.assertThat(guardado.getFacultad()).isNotNull();
        Assertions.assertThat(guardado.getFacultad().getNombre()).isEqualTo("Facultad de Ingeniería");
    }

    @Test
    @Order(2)
    @DisplayName("Listar Programas de Estudio")
    public void testListProgramas() {
        // Given
        ProgramaEstudio programa2 = ProgramaEstudio.builder()
                .idPrograma(2L)
                .nombre("Ingeniería Civil")
                .facultad(facultad)
                .descripcion("Programa de civil")
                .build();

        given(repo.findAll()).willReturn(List.of(programa, programa2));

        // When
        List<ProgramaEstudio> programas = programaService.findAll();

        // Then
        Assertions.assertThat(programas).hasSize(2);
        Assertions.assertThat(programas.get(0).getNombre()).isEqualTo("Ingeniería de Sistemas");
        Assertions.assertThat(programas.get(1).getNombre()).isEqualTo("Ingeniería Civil");

        // Imprimir resultados
        for (ProgramaEstudio p : programas) {
            System.out.println(p.getNombre() + " - " + p.getFacultad().getNombre());
        }
    }

    @Test
    @Order(3)
    @DisplayName("Buscar Programa por ID")
    public void testFindById() {
        // Given
        given(repo.findById(1L)).willReturn(Optional.of(programa));

        // When
        ProgramaEstudio encontrado = programaService.findById(1L);

        // Then
        Assertions.assertThat(encontrado).isNotNull();
        Assertions.assertThat(encontrado.getIdPrograma()).isEqualTo(1L);
        Assertions.assertThat(encontrado.getNombre()).isEqualTo("Ingeniería de Sistemas");
    }

    @Test
    @Order(4)
    @DisplayName("Buscar Programa por Nombre")
    public void testFindByNombre() {
        // Given
        given(repo.findByNombre("Ingeniería de Sistemas")).willReturn(Optional.of(programa));

        // When
        Optional<ProgramaEstudio> encontrado = programaService.findByNombre("Ingeniería de Sistemas");

        // Then
        Assertions.assertThat(encontrado).isPresent();
        Assertions.assertThat(encontrado.get().getNombre()).isEqualTo("Ingeniería de Sistemas");
    }

    @Test
    @Order(5)
    @DisplayName("Actualizar Programa de Estudio")
    public void testUpdatePrograma() {
        // Given
        given(repo.findById(1L)).willReturn(Optional.of(programa));
        given(repo.save(programa)).willReturn(programa);

        // When
        programa.setNombre("Ingeniería de Sistemas e Informática");
        programa.setDescripcion("Descripción actualizada");
        ProgramaEstudio actualizado = programaService.update(1L, programa);

        // Then
        System.out.println("Nombre actualizado: " + actualizado.getNombre());
        Assertions.assertThat(actualizado.getNombre()).isEqualTo("Ingeniería de Sistemas e Informática");
        Assertions.assertThat(actualizado.getDescripcion()).isEqualTo("Descripción actualizada");
    }

    @Test
    @Order(6)
    @DisplayName("Eliminar Programa de Estudio")
    public void testDeletePrograma() {
        // Given
        given(repo.findById(1L)).willReturn(Optional.of(programa));
        willDoNothing().given(repo).deleteById(1L);

        // When
        CustomResponse respuesta = programaService.delete(1L);

        // Then
        System.out.println("Mensaje: " + respuesta.getMessage());
        Assertions.assertThat(respuesta.getMessage()).isEqualTo("true");
        verify(repo, times(1)).deleteById(1L);
    }

    @Test
    @Order(7)
    @DisplayName("Eliminar Programa - ID no Existe")
    public void testDeleteByIdNonExistent() {
        // Given
        Long idInexistente = 99L;
        given(repo.findById(idInexistente)).willReturn(Optional.empty());

        // When & Then
        Assertions.assertThatThrownBy(() -> programaService.delete(idInexistente))
                .isInstanceOf(ModelNotFoundException.class)
                .hasMessageContaining("ID NOT FOUND: " + idInexistente);
    }
}
