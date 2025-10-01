package com.sigret.config;

import com.sigret.entities.*;
import com.sigret.enums.RolUsuario;
import com.sigret.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private TipoPersonaRepository tipoPersonaRepository;

    @Autowired
    private TipoEmpleadoRepository tipoEmpleadoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            crearDatosIniciales();
        }
    }

    private void crearDatosIniciales() {
        // Crear tipos básicos
        TipoDocumento dni = new TipoDocumento();
        dni.setDescripcion("DNI");
        tipoDocumentoRepository.save(dni);

        TipoPersona fisica = new TipoPersona();
        fisica.setDescripcion("Persona Física");
        tipoPersonaRepository.save(fisica);

        TipoEmpleado propietario = new TipoEmpleado();
        propietario.setDescripcion("Propietario");
        tipoEmpleadoRepository.save(propietario);

        TipoEmpleado tecnico = new TipoEmpleado();
        tecnico.setDescripcion("Técnico");
        tipoEmpleadoRepository.save(tecnico);

        // Crear persona admin
        Persona personaAdmin = new Persona();
        personaAdmin.setTipoPersona(fisica);
        personaAdmin.setTipoDocumento(dni);
        personaAdmin.setNombre("Administrador");
        personaAdmin.setApellido("Sistema");
        personaAdmin.setDocumento("12345678");
        personaAdmin.setSexo("M");
        personaRepository.save(personaAdmin);

        // Crear empleado admin
        Empleado empleadoAdmin = new Empleado();
        empleadoAdmin.setTipoEmpleado(propietario);
        empleadoAdmin.setPersona(personaAdmin);
        empleadoAdmin.setActivo(true);
        empleadoRepository.save(empleadoAdmin);

        // Crear usuario admin
        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setEmpleado(empleadoAdmin);
        usuarioAdmin.setUsername("admin");
        usuarioAdmin.setPassword(passwordEncoder.encode("admin123"));
        usuarioAdmin.setRol(RolUsuario.PROPIETARIO);
        usuarioAdmin.setActivo(true);
        usuarioRepository.save(usuarioAdmin);

        //--------------------------------------------------------------------
        // Crear persona admin
        Persona personaTecnico = new Persona();
        personaTecnico.setTipoPersona(fisica);
        personaTecnico.setTipoDocumento(dni);
        personaTecnico.setNombre("Tecnico");
        personaTecnico.setApellido("Sistema");
        personaTecnico.setDocumento("41847359");
        personaTecnico.setSexo("M");
        personaRepository.save(personaTecnico);

        // Crear empleado admin
        Empleado empleadoTecnico = new Empleado();
        empleadoTecnico.setTipoEmpleado(tecnico);
        empleadoTecnico.setPersona(personaTecnico);
        empleadoTecnico.setActivo(true);
        empleadoRepository.save(empleadoTecnico);

        // Crear usuario admin
        Usuario usuarioTecnico = new Usuario();
        usuarioTecnico.setEmpleado(empleadoTecnico);
        usuarioTecnico.setUsername("tecnico");
        usuarioTecnico.setPassword(passwordEncoder.encode("tecnico123"));
        usuarioTecnico.setRol(RolUsuario.TECNICO);
        usuarioTecnico.setActivo(true);
        usuarioRepository.save(usuarioTecnico);

        System.out.println("Usuario admin creado:");
        System.out.println("Username: admin");
        System.out.println("Password: admin123");
        System.out.println("Usuario tecnico creado:");
        System.out.println("Username: tecnico");
        System.out.println("Password: tecnico123");

    }
}
