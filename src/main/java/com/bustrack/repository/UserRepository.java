package com.bustrack.repository;

import com.bustrack.model.Role;
import com.bustrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleOrderByNameAsc(Role role);

    long countByRole(Role role);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.bus = null, u.boardingStop = null where u.bus.id = :busId")
    void clearBus(@Param("busId") Long busId);
}
