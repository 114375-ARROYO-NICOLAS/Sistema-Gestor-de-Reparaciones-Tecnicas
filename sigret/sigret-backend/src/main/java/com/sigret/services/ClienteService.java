package com.sigret.services;

import com.sigret.dtos.cliente.ClienteCreateDto;
import com.sigret.dtos.cliente.ClienteListDto;
import com.sigret.dtos.cliente.ClienteResponseDto;
import com.sigret.dtos.cliente.ClienteUpdateDto;
import com.sigret.entities.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClienteService {
    
    /**
     * Crear un nuevo cliente
     */
    ClienteResponseDto crearCliente(ClienteCreateDto clienteCreateDto);
    
    /**
     * Obtener un cliente por ID
     */
    ClienteResponseDto obtenerClientePorId(Long id);
    
    /**
     * Obtener todos los clientes con paginación
     */
    Page<ClienteListDto> obtenerClientes(Pageable pageable);
    
    /**
     * Obtener todos los clientes
     */
    List<ClienteListDto> obtenerTodosLosClientes();
    
    /**
     * Buscar clientes por nombre o documento
     */
    List<ClienteListDto> buscarClientes(String termino);
    
    /**
     * Obtener cliente por documento
     */
    ClienteResponseDto obtenerClientePorDocumento(String documento);
    
    /**
     * Actualizar un cliente
     */
    ClienteResponseDto actualizarCliente(Long id, ClienteUpdateDto clienteUpdateDto);
    
    /**
     * Eliminar un cliente
     */
    void eliminarCliente(Long id);
    
    /**
     * Verificar si un documento ya existe
     */
    boolean existeClienteConDocumento(String documento);
    
    /**
     * Obtener cliente con sus equipos
     */
    ClienteResponseDto obtenerClienteConEquipos(Long id);
}
