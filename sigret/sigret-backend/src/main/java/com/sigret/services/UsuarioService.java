package com.sigret.services;

import com.sigret.dtos.usuario.*;
import com.sigret.entities.Empleado;
import com.sigret.entities.Persona;
import com.sigret.entities.Usuario;
import com.sigret.exception.EmpleadoAlreadyHasUserException;
import com.sigret.exception.UsernameAlreadyExistsException;
import com.sigret.exception.UsuarioNotFoundException;
import com.sigret.repositories.EmpleadoRepository;
import com.sigret.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Crear un nuevo usuario
     */
    public UsuarioResponseDto crearUsuario(UsuarioCreateDto usuarioCreateDto) {
        // Validar que el empleado existe y no tiene usuario asociado
        Empleado empleado = empleadoRepository.findById(usuarioCreateDto.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + usuarioCreateDto.getEmpleadoId()));

        if (empleado.getUsuario() != null) {
            throw new EmpleadoAlreadyHasUserException("El empleado ya tiene un usuario asociado");
        }

        // Validar que el username no existe
        if (usuarioRepository.existsByUsername(usuarioCreateDto.getUsername())) {
            throw new UsernameAlreadyExistsException("El username ya está en uso");
        }

        // Crear el usuario
        Usuario usuario = new Usuario();
        usuario.setEmpleado(empleado);
        usuario.setUsername(usuarioCreateDto.getUsername());
        usuario.setPassword(passwordEncoder.encode(usuarioCreateDto.getPassword()));
        usuario.setRol(usuarioCreateDto.getRol());
        usuario.setActivo(usuarioCreateDto.getActivo() != null ? usuarioCreateDto.getActivo() : true);
        usuario.setFechaCreacion(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return convertirAUsuarioResponseDto(usuarioGuardado);
    }

    /**
     * Obtener un usuario por ID
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDto obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

        return convertirAUsuarioResponseDto(usuario);
    }

    /**
     * Obtener todos los usuarios con paginación
     */
    @Transactional(readOnly = true)
    public Page<UsuarioListDto> obtenerUsuarios(Pageable pageable) {
        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);
        return usuarios.map(this::convertirAUsuarioListDto);
    }

    /**
     * Obtener todos los usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UsuarioListDto> obtenerUsuariosActivos() {
        List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(Usuario::esActivo)
                .collect(Collectors.toList());
        
        return usuarios.stream()
                .map(this::convertirAUsuarioListDto)
                .collect(Collectors.toList());
    }

    /**
     * Buscar usuarios por username
     */
    @Transactional(readOnly = true)
    public List<UsuarioListDto> buscarUsuariosPorUsername(String username) {
        List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(username.toLowerCase()))
                .collect(Collectors.toList());
        
        return usuarios.stream()
                .map(this::convertirAUsuarioListDto)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar un usuario
     */
    public UsuarioResponseDto actualizarUsuario(Long id, UsuarioUpdateDto usuarioUpdateDto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

        // Actualizar contraseña si se proporciona
        if (usuarioUpdateDto.getPassword() != null && !usuarioUpdateDto.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioUpdateDto.getPassword()));
        }

        // Actualizar rol si se proporciona
        if (usuarioUpdateDto.getRol() != null) {
            usuario.setRol(usuarioUpdateDto.getRol());
        }

        // Actualizar estado activo si se proporciona
        if (usuarioUpdateDto.getActivo() != null) {
            usuario.setActivo(usuarioUpdateDto.getActivo());
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return convertirAUsuarioResponseDto(usuarioActualizado);
    }

    /**
     * Desactivar un usuario (soft delete)
     */
    public void desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Activar un usuario
     */
    public void activarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    /**
     * Eliminar un usuario (hard delete)
     */
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNotFoundException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Cambiar contraseña de un usuario
     */
    public void cambiarPassword(Long id, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    /**
     * Verificar si un username está disponible
     */
    @Transactional(readOnly = true)
    public boolean isUsernameDisponible(String username) {
        return !usuarioRepository.existsByUsername(username);
    }

    /**
     * Obtener usuario por username
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDto obtenerUsuarioPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con username: " + username));
        
        return convertirAUsuarioResponseDto(usuario);
    }

    /**
     * Convertir Usuario a UsuarioResponseDto
     */
    private UsuarioResponseDto convertirAUsuarioResponseDto(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getEmpleado().getId(),
                usuario.getEmpleado().getNombreCompleto(),
                usuario.getRol(),
                usuario.getActivo(),
                usuario.getFechaCreacion(),
                usuario.getUltimoLogin()
        );
    }

    /**
     * Convertir Usuario a UsuarioListDto
     */
    private UsuarioListDto convertirAUsuarioListDto(Usuario usuario) {
        return new UsuarioListDto(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getEmpleado().getNombreCompleto(),
                usuario.getRol(),
                usuario.getActivo(),
                usuario.getFechaCreacion(),
                usuario.getUltimoLogin()
        );
    }

    /**
     * Obtener perfil del usuario autenticado
     */
    @Transactional(readOnly = true)
    public PerfilResponseDto obtenerPerfil(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con username: " + username));

        Empleado empleado = usuario.getEmpleado();
        Persona persona = empleado.getPersona();

        return new PerfilResponseDto(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.getActivo(),
                usuario.getFechaCreacion(),
                usuario.getUltimoLogin(),
                empleado.getId(),
                persona.getNombreCompleto(),
                persona.getNombre(),
                persona.getApellido(),
                persona.getDocumento(),
                persona.getTipoDocumento().getDescripcion(),
                persona.getSexo(),
                empleado.getTipoEmpleado().getDescripcion(),
                empleado.getActivo()
        );
    }

    /**
     * Cambiar contraseña del usuario autenticado
     */
    public void cambiarPasswordAutenticado(String username, CambiarPasswordDto cambiarPasswordDto) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con username: " + username));

        // Validar que las contraseñas nuevas coincidan
        if (!cambiarPasswordDto.getPasswordNueva().equals(cambiarPasswordDto.getPasswordNuevaConfirmacion())) {
            throw new RuntimeException("Las contraseñas nuevas no coinciden");
        }

        // Validar contraseña actual
        if (!passwordEncoder.matches(cambiarPasswordDto.getPasswordActual(), usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(cambiarPasswordDto.getPasswordNueva(), usuario.getPassword())) {
            throw new RuntimeException("La nueva contraseña debe ser diferente a la actual");
        }

        // Cambiar la contraseña
        usuario.setPassword(passwordEncoder.encode(cambiarPasswordDto.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }
}
