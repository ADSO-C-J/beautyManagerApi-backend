package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
    JpaRepository<User, UUID>
    Es una interfaz de Spring que ya viene con métodos CRUD básicos
    (crear, leer, actualizar, borrar).
*/

/*
    @Repository
    Anotación que le dice a Spring: "Esta interface gestiona datos y debe ser gestionada por Spring".
*/


/*•
    interface en lugar de class:
    ◦ En Spring Data JPA, solo necesitamos interfaces.
    ◦ Spring genera automáticamente la implementación basada en los nombres de los métodos.
*/

/*
    Por heredar de JpaRepository, automáticamente tienes estos métodos sin escribir nada:
    save(User user):
    Guarda o actualiza un usuario

    findById(UUID id):
    Busca por ID

    findAll():
    Obtiene todos los usuarios

    deleteById(UUID id):
    Elimina por ID

    count():
    Cuenta cuántos usuarios hay

    existsById(UUID id):
    Verifica si existe un usuario
 */

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Solo usuarios activos (soft delete)
    List<User> findAllByDeletedAtIsNull();

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    List<User> findAllByRoleAndDeletedAtIsNull(UserRole role);
}
