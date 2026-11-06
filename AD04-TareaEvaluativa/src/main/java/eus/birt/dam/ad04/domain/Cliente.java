package eus.birt.dam.ad04.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente",
       uniqueConstraints = @UniqueConstraint(name = "uk_cliente_email", columnNames = "email"))
public class Cliente {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(nullable = false, length = 100)
	    private String nombre;

	    @Column(nullable = false, length = 150)
	    private String email;

	    protected Cliente() { }

	    public Cliente(String nombre, String email) {
	        this.nombre = nombre;
	        this.email = email;
	    }

	    public Long getId() { return id; }
	    public String getNombre() { return nombre; }
	    public String getEmail() { return email; }
}
