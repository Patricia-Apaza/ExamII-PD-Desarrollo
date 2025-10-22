package pe.edu.upeu.sysasistencia.repositorio;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.upeu.sysasistencia.modelo.Facultad;
import pe.edu.upeu.sysasistencia.modelo.ProgramaEstudio;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class IProgramaEstudioRepositoryTest {

    @Autowired
    private IProgramaEstudioRepository programaRepository;

    @Autowired
    private IFacultadRepository facultadRepository;

    private static Long programaId;
    private static Long facultadId;

    @BeforeEach
    public void setUp() {
        // Primero crear una Facultad para la relación
        Facultad facultad = new Facultad();
        facultad.setNombre("Facultad de Ingeniería y Arquitectura");
        facultad.setDescripcion("Facultad de Ingeniería");
        Facultad facultadGuardada = facultadRepository.save(facultad);
        facultadId = facultadGuardada.getIdFacultad();

        // Crear un ProgramaEstudio
        ProgramaEstudio programa = new ProgramaEstudio();
        programa.setNombre("Ingeniería de Sistemas");
        programa.setFacultad(facultadGuardada);
        programa.setDescripcion("Programa de sistemas e informática");
        ProgramaEstudio guardado = programaRepository.save(programa);
        programaId = guardado.getIdPrograma();
    }

    @Test
    @Order(1)
    public void testGuardarPrograma() {
        Facultad facultad = facultadRepository.findById(facultadId).orElseThrow();
        ProgramaEstudio nuevoPrograma = new ProgramaEstudio();
        nuevoPrograma.setNombre("Ingeniería Civil");
        nuevoPrograma.setFacultad(facultad);
        nuevoPrograma.setDescripcion("Programa de construcción civil");

        ProgramaEstudio guardado = programaRepository.save(nuevoPrograma);

        assertNotNull(guardado.getIdPrograma());
        assertEquals("Ingeniería Civil", guardado.getNombre());
        assertNotNull(guardado.getFacultad());
        assertEquals(facultadId, guardado.getFacultad().getIdFacultad());
    }

    @Test
    @Order(2)
    public void testBuscarPorId() {
        Optional<ProgramaEstudio> programa = programaRepository.findById(programaId);

        assertTrue(programa.isPresent());
        assertEquals("Ingeniería de Sistemas", programa.get().getNombre());
        assertNotNull(programa.get().getFacultad());
    }

    @Test
    @Order(3)
    public void testBuscarPorNombre() {
        Optional<ProgramaEstudio> programa = programaRepository.findByNombre("Ingeniería de Sistemas");

        assertTrue(programa.isPresent());
        assertEquals(programaId, programa.get().getIdPrograma());
        assertEquals("Ingeniería de Sistemas", programa.get().getNombre());
    }

    @Test
    @Order(4)
    public void testExistsByNombre() {
        boolean existe = programaRepository.existsByNombre("Ingeniería de Sistemas");
        boolean noExiste = programaRepository.existsByNombre("Medicina Humana");

        assertTrue(existe);
        assertFalse(noExiste);
    }

    @Test
    @Order(5)
    public void testActualizarPrograma() {
        ProgramaEstudio programa = programaRepository.findById(programaId).orElseThrow();
        programa.setNombre("Ingeniería de Sistemas e Informática");
        programa.setDescripcion("Descripción actualizada");

        ProgramaEstudio actualizado = programaRepository.save(programa);

        assertEquals("Ingeniería de Sistemas e Informática", actualizado.getNombre());
        assertEquals("Descripción actualizada", actualizado.getDescripcion());
    }

    @Test
    @Order(6)
    public void testListarProgramas() {
        List<ProgramaEstudio> programas = programaRepository.findAll();

        assertFalse(programas.isEmpty());
        System.out.println("Total programas registrados: " + programas.size());
        for (ProgramaEstudio p : programas) {
            System.out.println(p.getNombre() + "\t" + p.getIdPrograma() + "\t" + p.getFacultad().getNombre());
        }
    }

    @Test
    @Order(7)
    public void testEliminarPrograma() {
        programaRepository.deleteById(programaId);
        Optional<ProgramaEstudio> eliminado = programaRepository.findById(programaId);

        assertFalse(eliminado.isPresent(), "El programa debería haber sido eliminado");
    }
}
