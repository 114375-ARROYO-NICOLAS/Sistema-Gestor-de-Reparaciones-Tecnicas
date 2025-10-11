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
     * Obtener un cliente por ID (solo activos)
     */
    ClienteResponseDto obtenerClientePorId(Long id);
    
    /**
     * Obtener todos los clientes activos con paginación y filtros opcionales
     */
    Page<ClienteListDto> obtenerClientes(Pageable pageable, String filtro);
    
    /**
     * Obtener todos los clientes activos
     */
    List<ClienteListDto> obtenerTodosLosClientes();
    
    /**
     * Buscar clientes por nombre o documento (para autocompletado, límite de resultados)
     */
    List<ClienteListDto> buscarClientesAutocompletado(String termino, int limite);
    
    /**
     * Obtener cliente por documento
     */
    ClienteResponseDto obtenerClientePorDocumento(String documento);
    
    /**
     * Actualizar un cliente
     */
    ClienteResponseDto actualizarCliente(Long id, ClienteUpdateDto clienteUpdateDto);
    
    /**
     * Eliminar un cliente (baja lógica)
     */
    void eliminarCliente(Long id);
    
    /**
     * Reactivar un cliente
     */
    void reactivarCliente(Long id);
    
    /**
     * Obtener cliente con sus equipos
     */
    ClienteResponseDto obtenerClienteConEquipos(Long id);
}
