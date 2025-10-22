package pe.edu.upeu.sysasistencia.control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.edu.upeu.sysasistencia.dtos.ProgramaEstudioDTO;
import pe.edu.upeu.sysasistencia.excepciones.CustomResponse;
import pe.edu.upeu.sysasistencia.mappers.ProgramaEstudioMapper;
import pe.edu.upeu.sysasistencia.modelo.Facultad;
import pe.edu.upeu.sysasistencia.modelo.ProgramaEstudio;
import pe.edu.upeu.sysasistencia.servicio.IProgramaEstudioService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@ExtendWith(MockitoExtension.class)
public class ProgramaEstudioControllerTest {

    @Mock
    private IProgramaEstudioService programaService;

    @Mock
    private ProgramaEstudioMapper programaMapper;

    @InjectMocks
    private ProgramaEstudioController programaController;

    private ProgramaEstudio programa;
    private ProgramaEstudioDTO programaDTO;
    private Facultad facultad;
    private List<ProgramaEstudio> programas;

    private static final Logger logger = Logger.getLogger(ProgramaEstudioControllerTest.class.getName());

    @BeforeEach
    void setUp() {
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
                .descripcion("Programa de sistemas e informática")
                .build();

        // Preparar DTO
        programaDTO = new ProgramaEstudioDTO();
        programaDTO.setIdPrograma(1L);
        programaDTO.setNombre("Ingeniería de Sistemas");
        programaDTO.setFacultadId(1L);
        programaDTO.setFacultadNombre("Facultad de Ingeniería");
        programaDTO.setDescripcion("Programa de sistemas e informática");

        programas = List.of(programa);
    }

    @Test
    public void testFindAll_ReturnsListOfProgramaDTO_WithHttpStatusOK() {
        // Given
        BDDMockito.given(programaService.findAll()).willReturn(programas);
        BDDMockito.given(programaMapper.toDTOs(programas)).willReturn(List.of(programaDTO));

        // When
        ResponseEntity<List<ProgramaEstudioDTO>> response = programaController.findAll();

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(1, response.getBody().size());
        Assertions.assertEquals(programaDTO.getNombre(), response.getBody().get(0).getNombre());

        // Log
        for (ProgramaEstudioDTO p : response.getBody()) {
            logger.info(String.format("ProgramaDTO{id=%d, nombre='%s', facultad='%s'}",
                    p.getIdPrograma(), p.getNombre(), p.getFacultadNombre()));
        }

        BDDMockito.then(programaService).should().findAll();
        BDDMockito.then(programaMapper).should().toDTOs(programas);
    }

    @Test
    void testFindById_ReturnsProgramaDTO_WithHttpStatusOK() {
        // Given
        Long id = 1L;
        BDDMockito.given(programaService.findById(id)).willReturn(programa);
        BDDMockito.given(programaMapper.toDTO(programa)).willReturn(programaDTO);

        // When
        ResponseEntity<ProgramaEstudioDTO> response = programaController.findById(id);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(programaDTO.getNombre(), response.getBody().getNombre());
        Assertions.assertEquals(programaDTO.getFacultadNombre(), response.getBody().getFacultadNombre());

        BDDMockito.then(programaService).should().findById(id);
        BDDMockito.then(programaMapper).should().toDTO(programa);
    }

    @Test
    void testSave_ReturnsCreatedStatusAndProgramaDTO() {
        // Given
        BDDMockito.given(programaMapper.toEntity(programaDTO)).willReturn(programa);
        BDDMockito.given(programaService.save(programa)).willReturn(programa);
        BDDMockito.given(programaMapper.toDTO(programa)).willReturn(programaDTO);

        // When
        ResponseEntity<ProgramaEstudioDTO> response = programaController.save(programaDTO);

        // Then
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(programaDTO.getNombre(), response.getBody().getNombre());

        BDDMockito.then(programaMapper).should().toEntity(programaDTO);
        BDDMockito.then(programaService).should().save(programa);
        BDDMockito.then(programaMapper).should().toDTO(programa);
    }

    @Test
    void testUpdate_ReturnsUpdatedProgramaDTO_WithHttpStatusOK() {
        // Given
        Long id = 1L;
        programaDTO.setIdPrograma(id);
        programaDTO.setNombre("Ingeniería de Sistemas e Informática");

        BDDMockito.given(programaMapper.toEntity(programaDTO)).willReturn(programa);
        BDDMockito.given(programaService.update(id, programa)).willReturn(programa);
        BDDMockito.given(programaMapper.toDTO(programa)).willReturn(programaDTO);

        // When
        ResponseEntity<ProgramaEstudioDTO> response = programaController.update(id, programaDTO);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(programaDTO.getNombre(), response.getBody().getNombre());

        BDDMockito.then(programaMapper).should().toEntity(programaDTO);
        BDDMockito.then(programaService).should().update(id, programa);
        BDDMockito.then(programaMapper).should().toDTO(programa);
    }

    @Test
    void testDelete_ReturnsCustomResponse_WithHttpStatusOK() {
        // Given
        Long id = 1L;
        CustomResponse customResponse = new CustomResponse(
                200,
                LocalDateTime.now(),
                "true",
                "Programa eliminado correctamente"
        );

        BDDMockito.given(programaService.delete(id)).willReturn(customResponse);

        // When
        ResponseEntity<CustomResponse> response = programaController.delete(id);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("true", response.getBody().getMessage());

        BDDMockito.then(programaService).should().delete(id);
    }
}
