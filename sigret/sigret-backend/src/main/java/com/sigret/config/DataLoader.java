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
    private TipoContactoRepository tipoContactoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TipoEquipoRepository tipoEquipoRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private ModeloRepository modeloRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private ClienteEquipoRepository clienteEquipoRepository;

    @Autowired
    private ContactoRepository contactoRepository;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            crearDatosIniciales();
        }
    }

    private void crearDatosIniciales() {
        // Crear tipos de documento
        TipoDocumento dni = new TipoDocumento();
        dni.setDescripcion("DNI");
        tipoDocumentoRepository.save(dni);

        TipoDocumento cuit = new TipoDocumento();
        cuit.setDescripcion("CUIT");
        tipoDocumentoRepository.save(cuit);

        TipoDocumento cuil = new TipoDocumento();
        cuil.setDescripcion("CUIL");
        tipoDocumentoRepository.save(cuil);

        TipoDocumento pasaporte = new TipoDocumento();
        pasaporte.setDescripcion("Pasaporte");
        tipoDocumentoRepository.save(pasaporte);

        // Crear tipos de persona
        TipoPersona fisica = new TipoPersona();
        fisica.setDescripcion("Física");
        tipoPersonaRepository.save(fisica);

        TipoPersona juridica = new TipoPersona();
        juridica.setDescripcion("Jurídica");
        tipoPersonaRepository.save(juridica);

        // Crear tipos de empleado
        TipoEmpleado propietario = new TipoEmpleado();
        propietario.setDescripcion("Propietario");
        tipoEmpleadoRepository.save(propietario);

        TipoEmpleado administrativo = new TipoEmpleado();
        administrativo.setDescripcion("Administrativo");
        tipoEmpleadoRepository.save(administrativo);

        TipoEmpleado tecnico = new TipoEmpleado();
        tecnico.setDescripcion("Técnico");
        tipoEmpleadoRepository.save(tecnico);

        // Crear tipos de contacto
        TipoContacto email = new TipoContacto();
        email.setDescripcion("Email");
        tipoContactoRepository.save(email);

        TipoContacto telefono = new TipoContacto();
        telefono.setDescripcion("Teléfono");
        tipoContactoRepository.save(telefono);

        TipoContacto celular = new TipoContacto();
        celular.setDescripcion("Celular");
        tipoContactoRepository.save(celular);

        TipoContacto whatsapp = new TipoContacto();
        whatsapp.setDescripcion("WhatsApp");
        tipoContactoRepository.save(whatsapp);

        TipoContacto telegram = new TipoContacto();
        telegram.setDescripcion("Telegram");
        tipoContactoRepository.save(telegram);

        TipoContacto fax = new TipoContacto();
        fax.setDescripcion("Fax");
        tipoContactoRepository.save(fax);

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

        // Crear tipos de equipos (electrodomésticos)
        TipoEquipo lavarropas = crearTipoEquipo("Lavarropas");
        TipoEquipo heladera = crearTipoEquipo("Heladera");
        TipoEquipo freezer = crearTipoEquipo("Freezer");
        TipoEquipo cocina = crearTipoEquipo("Cocina");
        TipoEquipo microondas = crearTipoEquipo("Microondas");
        TipoEquipo lavavajillas = crearTipoEquipo("Lavavajillas");
        TipoEquipo secarropas = crearTipoEquipo("Secarropas");
        TipoEquipo aireAcondicionado = crearTipoEquipo("Aire Acondicionado");
        TipoEquipo calefactor = crearTipoEquipo("Calefactor");
        TipoEquipo horno = crearTipoEquipo("Horno");
        TipoEquipo anafe = crearTipoEquipo("Anafe");
        TipoEquipo campana = crearTipoEquipo("Campana");

        // Crear marcas
        Marca whirlpool = crearMarca("Whirlpool");
        Marca samsung = crearMarca("Samsung");
        Marca lg = crearMarca("LG");
        Marca drean = crearMarca("Drean");
        Marca gafa = crearMarca("Gafa");
        Marca philco = crearMarca("Philco");
        Marca electrolux = crearMarca("Electrolux");
        Marca bosch = crearMarca("Bosch");
        Marca ariston = crearMarca("Ariston");
        Marca patrick = crearMarca("Patrick");
        Marca carrier = crearMarca("Carrier");
        Marca surrey = crearMarca("Surrey");

        // Crear modelos para Whirlpool
        Modelo whirlpoolWLF80AB = crearModelo(whirlpool, "WLF80AB");
        Modelo whirlpoolWRM45A = crearModelo(whirlpool, "WRM45A");
        Modelo whirlpoolWCF80A = crearModelo(whirlpool, "WCF80A");
        Modelo whirlpoolWRM54D = crearModelo(whirlpool, "WRM54D");

        // Crear modelos para Samsung
        Modelo samsungWW90T = crearModelo(samsung, "WW90T");
        Modelo samsungRT38K = crearModelo(samsung, "RT38K");
        Modelo samsungDV80T = crearModelo(samsung, "DV80T");
        Modelo samsungAR12 = crearModelo(samsung, "AR12TXHQASINEU");

        // Crear modelos para LG
        Modelo lgF1403RD = crearModelo(lg, "F1403RD");
        Modelo lgGRB429 = crearModelo(lg, "GR-B429GGUA");
        Modelo lgGCL247 = crearModelo(lg, "GC-L247SLUV");
        Modelo lgS4500VR = crearModelo(lg, "S4500VR");

        // Crear modelos para Drean
        Modelo dreanNext812 = crearModelo(drean, "Next 8.12 Eco");
        Modelo dreanNext1006 = crearModelo(drean, "Next 10.06 Eco");
        Modelo dreanConcept505 = crearModelo(drean, "Concept 5.05G");
        Modelo dreanUniBlue = crearModelo(drean, "UniBlue 8.6");

        // Crear modelos para Gafa
        Modelo gafaExcellent = crearModelo(gafa, "Excellent S 8500");
        Modelo gafaMaxia = crearModelo(gafa, "Maxia Plus 8510");
        Modelo gafaPlatinium = crearModelo(gafa, "Platinium S 8550");
        Modelo gafaG1755 = crearModelo(gafa, "G1755AFC");

        // Crear modelos para Philco
        Modelo philcoWMPH10 = crearModelo(philco, "WM-PH10");
        Modelo philcoPHCT25 = crearModelo(philco, "PHCT25");
        Modelo philcoFRPHCE200 = crearModelo(philco, "FR-PHCE200");
        Modelo philcoPHCD250 = crearModelo(philco, "PHCD250");

        // Crear modelos para Electrolux
        crearModelo(electrolux, "EWF10843");
        crearModelo(electrolux, "TW3350");
        crearModelo(electrolux, "DW50X6");
        crearModelo(electrolux, "H17D");

        // Crear modelos para Bosch
        crearModelo(bosch, "WAW325X0SN");
        crearModelo(bosch, "KGN56XIDA");
        crearModelo(bosch, "SMV46MX03E");
        crearModelo(bosch, "HBG675BS1");

        // Crear modelos para Ariston
        crearModelo(ariston, "HLB10");
        crearModelo(ariston, "HBB24DAABC");
        crearModelo(ariston, "FA5844C");
        crearModelo(ariston, "LI660A");

        // Crear modelos para Patrick
        crearModelo(patrick, "CPF2900S");
        crearModelo(patrick, "HPK135M10");
        crearModelo(patrick, "HPK190M00");
        crearModelo(patrick, "WM6K");

        // Crear modelos para Carrier
        crearModelo(carrier, "53HVA1201");
        crearModelo(carrier, "53HVH0181");
        crearModelo(carrier, "53QCE0241");
        crearModelo(carrier, "53HVH0241");

        // Crear modelos para Surrey
        crearModelo(surrey, "553IQV1201");
        crearModelo(surrey, "223TQO1231F");
        crearModelo(surrey, "554AIQ1231F");
        crearModelo(surrey, "331TQO1831F");

        // Crear clientes ficticios con sus equipos
        crearClientesYEquipos(fisica, dni, email, celular, telefono,
                            lavarropas, heladera, microondas, aireAcondicionado, secarropas, cocina,
                            dreanNext812, whirlpoolWRM45A, samsungWW90T, samsungAR12, lgS4500VR,
                            gafaExcellent, samsungDV80T, gafaMaxia, philcoWMPH10, samsungRT38K,
                            lgGRB429, dreanNext1006, lgF1403RD, philcoPHCT25, lgF1403RD,
                            philcoPHCD250, whirlpoolWLF80AB, lgGCL247);

        System.out.println("===========================================");
        System.out.println("DATOS INICIALES CREADOS EXITOSAMENTE");
        System.out.println("===========================================");
        System.out.println("Tipos de Documento: DNI, CUIT, CUIL, Pasaporte");
        System.out.println("Tipos de Persona: Física, Jurídica");
        System.out.println("Tipos de Empleado: Propietario, Administrativo, Técnico");
        System.out.println("Tipos de Contacto: Email, Teléfono, Celular, WhatsApp, Telegram, Fax");
        System.out.println("-------------------------------------------");
        System.out.println("Tipos de Equipos: Lavarropas, Heladera, Freezer, Cocina, Microondas,");
        System.out.println("  Lavavajillas, Secarropas, Aire Acondicionado, Calefactor, Horno, Anafe, Campana");
        System.out.println("-------------------------------------------");
        System.out.println("Marcas: Whirlpool, Samsung, LG, Drean, Gafa, Philco,");
        System.out.println("  Electrolux, Bosch, Ariston, Patrick, Carrier, Surrey");
        System.out.println("  (Total: " + marcaRepository.count() + " marcas con " + modeloRepository.count() + " modelos)");
        System.out.println("-------------------------------------------");
        System.out.println("Clientes creados: " + clienteRepository.count());
        System.out.println("Equipos creados: " + equipoRepository.count());
        System.out.println("===========================================");
        System.out.println("Usuario admin creado:");
        System.out.println("  Username: admin");
        System.out.println("  Password: admin123");
        System.out.println("  Rol: PROPIETARIO");
        System.out.println("-------------------------------------------");
        System.out.println("Usuario tecnico creado:");
        System.out.println("  Username: tecnico");
        System.out.println("  Password: tecnico123");
        System.out.println("  Rol: TECNICO");
        System.out.println("===========================================");

    }

    private TipoEquipo crearTipoEquipo(String descripcion) {
        TipoEquipo tipoEquipo = new TipoEquipo();
        tipoEquipo.setDescripcion(descripcion);
        return tipoEquipoRepository.save(tipoEquipo);
    }

    private Marca crearMarca(String descripcion) {
        Marca marca = new Marca();
        marca.setDescripcion(descripcion);
        return marcaRepository.save(marca);
    }

    private Modelo crearModelo(Marca marca, String descripcion) {
        Modelo modelo = new Modelo();
        modelo.setMarca(marca);
        modelo.setDescripcion(descripcion);
        return modeloRepository.save(modelo);
    }

    private void crearClientesYEquipos(TipoPersona fisica, TipoDocumento dni,
                                       TipoContacto email, TipoContacto celular, TipoContacto telefono,
                                       TipoEquipo lavarropas, TipoEquipo heladera, TipoEquipo microondas,
                                       TipoEquipo aireAcondicionado, TipoEquipo secarropas, TipoEquipo cocina,
                                       Modelo dreanNext812, Modelo whirlpoolWRM45A, Modelo samsungWW90T,
                                       Modelo samsungAR12, Modelo lgS4500VR, Modelo gafaExcellent,
                                       Modelo samsungDV80T, Modelo gafaMaxia, Modelo philcoWMPH10,
                                       Modelo samsungRT38K, Modelo lgGRB429, Modelo dreanNext1006,
                                       Modelo lgF1403RD, Modelo philcoPHCT25, Modelo lgF1403RD2,
                                       Modelo philcoPHCD250, Modelo whirlpoolWLF80AB, Modelo lgGCL247) {

        // Cliente 1: María González
        Persona personaMaria = crearPersona(fisica, dni, "María", "González", "28456789", "F");
        crearContacto(personaMaria, email, "maria.gonzalez@gmail.com");
        crearContacto(personaMaria, celular, "351-6789012");
        Cliente clienteMaria = crearCliente(personaMaria, "Cliente frecuente, prefiere coordinar turnos por la mañana");

        Equipo equipoMariaLava = crearEquipo(lavarropas, dreanNext812, "DRN2023789", "Blanco", "Comprado hace 2 años");
        asociarEquipoCliente(clienteMaria, equipoMariaLava);

        Equipo equipoMariaHela = crearEquipo(heladera, whirlpoolWRM45A, "WHP2022456", "Gris", "No-Frost");
        asociarEquipoCliente(clienteMaria, equipoMariaHela);

        // Cliente 2: Juan Pérez
        Persona personaJuan = crearPersona(fisica, dni, "Juan", "Pérez", "35123456", "M");
        crearContacto(personaJuan, email, "juan.perez@hotmail.com");
        crearContacto(personaJuan, celular, "351-5432109");
        Cliente clienteJuan = crearCliente(personaJuan, "Taller mecánico - varias reparaciones previas");

        Equipo equipoJuanAire = crearEquipo(aireAcondicionado, samsungAR12, "SAM2021334", "Blanco", "3000 frigorías");
        asociarEquipoCliente(clienteJuan, equipoJuanAire);

        Equipo equipoJuanMicro = crearEquipo(microondas, lgS4500VR, "LG2022556", "Negro", "Funciona pero hace ruido");
        asociarEquipoCliente(clienteJuan, equipoJuanMicro);

        // Cliente 3: Ana Martínez
        Persona personaAna = crearPersona(fisica, dni, "Ana", "Martínez", "41987654", "F");
        crearContacto(personaAna, email, "ana.martinez@outlook.com");
        crearContacto(personaAna, telefono, "351-4567890");
        Cliente clienteAna = crearCliente(personaAna, "Recomendada por Juan Pérez");

        Equipo equipoAnaLava = crearEquipo(lavarropas, samsungWW90T, "SAM2023001", "Blanco", "Bajo garantía");
        asociarEquipoCliente(clienteAna, equipoAnaLava);

        // Cliente 4: Roberto Fernández
        Persona personaRoberto = crearPersona(fisica, dni, "Roberto", "Fernández", "32654321", "M");
        crearContacto(personaRoberto, celular, "351-7890123");
        crearContacto(personaRoberto, email, "rfernandez@yahoo.com");
        Cliente clienteRoberto = crearCliente(personaRoberto, "Complejo de departamentos - varios equipos");

        Equipo equipoRobertoHela = crearEquipo(heladera, gafaExcellent, "GAF2020789", "Blanco", "Freezer superior");
        asociarEquipoCliente(clienteRoberto, equipoRobertoHela);

        Equipo equipoRobertoSeca = crearEquipo(secarropas, samsungDV80T, "SAM2022890", "Blanco", "Para uso comercial");
        asociarEquipoCliente(clienteRoberto, equipoRobertoSeca);

        Equipo equipoRobertoCocina = crearEquipo(cocina, gafaMaxia, "GAF2019345", "Gris", "4 hornallas");
        asociarEquipoCliente(clienteRoberto, equipoRobertoCocina);

        // Cliente 5: Laura Sánchez
        Persona personaLaura = crearPersona(fisica, dni, "Laura", "Sánchez", "38765432", "F");
        crearContacto(personaLaura, celular, "351-6543210");
        crearContacto(personaLaura, email, "laura.sanchez@gmail.com");
        Cliente clienteLaura = crearCliente(personaLaura, "Profesora - prefiere atención vespertina");

        Equipo equipoLauraLava = crearEquipo(lavarropas, philcoWMPH10, "PHI2021567", "Blanco", "Carga frontal 6kg");
        asociarEquipoCliente(clienteLaura, equipoLauraLava);

        Equipo equipoLauraMicro = crearEquipo(microondas, samsungRT38K, "SAM2023445", "Negro", "Modelo digital");
        asociarEquipoCliente(clienteLaura, equipoLauraMicro);

        // Cliente 6: Carlos Ramírez
        Persona personaCarlos = crearPersona(fisica, dni, "Carlos", "Ramírez", "29876543", "M");
        crearContacto(personaCarlos, email, "carlos.ramirez@gmail.com");
        crearContacto(personaCarlos, celular, "351-8901234");
        Cliente clienteCarlos = crearCliente(personaCarlos, "Cliente nuevo");

        Equipo equipoCarlosHela = crearEquipo(heladera, lgGRB429, "LG2023445", "Inox", "No-Frost");
        asociarEquipoCliente(clienteCarlos, equipoCarlosHela);

        // Cliente 7: Patricia López
        Persona personaPatricia = crearPersona(fisica, dni, "Patricia", "López", "36543210", "F");
        crearContacto(personaPatricia, telefono, "351-3456789");
        crearContacto(personaPatricia, email, "patricia.lopez@hotmail.com");
        Cliente clientePatricia = crearCliente(personaPatricia, "Recomendada por María González");

        Equipo equipoPatriciaLava = crearEquipo(lavarropas, dreanNext1006, "DRN2023990", "Blanco", "Carga frontal 10kg");
        asociarEquipoCliente(clientePatricia, equipoPatriciaLava);

        Equipo equipoPatriciaAire = crearEquipo(aireAcondicionado, lgF1403RD, "LG2020123", "Blanco", "Frío/Calor");
        asociarEquipoCliente(clientePatricia, equipoPatriciaAire);

        // Cliente 8: Diego Torres
        Persona personaDiego = crearPersona(fisica, dni, "Diego", "Torres", "33210987", "M");
        crearContacto(personaDiego, celular, "351-2345678");
        Cliente clienteDiego = crearCliente(personaDiego, "Trabajo en edificio Tribunales");

        Equipo equipoDiegoMicro = crearEquipo(microondas, philcoPHCT25, "PHI2022678", "Blanco", "25 litros");
        asociarEquipoCliente(clienteDiego, equipoDiegoMicro);

        // Cliente 9: Mónica Herrera
        Persona personaMonica = crearPersona(fisica, dni, "Mónica", "Herrera", "40123456", "F");
        crearContacto(personaMonica, email, "monica.herrera@gmail.com");
        crearContacto(personaMonica, celular, "351-9012345");
        Cliente clienteMonica = crearCliente(personaMonica, "Casa de familia - varios equipos");

        Equipo equipoMonicaHela = crearEquipo(heladera, samsungRT38K, "SAM2021789", "Gris", "Twin Cooling");
        asociarEquipoCliente(clienteMonica, equipoMonicaHela);

        Equipo equipoMonicaLava = crearEquipo(lavarropas, lgF1403RD2, "LG2022334", "Blanco", "Direct Drive");
        asociarEquipoCliente(clienteMonica, equipoMonicaLava);

        Equipo equipoMonicaCocina = crearEquipo(cocina, philcoPHCD250, "PHI2020112", "Blanco", "4 hornallas + horno");
        asociarEquipoCliente(clienteMonica, equipoMonicaCocina);

        // Cliente 10: Fernando Silva
        Persona personaFernando = crearPersona(fisica, dni, "Fernando", "Silva", "37654321", "M");
        crearContacto(personaFernando, celular, "351-1234567");
        crearContacto(personaFernando, email, "fsilva@gmail.com");
        Cliente clienteFernando = crearCliente(personaFernando, "Edificio - administrador");

        Equipo equipoFernandoLava = crearEquipo(lavarropas, whirlpoolWLF80AB, "WHP2023112", "Blanco", "Uso comunitario");
        asociarEquipoCliente(clienteFernando, equipoFernandoLava);

        Equipo equipoFernandoSeca = crearEquipo(secarropas, lgGCL247, "LG2023556", "Blanco", "Uso comunitario");
        asociarEquipoCliente(clienteFernando, equipoFernandoSeca);
    }

    private Persona crearPersona(TipoPersona tipoPersona, TipoDocumento tipoDocumento,
                                 String nombre, String apellido, String documento, String sexo) {
        Persona persona = new Persona();
        persona.setTipoPersona(tipoPersona);
        persona.setTipoDocumento(tipoDocumento);
        persona.setNombre(nombre);
        persona.setApellido(apellido);
        persona.setDocumento(documento);
        persona.setSexo(sexo);
        return personaRepository.save(persona);
    }

    private void crearContacto(Persona persona, TipoContacto tipoContacto, String descripcion) {
        Contacto contacto = new Contacto();
        contacto.setPersona(persona);
        contacto.setTipoContacto(tipoContacto);
        contacto.setDescripcion(descripcion);
        contactoRepository.save(contacto);
    }

    private Cliente crearCliente(Persona persona, String comentarios) {
        Cliente cliente = new Cliente();
        cliente.setPersona(persona);
        cliente.setComentarios(comentarios);
        cliente.setActivo(true);
        return clienteRepository.save(cliente);
    }

    private Equipo crearEquipo(TipoEquipo tipoEquipo, Modelo modelo,
                               String numeroSerie, String color, String observaciones) {
        Equipo equipo = new Equipo();
        equipo.setTipoEquipo(tipoEquipo);
        equipo.setMarca(modelo.getMarca());
        equipo.setModelo(modelo);
        equipo.setNumeroSerie(numeroSerie);
        equipo.setColor(color);
        equipo.setObservaciones(observaciones);
        return equipoRepository.save(equipo);
    }

    private void asociarEquipoCliente(Cliente cliente, Equipo equipo) {
        ClienteEquipo clienteEquipo = new ClienteEquipo();
        clienteEquipo.setCliente(cliente);
        clienteEquipo.setEquipo(equipo);
        clienteEquipo.setActivo(true);
        clienteEquipoRepository.save(clienteEquipo);
    }
}
