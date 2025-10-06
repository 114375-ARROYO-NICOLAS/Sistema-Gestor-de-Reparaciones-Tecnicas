package com.sigret.services;

import com.sigret.dtos.direccion.DireccionCreateDto;
import com.sigret.dtos.direccion.DireccionListDto;
import com.sigret.dtos.direccion.GooglePlacesDto;
import com.sigret.dtos.empleado.EmpleadoCreateDto;
import com.sigret.dtos.empleado.EmpleadoListDto;
import com.sigret.dtos.empleado.EmpleadoResponseDto;
import com.sigret.dtos.empleado.EmpleadoUpdateDto;
import com.sigret.entities.*;
import com.sigret.exception.DocumentoAlreadyExistsException;
import com.sigret.exception.EmpleadoNotFoundException;
import com.sigret.exception.UsernameAlreadyExistsException;
import com.sigret.repositories.*;
import com.sigret.utilities.GooglePlacesParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoEmpleadoRepository tipoEmpleadoRepository;

    @Autowired
    private TipoPersonaRepository tipoPersonaRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DireccionRepository direccionRepository;

    /**
     * Crear un nuevo empleado y automáticamente crear su usuario
     * El username y password por defecto será el documento del empleado
     */
    public EmpleadoResponseDto crearEmpleado(EmpleadoCreateDto empleadoCreateDto) {
        // Validar que el documento no exista
        if (personaRepository.existsByDocumento(empleadoCreateDto.getDocumento())) {
            throw new DocumentoAlreadyExistsException("Ya existe una persona con el documento: " + empleadoCreateDto.getDocumento());
        }

        // Determinar username para el usuario
        String username = empleadoCreateDto.getUsernamePersonalizado() != null 
            ? empleadoCreateDto.getUsernamePersonalizado() 
            : empleadoCreateDto.getDocumento();

        // Validar que el username no exista
        if (usuarioRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("El username ya está en uso: " + username);
        }

        // Obtener las entidades de referencia
        TipoEmpleado tipoEmpleado = tipoEmpleadoRepository.findById(empleadoCreateDto.getTipoEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Tipo de empleado no encontrado con ID: " + empleadoCreateDto.getTipoEmpleadoId()));

        TipoPersona tipoPersona = tipoPersonaRepository.findById(empleadoCreateDto.getTipoPersonaId())
                .orElseThrow(() -> new RuntimeException("Tipo de persona no encontrado con ID: " + empleadoCreateDto.getTipoPersonaId()));

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(empleadoCreateDto.getTipoDocumentoId())
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado con ID: " + empleadoCreateDto.getTipoDocumentoId()));

        // Crear la persona
        Persona persona = new Persona();
        persona.setTipoPersona(tipoPersona);
        persona.setNombre(empleadoCreateDto.getNombre());
        persona.setApellido(empleadoCreateDto.getApellido());
        persona.setRazonSocial(empleadoCreateDto.getRazonSocial());
        persona.setTipoDocumento(tipoDocumento);
        persona.setDocumento(empleadoCreateDto.getDocumento());
        persona.setSexo(empleadoCreateDto.getSexo());
        persona = personaRepository.save(persona);

        // Crear el empleado
        Empleado empleado = new Empleado();
        empleado.setTipoEmpleado(tipoEmpleado);
        empleado.setPersona(persona);
        empleado.setActivo(true);
        empleado = empleadoRepository.save(empleado);

        // Crear el usuario automáticamente
        Usuario usuario = new Usuario();
        usuario.setEmpleado(empleado);
        usuario.setUsername(username);
        
        // Determinar password (por defecto el documento)
        String password = empleadoCreateDto.getPasswordPersonalizada() != null 
            ? empleadoCreateDto.getPasswordPersonalizada() 
            : empleadoCreateDto.getDocumento();
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(empleadoCreateDto.getRolUsuario());
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);

        // Actualizar la referencia en empleado
        empleado.setUsuario(usuario);

        // Crear direcciones si fueron proporcionadas
        if (empleadoCreateDto.getDirecciones() != null && !empleadoCreateDto.getDirecciones().isEmpty()) {
            crearDirecciones(persona, empleadoCreateDto.getDirecciones());
        }

        return convertirAEmpleadoResponseDto(empleado);
    }

    /**
     * Obtener un empleado por ID
     */
    @Transactional(readOnly = true)
    public EmpleadoResponseDto obtenerEmpleadoPorId(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new EmpleadoNotFoundException("Empleado no encontrado con ID: " + id));

        return convertirAEmpleadoResponseDto(empleado);
    }

    /**
     * Obtener todos los empleados con paginación y filtros
     */
    @Transactional(readOnly = true)
    public Page<EmpleadoListDto> obtenerEmpleadosConFiltros(Boolean activo, String busqueda, Pageable pageable) {
        Page<Empleado> empleados;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            // Búsqueda con filtro de texto
            empleados = empleadoRepository.buscarEmpleadosConFiltros(activo, busqueda, pageable);
        } else if (activo != null) {
            // Solo filtro por estado
            empleados = empleadoRepository.findByActivo(activo, pageable);
        } else {
            // Sin filtros
            empleados = empleadoRepository.findAll(pageable);
        }

        return empleados.map(this::convertirAEmpleadoListDto);
    }

    /**
     * Obtener todos los empleados activos (sin paginación)
     */
    @Transactional(readOnly = true)
    public List<EmpleadoListDto> obtenerEmpleadosActivos() {
        List<Empleado> empleados = empleadoRepository.findByActivoTrue();
        return empleados.stream()
                .map(this::convertirAEmpleadoListDto)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar un empleado
     */
    public EmpleadoResponseDto actualizarEmpleado(Long id, EmpleadoUpdateDto empleadoUpdateDto) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new EmpleadoNotFoundException("Empleado no encontrado con ID: " + id));

        Persona persona = empleado.getPersona();

        // Actualizar tipo de empleado si se proporciona
        if (empleadoUpdateDto.getTipoEmpleadoId() != null) {
            TipoEmpleado tipoEmpleado = tipoEmpleadoRepository.findById(empleadoUpdateDto.getTipoEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Tipo de empleado no encontrado"));
            empleado.setTipoEmpleado(tipoEmpleado);
        }

        // Actualizar datos de persona
        if (empleadoUpdateDto.getNombre() != null) {
            persona.setNombre(empleadoUpdateDto.getNombre());
        }
        if (empleadoUpdateDto.getApellido() != null) {
            persona.setApellido(empleadoUpdateDto.getApellido());
        }
        if (empleadoUpdateDto.getRazonSocial() != null) {
            persona.setRazonSocial(empleadoUpdateDto.getRazonSocial());
        }
        if (empleadoUpdateDto.getSexo() != null) {
            persona.setSexo(empleadoUpdateDto.getSexo());
        }

        // Actualizar documento si se proporciona y no existe
        if (empleadoUpdateDto.getDocumento() != null && !empleadoUpdateDto.getDocumento().equals(persona.getDocumento())) {
            if (personaRepository.existsByDocumento(empleadoUpdateDto.getDocumento())) {
                throw new DocumentoAlreadyExistsException("Ya existe una persona con el documento: " + empleadoUpdateDto.getDocumento());
            }
            persona.setDocumento(empleadoUpdateDto.getDocumento());
        }

        // Actualizar tipo de documento
        if (empleadoUpdateDto.getTipoDocumentoId() != null) {
            TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(empleadoUpdateDto.getTipoDocumentoId())
                    .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
            persona.setTipoDocumento(tipoDocumento);
        }

        // Actualizar tipo de persona
        if (empleadoUpdateDto.getTipoPersonaId() != null) {
            TipoPersona tipoPersona = tipoPersonaRepository.findById(empleadoUpdateDto.getTipoPersonaId())
                    .orElseThrow(() -> new RuntimeException("Tipo de persona no encontrado"));
            persona.setTipoPersona(tipoPersona);
        }

        // Actualizar estado activo
        if (empleadoUpdateDto.getActivo() != null) {
            empleado.setActivo(empleadoUpdateDto.getActivo());
        }

        personaRepository.save(persona);
        empleado = empleadoRepository.save(empleado);

        // Actualizar direcciones si fueron proporcionadas
        if (empleadoUpdateDto.getDirecciones() != null) {
            actualizarDirecciones(persona, empleadoUpdateDto.getDirecciones());
        }

        return convertirAEmpleadoResponseDto(empleado);
    }

    /**
     * Desactivar un empleado (baja lógica)
     */
    public void desactivarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new EmpleadoNotFoundException("Empleado no encontrado con ID: " + id));
        
        empleado.setActivo(false);
        
        // También desactivar el usuario asociado si existe
        if (empleado.getUsuario() != null) {
            empleado.getUsuario().setActivo(false);
            usuarioRepository.save(empleado.getUsuario());
        }
        
        empleadoRepository.save(empleado);
    }

    /**
     * Activar un empleado
     */
    public void activarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new EmpleadoNotFoundException("Empleado no encontrado con ID: " + id));
        
        empleado.setActivo(true);
        
        // También activar el usuario asociado si existe
        if (empleado.getUsuario() != null) {
            empleado.getUsuario().setActivo(true);
            usuarioRepository.save(empleado.getUsuario());
        }
        
        empleadoRepository.save(empleado);
    }

    /**
     * Eliminar un empleado (hard delete)
     */
    public void eliminarEmpleado(Long id) {
        if (!empleadoRepository.existsById(id)) {
            throw new EmpleadoNotFoundException("Empleado no encontrado con ID: " + id);
        }
        empleadoRepository.deleteById(id);
    }

    /**
     * Crear direcciones para una persona
     */
    private void crearDirecciones(Persona persona, List<DireccionCreateDto> direccionesDto) {
        for (DireccionCreateDto direccionDto : direccionesDto) {
            Direccion direccion = new Direccion();
            direccion.setPersona(persona);
            
            // Procesar Google Places si está disponible
            if (direccionDto.getGooglePlacesData() != null) {
                procesarGooglePlacesData(direccion, direccionDto.getGooglePlacesData());
            }
            
            // Establecer campos individuales (sobrescriben Google Places)
            if (direccionDto.getPlaceId() != null) direccion.setPlaceId(direccionDto.getPlaceId());
            if (direccionDto.getLatitud() != null) direccion.setLatitud(direccionDto.getLatitud());
            if (direccionDto.getLongitud() != null) direccion.setLongitud(direccionDto.getLongitud());
            if (direccionDto.getDireccionFormateada() != null) direccion.setDireccionFormateada(direccionDto.getDireccionFormateada());
            if (direccionDto.getCalle() != null) direccion.setCalle(direccionDto.getCalle());
            if (direccionDto.getNumero() != null) direccion.setNumero(direccionDto.getNumero());
            if (direccionDto.getPiso() != null) direccion.setPiso(direccionDto.getPiso());
            if (direccionDto.getDepartamento() != null) direccion.setDepartamento(direccionDto.getDepartamento());
            if (direccionDto.getBarrio() != null) direccion.setBarrio(direccionDto.getBarrio());
            if (direccionDto.getCiudad() != null) direccion.setCiudad(direccionDto.getCiudad());
            if (direccionDto.getProvincia() != null) direccion.setProvincia(direccionDto.getProvincia());
            if (direccionDto.getCodigoPostal() != null) direccion.setCodigoPostal(direccionDto.getCodigoPostal());
            if (direccionDto.getPais() != null) direccion.setPais(direccionDto.getPais());
            if (direccionDto.getObservaciones() != null) direccion.setObservaciones(direccionDto.getObservaciones());
            
            direccion.setEsPrincipal(direccionDto.getEsPrincipal() != null ? direccionDto.getEsPrincipal() : false);
            
            // Si es principal, desmarcar otras direcciones
            if (direccion.getEsPrincipal()) {
                List<Direccion> direccionesExistentes = direccionRepository.findByPersonaId(persona.getId());
                direccionesExistentes.forEach(d -> d.setEsPrincipal(false));
                direccionRepository.saveAll(direccionesExistentes);
            }
            
            direccionRepository.save(direccion);
        }
    }

    /**
     * Actualizar direcciones de una persona (reemplaza todas las existentes)
     */
    private void actualizarDirecciones(Persona persona, List<DireccionCreateDto> direccionesDto) {
        // Eliminar todas las direcciones existentes
        List<Direccion> direccionesExistentes = direccionRepository.findByPersonaId(persona.getId());
        direccionRepository.deleteAll(direccionesExistentes);
        
        // Crear las nuevas direcciones
        if (direccionesDto != null && !direccionesDto.isEmpty()) {
            crearDirecciones(persona, direccionesDto);
        }
    }

    /**
     * Procesa los datos de Google Places y los asigna a la dirección
     */
    private void procesarGooglePlacesData(Direccion direccion, GooglePlacesDto googlePlacesData) {
        if (googlePlacesData.getPlaceId() != null) {
            direccion.setPlaceId(googlePlacesData.getPlaceId());
        }
        if (googlePlacesData.getFormattedAddress() != null) {
            direccion.setDireccionFormateada(googlePlacesData.getFormattedAddress());
        }
        
        Double[] coordinates = GooglePlacesParser.extractCoordinates(googlePlacesData);
        if (coordinates != null) {
            direccion.setLatitud(coordinates[0]);
            direccion.setLongitud(coordinates[1]);
        }
        
        Map<String, String> components = GooglePlacesParser.extractAddressComponents(googlePlacesData);
        if (components.containsKey("calle")) direccion.setCalle(components.get("calle"));
        if (components.containsKey("numero")) direccion.setNumero(components.get("numero"));
        if (components.containsKey("barrio")) direccion.setBarrio(components.get("barrio"));
        if (components.containsKey("ciudad")) direccion.setCiudad(components.get("ciudad"));
        if (components.containsKey("provincia")) direccion.setProvincia(components.get("provincia"));
        if (components.containsKey("pais")) direccion.setPais(components.get("pais"));
        if (components.containsKey("codigoPostal")) direccion.setCodigoPostal(components.get("codigoPostal"));
    }

    /**
     * Convertir lista de direcciones a DTOs
     */
    private List<DireccionListDto> convertirDireccionesADto(List<Direccion> direcciones) {
        if (direcciones == null) {
            return new ArrayList<>();
        }
        return direcciones.stream()
                .map(d -> new DireccionListDto(
                        d.getId(),
                        d.getPlaceId(),
                        d.getCalle(),
                        d.getNumero(),
                        d.getCiudad(),
                        d.getProvincia(),
                        d.getPais(),
                        d.getEsPrincipal(),
                        d.getDireccionCompleta(),
                        d.getLatitud(),
                        d.getLongitud()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Convertir Empleado a EmpleadoResponseDto
     */
    private EmpleadoResponseDto convertirAEmpleadoResponseDto(Empleado empleado) {
        Persona persona = empleado.getPersona();
        Usuario usuario = empleado.getUsuario();

        // Obtener direcciones de la persona
        List<Direccion> direcciones = direccionRepository.findByPersonaId(persona.getId());
        List<DireccionListDto> direccionesDto = convertirDireccionesADto(direcciones);

        EmpleadoResponseDto response = new EmpleadoResponseDto(
                empleado.getId(),
                persona.getNombreCompleto(),
                persona.getNombre(),
                persona.getApellido(),
                persona.getRazonSocial(),
                persona.getDocumento(),
                persona.getTipoDocumento().getDescripcion(),
                persona.getTipoPersona().getDescripcion(),
                persona.getSexo(),
                empleado.getTipoEmpleado().getDescripcion(),
                empleado.getTipoEmpleado().getId(),
                empleado.getActivo(),
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getUsername() : null,
                usuario != null ? usuario.getRol() : null,
                usuario != null ? usuario.getActivo() : null,
                usuario != null ? usuario.getFechaCreacion() : null,
                usuario != null ? usuario.getUltimoLogin() : null,
                direccionesDto
        );
        
        return response;
    }

    /**
     * Convertir Empleado a EmpleadoListDto
     */
    private EmpleadoListDto convertirAEmpleadoListDto(Empleado empleado) {
        Persona persona = empleado.getPersona();
        Usuario usuario = empleado.getUsuario();

        return new EmpleadoListDto(
                empleado.getId(),
                persona.getNombreCompleto(),
                persona.getDocumento(),
                empleado.getTipoEmpleado().getDescripcion(),
                empleado.getActivo(),
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getUsername() : null,
                usuario != null ? usuario.getRol() : null,
                usuario != null ? usuario.getActivo() : null
        );
    }
}

