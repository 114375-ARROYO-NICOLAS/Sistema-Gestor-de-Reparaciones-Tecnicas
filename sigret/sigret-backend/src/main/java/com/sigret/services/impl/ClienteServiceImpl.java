package com.sigret.services.impl;

import com.sigret.dtos.cliente.ClienteCreateDto;
import com.sigret.dtos.cliente.ClienteListDto;
import com.sigret.dtos.cliente.ClienteResponseDto;
import com.sigret.dtos.cliente.ClienteUpdateDto;
import com.sigret.dtos.direccion.DireccionCreateDto;
import com.sigret.dtos.direccion.DireccionListDto;
import com.sigret.dtos.direccion.GooglePlacesDto;
import com.sigret.entities.Cliente;
import com.sigret.entities.Direccion;
import com.sigret.entities.Persona;
import com.sigret.exception.ClienteNotFoundException;
import com.sigret.exception.DocumentoAlreadyExistsException;
import com.sigret.repositories.ClienteRepository;
import com.sigret.repositories.DireccionRepository;
import com.sigret.repositories.PersonaRepository;
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

    @Override
    public ClienteResponseDto crearCliente(ClienteCreateDto clienteCreateDto) {
        // Validar que el documento no existe
        if (existeClienteConDocumento(clienteCreateDto.getDocumento())) {
            throw new DocumentoAlreadyExistsException("Ya existe un cliente con el documento: " + clienteCreateDto.getDocumento());
        }

        // Crear o encontrar la persona
        Persona persona = new Persona();
        persona.setTipoPersona(clienteCreateDto.getTipoPersona());
        persona.setNombre(clienteCreateDto.getNombre());
        persona.setApellido(clienteCreateDto.getApellido());
        persona.setRazonSocial(clienteCreateDto.getRazonSocial());
        persona.setTipoDocumento(clienteCreateDto.getTipoDocumento());
        persona.setDocumento(clienteCreateDto.getDocumento());
        persona.setSexo(clienteCreateDto.getSexo());

        Persona personaGuardada = personaRepository.save(persona);

        // Crear el cliente
        Cliente cliente = new Cliente(personaGuardada);
        cliente.setComentarios(clienteCreateDto.getComentarios());

        Cliente clienteGuardado = clienteRepository.save(cliente);

        // Crear direcciones si fueron proporcionadas
        if (clienteCreateDto.getDirecciones() != null && !clienteCreateDto.getDirecciones().isEmpty()) {
            crearDirecciones(personaGuardada, clienteCreateDto.getDirecciones());
        }

        return convertirAClienteResponseDto(clienteGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));

        return convertirAClienteResponseDto(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteListDto> obtenerClientes(Pageable pageable) {
        Page<Cliente> clientes = clienteRepository.findAll(pageable);
        return clientes.map(this::convertirAClienteListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteListDto> obtenerTodosLosClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream()
                .map(this::convertirAClienteListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteListDto> buscarClientes(String termino) {
        List<Cliente> clientes = clienteRepository.findAll().stream()
                .filter(c -> c.getNombreCompleto().toLowerCase().contains(termino.toLowerCase()) ||
                           c.getDocumento().contains(termino))
                .collect(Collectors.toList());

        return clientes.stream()
                .map(this::convertirAClienteListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerClientePorDocumento(String documento) {
        Cliente cliente = clienteRepository.findByPersonaDocumento(documento)
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

        // Actualizar direcciones si fueron proporcionadas
        if (clienteUpdateDto.getDirecciones() != null) {
            actualizarDirecciones(persona, clienteUpdateDto.getDirecciones());
        }

        return convertirAClienteResponseDto(clienteActualizado);
    }

    @Override
    public void eliminarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ClienteNotFoundException("Cliente no encontrado con ID: " + id);
        }
        clienteRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteConDocumento(String documento) {
        return clienteRepository.existsByPersonaDocumento(documento);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerClienteConEquipos(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));

        return convertirAClienteResponseDto(cliente);
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
                direccionesDto
        );
        
        return response;
    }

    private ClienteListDto convertirAClienteListDto(Cliente cliente) {
        return new ClienteListDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getDocumento(),
                cliente.getPrimerEmail(),
                cliente.getPrimerTelefono(),
                cliente.esPersonaJuridica()
        );
    }
}
