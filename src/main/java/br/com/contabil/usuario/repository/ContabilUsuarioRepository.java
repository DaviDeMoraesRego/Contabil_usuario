package br.com.contabil.usuario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.contabil.usuario.entity.ContabilUsuarioEntity;

@Repository
public interface ContabilUsuarioRepository extends JpaRepository<ContabilUsuarioEntity, String> {

	@Query("""
			    SELECT u
			    FROM ContabilUsuarioEntity u
			    WHERE u.clerkId = :clerkId
			""")
	Optional<ContabilUsuarioEntity> findByClerkId(@Param("clerkId") String clerkId);

	@Modifying
	@Transactional
	@Query("""
			    DELETE FROM ContabilUsuarioEntity u
			    WHERE u.clerkId = :clerkId
			""")
	void deleteByClerkId(@Param("clerkId") String clerkId);

	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("""
			    UPDATE ContabilUsuarioEntity u
			    SET u.points = :points,
			        u.hearts = :hearts
			    WHERE u.clerkId = :clerkId
			""")
	int updatePointsAndHearts(@Param("clerkId") String clerkId, @Param("hearts") int hearts,
			@Param("points") int points);

	@Query("SELECT u FROM ContabilUsuarioEntity u ORDER BY u.points DESC")
	List<ContabilUsuarioEntity> findTop200ByOrderByPointsDesc(Pageable pageable);

	@Query(value = "SELECT COUNT(*) + 1 FROM contabil.tab_usuario WHERE points > "
			+ "(SELECT points FROM contabil.tab_usuario WHERE clerk_id = :clerkId)", nativeQuery = true)
	Optional<Integer> findUserRank(@Param("clerkId") String clerkId);
}
