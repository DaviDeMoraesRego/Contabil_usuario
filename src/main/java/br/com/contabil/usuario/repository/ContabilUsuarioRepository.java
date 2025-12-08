package br.com.contabil.usuario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.contabil.usuario.entity.ContabilUsuarioEntity;

@Repository
public interface ContabilUsuarioRepository extends JpaRepository<ContabilUsuarioEntity, String> {

	@Query(value = "SELECT * FROM contabil.tab_usuario WHERE clerk_id = :clerkId", nativeQuery = true)
	Optional<ContabilUsuarioEntity> findByClerkId(@Param("clerkId") String clerkId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM contabil.tab_usuario WHERE clerk_id = :clerkId", nativeQuery = true)
	void deleteByClerkId(@Param("clerkId") String clerkId);

	@Modifying
	@Transactional
	@Query(value = "UPDATE contabil.tab_usuario SET points = :points, hearts = :hearts WHERE clerk_id = :clerkId", nativeQuery = true)
	int updatePointsAndHearts(@Param("clerkId") String clerkId, @Param("hearts") int hearts,
			@Param("points") int points);
}
