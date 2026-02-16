package com.sigret.services.impl;

import com.sigret.dtos.cliente.ClienteCreateDto;
import com.sigret.dtos.cliente.ClienteListDto;
import com.sigret.dtos.cliente.ClienteResponseDto;
import com.sigret.dtos.cliente.ClienteUpdateDto;
import com.sigret.dtos.contacto.ContactoCreateDto;
import com.sigret.dtos.contacto.ContactoListDto;
import com.sigret.dtos.direccion.DireccionCreateDto;
import com.sigret.dtos.direccion.DireccionListDto;
import com.sigret.dtos.direccion.GooglePlacesDto;
import com.sigret.entities.Cliente;
import com.sigret.entities.Contacto;
import com.sigret.entities.Direccion;
import com.sigret.entities.Persona;
import com.sigret.entities.TipoContacto;
import com.sigret.entities.TipoDocumento;
import com.sigret.entities.TipoPersona;
import com.sigret.exception.ClienteNotFoundException;
import com.sigret.exception.DocumentoAlreadyExistsException;
import com.sigret.exception.TipoDocumentoNotFoundException;
import com.sigret.exception.TipoContactoNotFoundException;
import com.sigret.exception.TipoPersonaNotFoundException;
import com.sigret.repositories.ClienteRepository;
import com.sigret.repositories.ContactoRepository;
import com.sigret.repositories.DireccionRepository;
import com.sigret.repositories.PersonaRepository;
import com.sigret.repositories.TipoContactoRepository;
import com.sigret.repositories.TipoDocumentoRepository;
import com.sigret.repositories.TipoPersonaRepository;
import com.sigret.services.ClienteService;
import com.sigret.utilities.GooglePlacesParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private ContactoRepository contactoRepository;

    @Autowired
    private TipoPersonaRepository tipoPersonaRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private TipoContactoRepository tipoContactoRepository;

    @Override
    public ClienteResponseDto crearCliente(ClienteCreateDto clienteCreateDto) {
        // Validar que el documento no existe (validación interna)
        if (clienteRepository.existsByPersonaDocumento(clienteCreateDto.getDocumento())) {
            throw new DocumentoAlreadyExistsException("Ya existe un cliente con el documento: " + clienteCreateDto.getDocumento());
        }

        // Buscar TipoPersona por ID
        TipoPersona tipoPersona = tipoPersonaRepository.findById(clienteCreateDto.getTipoPersonaId())
                .orElseThrow(() -> new TipoPersonaNotFoundException("Tipo de persona no encontrado con ID: " + clienteCreateDto.getTipoPersonaId()));

        // Buscar TipoDocumento por ID
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(clienteCreateDto.getTipoDocumentoId())
                .orElseThrow(() -> new TipoDocumentoNotFoundException("Tipo de documento no encontrado con ID: " + clienteCreateDto.getTipoDocumentoId()));

        // Crear la persona
        Persona persona = new Persona();
        persona.setTipoPersona(tipoPersona);
        persona.setNombre(clienteCreateDto.getNombre());
        persona.setApellido(clienteCreateDto.getApellido());
        persona.setRazonSocial(clienteCreateDto.getRazonSocial());
        persona.setTipoDocumento(tipoDocumento);
        persona.setDocumento(clienteCreateDto.getDocumento());
        persona.setSexo(clienteCreateDto.getSexo());

        Persona personaGuardada = personaRepository.save(persona);

        // Crear el cliente (activo por defecto)
        Cliente cliente = new Cliente(personaGuardada);
        cliente.setComentarios(clienteCreateDto.getComentarios());
        cliente.setActivo(true);

        Cliente clienteGuardado = clienteRepository.save(cliente);

        // Crear contactos si fueron proporcionados
        if (clienteCreateDto.getContactos() != null && !clienteCreateDto.getContactos().isEmpty()) {
            crearContactos(personaGuardada, clienteCreateDto.getContactos());
        }

        // Crear direcciones si fueron proporcionadas
        if (clienteCreateDto.getDirecciones() != null && !clienteCreateDto.getDirecciones().isEmpty()) {
            crearDirecciones(personaGuardada, clienteCreateDto.getDirecciones());
        }

        return convertirAClienteResponseDto(clienteGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findByIdWithPersona(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));

        return convertirAClienteResponseDto(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteListDto> obtenerClientes(Pageable pageable, String filtro) {
        Page<Cliente> clientes;
        
        if (filtro != null && !filtro.trim().isEmpty()) {
            clientes = clienteRepository.buscarClientesConFiltros(filtro.trim(), pageable);
        } else {
            clientes = clienteRepository.findByActivoTrue(pageable);
        }
        
        return clientes.map(this::convertirAClienteListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteListDto> obtenerTodosLosClientes() {
        List<Cliente> clientes = clienteRepository.findByActivoTrue();
        return clientes.stream()
                .map(this::convertirAClienteListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteListDto> buscarClientesAutocompletado(String termino, int limite) {
        if (termino == null || termino.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limite);
        List<Cliente> clientes = clienteRepository.buscarClientesPorTermino(termino.trim(), pageable);
        
        return clientes.stream()
                .map(this::convertirAClienteListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerClientePorDocumento(String documento) {
        Cliente cliente = clienteRepository.findByPersonaDocumentoAndActivoTrue(documento)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con documento: " + documento));

        return convertirAClienteResponseDto(cliente);
    }

    @Override
    public ClienteResponseDto actualizarCliente(Long id, ClienteUpdateDto clienteUpdateDto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));

        // Actualizar datos de la persona
        Persona persona = cliente.getPersona();
        if (clienteUpdateDto.getNombre() != null) {
            persona.setNombre(clienteUpdateDto.getNombre());
        }
        if (clienteUpdateDto.getApellido() != null) {
            persona.setApellido(clienteUpdateDto.getApellido());
        }
        if (clienteUpdateDto.getRazonSocial() != null) {
            persona.setRazonSocial(clienteUpdateDto.getRazonSocial());
        }
        if (clienteUpdateDto.getSexo() != null) {
            persona.setSexo(clienteUpdateDto.getSexo());
        }
        if (clienteUpdateDto.getComentarios() != null) {
            cliente.setComentarios(clienteUpdateDto.getComentarios());
        }

        personaRepository.save(persona);
        Cliente clienteActualizado = clienteRepository.save(cliente);

        // Actualizar contactos si fueron proporcionados
        if (clienteUpdateDto.getContactos() != null) {
            actualizarContactos(persona, clienteUpdateDto.getContactos());
        }

        // Actualizar direcciones si fueron proporcionadas
        if (clienteUpdateDto.getDirecciones() != null) {
            actualizarDirecciones(persona, clienteUpdateDto.getDirecciones());
        }

        return convertirAClienteResponseDto(clienteActualizado);
    }

    @Override
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));
        
        // Baja lógica
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    public void reactivarCliente(Long id) {
        Cliente cliente = clienteRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));
        
        cliente.setActivo(true);
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerClienteConEquipos(Long id) {
        Cliente cliente = clienteRepository.findByIdWithPersona(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));

        return convertirAClienteResponseDto(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteListDto> obtenerClientesInactivos(Pageable pageable) {
        Page<Cliente> clientes = clienteRepository.findByActivoFalse(pageable);
        return clientes.map(this::convertirAClienteListDto);
    }

    /**
     * Crear contactos para una persona
     */
    private void crearContactos(Persona persona, List<ContactoCreateDto> contactosDto) {
        for (ContactoCreateDto contactoDto : contactosDto) {
            // Buscar TipoContacto por ID
            TipoContacto tipoContacto = tipoContactoRepository.findById(contactoDto.getTipoContactoId())
                    .orElseThrow(() -> new TipoContactoNotFoundException("Tipo de contacto no encontrado con ID: " + contactoDto.getTipoContactoId()));

            Contacto contacto = new Contacto();
            contacto.setPersona(persona);
            contacto.setTipoContacto(tipoContacto);
            contacto.setDescripcion(contactoDto.getDescripcion());

            contactoRepository.save(contacto);
        }
    }

    /**
     * Actualizar contactos de una persona (reemplaza todos los existentes)
     */
    private void actualizarContactos(Persona persona, List<ContactoCreateDto> contactosDto) {
        // Eliminar todos los contactos existentes
        List<Contacto> contactosExistentes = contactoRepository.findByPersonaId(persona.getId());
        contactoRepository.deleteAll(contactosExistentes);

        // Crear los nuevos contactos
        if (contactosDto != null && !contactosDto.isEmpty()) {
            crearContactos(persona, contactosDto);
        }
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

    private ClienteResponseDto convertirAClienteResponseDto(Cliente cliente) {
        // Obtener contactos de la persona
        List<Contacto> contactos = contactoRepository.findByPersonaId(cliente.getPersona().getId());
        List<ContactoListDto> contactosDto = convertirContactosADto(contactos);

        // Obtener direcciones de la persona
        List<Direccion> direcciones = direccionRepository.findByPersonaId(cliente.getPersona().getId());
        List<DireccionListDto> direccionesDto = convertirDireccionesADto(direcciones);

        ClienteResponseDto response = new ClienteResponseDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getDocumento(),
                cliente.getPersona().getTipoPersona().getDescripcion(),
                cliente.getPersona().getTipoDocumento().getDescripcion(),
                cliente.getPrimerEmail(),
                cliente.getPrimerTelefono(),
                cliente.getComentarios(),
                cliente.esPersonaJuridica(),
                cliente.getActivo(),
                contactosDto,
                direccionesDto
        );

        return response;
    }

    /**
     * Convertir lista de contactos a DTOs
     */
    private List<ContactoListDto> convertirContactosADto(List<Contacto> contactos) {
        if (contactos == null) {
            return new ArrayList<>();
        }
        return contactos.stream()
                .map(c -> new ContactoListDto(
                        c.getId(),
                        c.getTipoContacto().getDescripcion(),
                        c.getDescripcion()
                ))
                .collect(Collectors.toList());
    }

    private ClienteListDto convertirAClienteListDto(Cliente cliente) {
        // Obtener dirección principal formateada
        String direccionPrincipal = obtenerDireccionPrincipalFormateada(cliente.getPersona().getId());

        return new ClienteListDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getDocumento(),
                cliente.getPrimerEmail(),
                cliente.getPrimerTelefono(),
                direccionPrincipal,
                cliente.esPersonaJuridica(),
                cliente.getActivo()
        );
    }

    /**
     * Obtiene la dirección principal de una persona y la formatea de forma corta
     * Formato: "calle numero, barrio" o "calle numero - piso departamento, barrio"
     */
    private String obtenerDireccionPrincipalFormateada(Long personaId) {
        List<Direccion> direcciones = direccionRepository.findByPersonaId(personaId);
        
        if (direcciones == null || direcciones.isEmpty()) {
            return null;
        }
        
        // Buscar dirección principal o la primera si no hay principal
        Direccion direccionPrincipal = direcciones.stream()
                .filter(Direccion::getEsPrincipal)
                .findFirst()
                .orElse(direcciones.get(0));
        
        return formatearDireccionCorta(direccionPrincipal);
    }

    /**
     * Formatea una dirección de forma corta
     * Formato: "calle numero, barrio" o "calle numero - piso departamento, barrio"
     */
    private String formatearDireccionCorta(Direccion direccion) {
        if (direccion == null) {
            return null;
        }
        
        StringBuilder direccionCorta = new StringBuilder();
        
        // Calle y número
        if (direccion.getCalle() != null && !direccion.getCalle().trim().isEmpty()) {
            direccionCorta.append(direccion.getCalle());
            
            if (direccion.getNumero() != null && !direccion.getNumero().trim().isEmpty()) {
                direccionCorta.append(" ").append(direccion.getNumero());
            }
        }
        
        // Piso y departamento (si existen)
        if ((direccion.getPiso() != null && !direccion.getPiso().trim().isEmpty()) ||
            (direccion.getDepartamento() != null && !direccion.getDepartamento().trim().isEmpty())) {
            
            direccionCorta.append(" - ");
            
            if (direccion.getPiso() != null && !direccion.getPiso().trim().isEmpty()) {
                direccionCorta.append(direccion.getPiso());
            }
            
            if (direccion.getDepartamento() != null && !direccion.getDepartamento().trim().isEmpty()) {
                if (direccion.getPiso() != null && !direccion.getPiso().trim().isEmpty()) {
                    direccionCorta.append(" ");
                }
                direccionCorta.append(direccion.getDepartamento());
            }
        }
        
        // Barrio
        if (direccion.getBarrio() != null && !direccion.getBarrio().trim().isEmpty()) {
            if (direccionCorta.length() > 0) {
                direccionCorta.append(", ");
            }
            direccionCorta.append(direccion.getBarrio());
        }
        
        return direccionCorta.length() > 0 ? direccionCorta.toString() : null;
    }
}
