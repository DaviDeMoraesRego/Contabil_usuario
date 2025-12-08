package br.com.contabil.usuario.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Data
@Entity
@Table(name = "tab_usuario", schema = "contabil")
public class ContabilUsuarioEntity implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="clerk_id")
	private String clerkId;
	
	@Column(name = "nome")
	private String nome;
	
	@Column(name = "email", nullable = false)
	private String email;
	
	@Column(name = "user_img_src")
	private String userImgSrc;

	@Column(name = "active_course", nullable = false)
	private int activeCourse;

	@Column(name = "hearts", nullable = false)
	private int hearts;

	@Column(name = "points", nullable = false)
	private int points;
	
	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "dthr_create", nullable = false, updatable = false)
	private Date dthr_create;
	
	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "dthr_update", nullable = false)
	private Date dthr_update;
	
	@PrePersist
	public void prePersist() {
		Date date = new Date();
		this.dthr_create = date;
		this.dthr_update = date;
	}
	
	@PreUpdate
	public void preupdate() {
		this.dthr_update = new Date();
	}
}
