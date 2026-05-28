package com.experienzia.repository;

import com.experienzia.entity.Estado;
import com.experienzia.entity.Rol;
import com.experienzia.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Acá meto todo lo de usuarios contra la BD sin escribir SQL a mano en cada lado (Spring Data me ayuda bastante)
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    // Lo uso en el login: busco por correo y si no hay nadie pues el front muestra que falló
    Optional<Usuario> findByEmail(String email);

    // Antes de registrar reviso que el email no exista ya, si no el usuario se confunde en el front
    boolean existsByEmail(String email);

    // Igual con la cédula, no pueden haber dos personas con el mismo documento en la BD
    boolean existsByNumeroDocumento(String numeroDocumento);

    // Y el teléfono también es único en mi app (para el perfil y registro)
    boolean existsByTelefono(String telefono);

    // Para mandar notificaciones a todos los admins cuando hay algo pendiente de revisión
    List<Usuario> findByRolAndEstado(Rol rol, Estado estado);

    // Estos tres los armé con @Query porque en los reportes del admin necesito contar sin traer todos los usuarios a memoria (eso me reventaba la RAM en teoría)
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado = :estado")
    long countUsuariosByEstado(@Param("estado") Estado estado);

    // Cuántos hay por rol, por ejemplo cuántos asistentes hay en total
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol")
    long countUsuariosByRol(@Param("rol") Rol rol);

    // Los dos filtros juntos, tipo organizadores que estén ACTIVOS nada más
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol AND u.estado = :estado")
    long countUsuariosByRolAndEstado(@Param("rol") Rol rol, @Param("estado") Estado estado);
}
