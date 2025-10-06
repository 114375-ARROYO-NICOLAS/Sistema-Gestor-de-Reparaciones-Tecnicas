package com.sigret.services.impl;

import com.sigret.dtos.direccion.DireccionCreateDto;
import com.sigret.dtos.direccion.DireccionListDto;
import com.sigret.dtos.direccion.DireccionResponseDto;
import com.sigret.dtos.direccion.DireccionUpdateDto;
import com.sigret.dtos.direccion.GooglePlacesDto;
import com.sigret.entities.Direccion;
import com.sigret.entities.Persona;
import com.sigret.exception.DireccionNotFoundException;
import com.sigret.repositories.DireccionRepository;
import com.sigret.repositories.PersonaRepository;
import com.sigret.services.DireccionService;
import com.sigret.utilities.GooglePlacesParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DireccionServiceImpl implements DireccionService {

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Override
    public DireccionResponseDto crearDireccion(DireccionCreateDto direccionCreateDto) {
        // Validar que personaId esté presente (requerido para endpoint directo)
        if (direccionCreateDto.getPersonaId() == null) {
            throw new IllegalArgumentException("El ID de la persona es obligatorio");
        }
        
        // Buscar la persona
        Persona persona = personaRepository.findById(direccionCreateDto.getPersonaId())
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + direccionCreateDto.getPersonaId()));

        // Si la dirección se marca como principal, desmarcar las otras
        if (Boolean.TRUE.equals(direccionCreateDto.getEsPrincipal())) {
            desmarcarDireccionesPrincipales(persona.getId());
        }

        // Crear la dirección
        Direccion direccion = new Direccion();
        direccion.setPersona(persona);

        // Procesar datos de Google Places si están disponibles
        if (direccionCreateDto.getGooglePlacesData() != null) {
            procesarGooglePlacesData(direccion, direccionCreateDto.getGooglePlacesData());
        }

        // Establecer o sobrescribir con datos específicos del DTO (tienen prioridad sobre Google Places)
        if (direccionCreateDto.getPlaceId() != null) {
            direccion.setPlaceId(direccionCreateDto.getPlaceId());
        }
        if (direccionCreateDto.getLatitud() != null) {
            direccion.setLatitud(direccionCreateDto.getLatitud());
        }
        if (direccionCreateDto.getLongitud() != null) {
            direccion.setLongitud(direccionCreateDto.getLongitud());
        }
        if (direccionCreateDto.getDireccionFormateada() != null) {
            direccion.setDireccionFormateada(direccionCreateDto.getDireccionFormateada());
        }
        if (direccionCreateDto.getCalle() != null) {
            direccion.setCalle(direccionCreateDto.getCalle());
        }
        if (direccionCreateDto.getNumero() != null) {
            direccion.setNumero(direccionCreateDto.getNumero());
        }
        if (direccionCreateDto.getPiso() != null) {
            direccion.setPiso(direccionCreateDto.getPiso());
        }
        if (direccionCreateDto.getDepartamento() != null) {
            direccion.setDepartamento(direccionCreateDto.getDepartamento());
        }
        if (direccionCreateDto.getBarrio() != null) {
            direccion.setBarrio(direccionCreateDto.getBarrio());
        }
        if (direccionCreateDto.getCiudad() != null) {
            direccion.setCiudad(direccionCreateDto.getCiudad());
        }
        if (direccionCreateDto.getProvincia() != null) {
            direccion.setProvincia(direccionCreateDto.getProvincia());
        }
        if (direccionCreateDto.getCodigoPostal() != null) {
            direccion.setCodigoPostal(direccionCreateDto.getCodigoPostal());
        }
        if (direccionCreateDto.getPais() != null) {
            direccion.setPais(direccionCreateDto.getPais());
        }
        if (direccionCreateDto.getObservaciones() != null) {
            direccion.setObservaciones(direccionCreateDto.getObservaciones());
        }
        
        direccion.setEsPrincipal(direccionCreateDto.getEsPrincipal());

        Direccion direccionGuardada = direccionRepository.save(direccion);

        return convertirADireccionResponseDto(direccionGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public DireccionResponseDto obtenerDireccionPorId(Long id) {
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new DireccionNotFoundException("Dirección no encontrada con ID: " + id));

        return convertirADireccionResponseDto(direccion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DireccionListDto> obtenerDireccionesPorPersona(Long personaId) {
        // Verificar que la persona existe
        if (!personaRepository.existsById(personaId)) {
            throw new RuntimeException("Persona no encontrada con ID: " + personaId);
        }

        List<Direccion> direcciones = direccionRepository.findByPersonaId(personaId);
        return direcciones.stream()
                .map(this::convertirADireccionListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DireccionResponseDto obtenerDireccionPrincipalPorPersona(Long personaId) {
        // Verificar que la persona existe
        if (!personaRepository.existsById(personaId)) {
            throw new RuntimeException("Persona no encontrada con ID: " + personaId);
        }

        Direccion direccion = direccionRepository.findByPersonaIdAndEsPrincipalTrue(personaId)
                .orElseThrow(() -> new DireccionNotFoundException("No se encontró dirección principal para la persona con ID: " + personaId));

        return convertirADireccionResponseDto(direccion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DireccionListDto> obtenerDirecciones(Pageable pageable) {
        Page<Direccion> direcciones = direccionRepository.findAll(pageable);
        return direcciones.map(this::convertirADireccionListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DireccionListDto> buscarPorCiudad(String ciudad) {
        List<Direccion> direcciones = direccionRepository.findByCiudadContainingIgnoreCase(ciudad);
        return direcciones.stream()
                .map(this::convertirADireccionListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DireccionListDto> buscarPorProvincia(String provincia) {
        List<Direccion> direcciones = direccionRepository.findByProvinciaContainingIgnoreCase(provincia);
        return direcciones.stream()
                .map(this::convertirADireccionListDto)
                .collect(Collectors.toList());
    }

    @Override
    public DireccionResponseDto actualizarDireccion(Long id, DireccionUpdateDto direccionUpdateDto) {
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new DireccionNotFoundException("Dirección no encontrada con ID: " + id));

        // Si se marca como principal, desmarcar las otras direcciones de la misma persona
        if (Boolean.TRUE.equals(direccionUpdateDto.getEsPrincipal()) && !direccion.getEsPrincipal()) {
            desmarcarDireccionesPrincipales(direccion.getPersona().getId());
        }

        // Procesar datos de Google Places si están disponibles
        if (direccionUpdateDto.getGooglePlacesData() != null) {
            procesarGooglePlacesData(direccion, direccionUpdateDto.getGooglePlacesData());
        }

        // Actualizar campos específicos (tienen prioridad sobre Google Places)
        if (direccionUpdateDto.getPlaceId() != null) {
            direccion.setPlaceId(direccionUpdateDto.getPlaceId());
        }
        if (direccionUpdateDto.getLatitud() != null) {
            direccion.setLatitud(direccionUpdateDto.getLatitud());
        }
        if (direccionUpdateDto.getLongitud() != null) {
            direccion.setLongitud(direccionUpdateDto.getLongitud());
        }
        if (direccionUpdateDto.getDireccionFormateada() != null) {
            direccion.setDireccionFormateada(direccionUpdateDto.getDireccionFormateada());
        }
        if (direccionUpdateDto.getCalle() != null) {
            direccion.setCalle(direccionUpdateDto.getCalle());
        }
        if (direccionUpdateDto.getNumero() != null) {
            direccion.setNumero(direccionUpdateDto.getNumero());
        }
        if (direccionUpdateDto.getPiso() != null) {
            direccion.setPiso(direccionUpdateDto.getPiso());
        }
        if (direccionUpdateDto.getDepartamento() != null) {
            direccion.setDepartamento(direccionUpdateDto.getDepartamento());
        }
        if (direccionUpdateDto.getBarrio() != null) {
            direccion.setBarrio(direccionUpdateDto.getBarrio());
        }
        if (direccionUpdateDto.getCiudad() != null) {
            direccion.setCiudad(direccionUpdateDto.getCiudad());
        }
        if (direccionUpdateDto.getProvincia() != null) {
            direccion.setProvincia(direccionUpdateDto.getProvincia());
        }
        if (direccionUpdateDto.getCodigoPostal() != null) {
            direccion.setCodigoPostal(direccionUpdateDto.getCodigoPostal());
        }
        if (direccionUpdateDto.getPais() != null) {
            direccion.setPais(direccionUpdateDto.getPais());
        }
        if (direccionUpdateDto.getObservaciones() != null) {
            direccion.setObservaciones(direccionUpdateDto.getObservaciones());
        }
        
        if (direccionUpdateDto.getEsPrincipal() != null) {
            direccion.setEsPrincipal(direccionUpdateDto.getEsPrincipal());
        }

        Direccion direccionActualizada = direccionRepository.save(direccion);

        return convertirADireccionResponseDto(direccionActualizada);
    }

    @Override
    public void eliminarDireccion(Long id) {
        if (!direccionRepository.existsById(id)) {
            throw new DireccionNotFoundException("Dirección no encontrada con ID: " + id);
        }
        direccionRepository.deleteById(id);
    }

    @Override
    public DireccionResponseDto marcarComoPrincipal(Long id) {
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new DireccionNotFoundException("Dirección no encontrada con ID: " + id));

        // Desmarcar todas las direcciones de la persona como principales
        desmarcarDireccionesPrincipales(direccion.getPersona().getId());

        // Marcar esta dirección como principal
        direccion.setEsPrincipal(true);
        Direccion direccionActualizada = direccionRepository.save(direccion);

        return convertirADireccionResponseDto(direccionActualizada);
    }

    /**
     * Método auxiliar para desmarcar todas las direcciones principales de una persona
     */
    private void desmarcarDireccionesPrincipales(Long personaId) {
        List<Direccion> direcciones = direccionRepository.findByPersonaId(personaId);
        direcciones.forEach(d -> d.setEsPrincipal(false));
        direccionRepository.saveAll(direcciones);
    }

    /**
     * Procesa los datos de Google Places y los asigna a la dirección
     */
    private void procesarGooglePlacesData(Direccion direccion, GooglePlacesDto googlePlacesData) {
        // Establecer Place ID
        if (googlePlacesData.getPlaceId() != null) {
            direccion.setPlaceId(googlePlacesData.getPlaceId());
        }

        // Establecer dirección formateada
        if (googlePlacesData.getFormattedAddress() != null) {
            direccion.setDireccionFormateada(googlePlacesData.getFormattedAddress());
        }

        // Extraer coordenadas
        Double[] coordinates = GooglePlacesParser.extractCoordinates(googlePlacesData);
        if (coordinates != null) {
            direccion.setLatitud(coordinates[0]);
            direccion.setLongitud(coordinates[1]);
        }

        // Extraer componentes de dirección
        Map<String, String> components = GooglePlacesParser.extractAddressComponents(googlePlacesData);
        
        if (components.containsKey("calle")) {
            direccion.setCalle(components.get("calle"));
        }
        if (components.containsKey("numero")) {
            direccion.setNumero(components.get("numero"));
        }
        if (components.containsKey("barrio")) {
            direccion.setBarrio(components.get("barrio"));
        }
        if (components.containsKey("ciudad")) {
            direccion.setCiudad(components.get("ciudad"));
        }
        if (components.containsKey("provincia")) {
            direccion.setProvincia(components.get("provincia"));
        }
        if (components.containsKey("pais")) {
            direccion.setPais(components.get("pais"));
        }
        if (components.containsKey("codigoPostal")) {
            direccion.setCodigoPostal(components.get("codigoPostal"));
        }
    }

    private DireccionResponseDto convertirADireccionResponseDto(Direccion direccion) {
        return new DireccionResponseDto(
                direccion.getId(),
                direccion.getPersona().getId(),
                direccion.getPersona().getNombreCompleto(),
                direccion.getPlaceId(),
                direccion.getLatitud(),
                direccion.getLongitud(),
                direccion.getDireccionFormateada(),
                direccion.getCalle(),
                direccion.getNumero(),
                direccion.getPiso(),
                direccion.getDepartamento(),
                direccion.getBarrio(),
                direccion.getCiudad(),
                direccion.getProvincia(),
                direccion.getCodigoPostal(),
                direccion.getPais(),
                direccion.getObservaciones(),
                direccion.getEsPrincipal(),
                direccion.getDireccionCompleta(),
                direccion.tieneUbicacion()
        );
    }

    private DireccionListDto convertirADireccionListDto(Direccion direccion) {
        return new DireccionListDto(
                direccion.getId(),
                direccion.getPlaceId(),
                direccion.getCalle(),
                direccion.getNumero(),
                direccion.getCiudad(),
                direccion.getProvincia(),
                direccion.getPais(),
                direccion.getEsPrincipal(),
                direccion.getDireccionCompleta(),
                direccion.getLatitud(),
                direccion.getLongitud()
        );
    }
}

