package eus.birt.dam.ad04.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reserva")
public class Reserva {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "numero_plazas", nullable = false)
    private int numeroPlazas;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    protected Reserva() { }

    public Reserva(Cliente cliente, Actividad actividad, int numeroPlazas,
                   LocalDateTime fechaReserva) {
        if (numeroPlazas <= 0) {
            throw new IllegalArgumentException("El numero de plazas debe ser positivo.");
        }
        this.cliente = cliente;
        this.actividad = actividad;
        this.numeroPlazas = numeroPlazas;
        this.fechaReserva = fechaReserva;
    }

    public Long getId() { return id; }
    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public int getNumeroPlazas() { return numeroPlazas; }
    public Actividad getActividad() { return actividad; }
    public Cliente getCliente() { return cliente; }
}
