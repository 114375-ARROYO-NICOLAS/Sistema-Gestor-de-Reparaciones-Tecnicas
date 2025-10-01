package com.sigret.services.impl;

import com.sigret.dtos.cliente.ClienteCreateDto;
import com.sigret.dtos.cliente.ClienteListDto;
import com.sigret.dtos.cliente.ClienteResponseDto;
import com.sigret.dtos.cliente.ClienteUpdateDto;
import com.sigret.entities.Cliente;
import com.sigret.entities.Persona;
import com.sigret.exception.ClienteNotFoundException;
import com.sigret.exception.DocumentoAlreadyExistsException;
import com.sigret.repositories.ClienteRepository;
import com.sigret.repositories.PersonaRepository;
import com.sigret.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PersonaRepository personaRepository;

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

    private ClienteResponseDto convertirAClienteResponseDto(Cliente cliente) {
        return new ClienteResponseDto(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getDocumento(),
                cliente.getPersona().getTipoPersona().getDescripcion(),
                cliente.getPersona().getTipoDocumento().getDescripcion(),
                cliente.getPrimerEmail(),
                cliente.getPrimerTelefono(),
                cliente.getComentarios(),
                cliente.esPersonaJuridica()
        );
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
