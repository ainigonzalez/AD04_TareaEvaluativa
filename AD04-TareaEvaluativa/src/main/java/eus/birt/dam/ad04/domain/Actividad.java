package eus.birt.dam.ad04.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "actividad")
public class Actividad {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String localidad;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "plazas_totales", nullable = false)
    private int plazasTotales;

    @Column(name = "plazas_disponibles", nullable = false)
    private int plazasDisponibles;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    protected Actividad() { }

    public Actividad(String nombre, String localidad, LocalDateTime fechaHora,
                     int plazasTotales, Monitor monitor) {
        if (plazasTotales <= 0) {
            throw new IllegalArgumentException("Las plazas totales deben ser positivas.");
        }
        this.nombre = nombre;
        this.localidad = localidad;
        this.fechaHora = fechaHora;
        this.plazasTotales = plazasTotales;
        this.plazasDisponibles = plazasTotales; // al crearla coinciden
        this.monitor = monitor;
    }

    /** Descuenta plazas; falla si no hay suficientes. */
    public void reservarPlazas(int plazas) {
        if (plazas <= 0) {
            throw new IllegalArgumentException("El numero de plazas debe ser positivo.");
        }
        if (plazas > plazasDisponibles) {
            throw new IllegalStateException("No hay plazas suficientes.");
        }
        plazasDisponibles -= plazas;
    }

    /** Devuelve plazas sin superar el total. */
    public void liberarPlazas(int plazas) {
        if (plazas <= 0 || plazasDisponibles + plazas > plazasTotales) {
            throw new IllegalStateException("Devolucion de plazas incoherente.");
        }
        plazasDisponibles += plazas;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLocalidad() { return localidad; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public int getPlazasTotales() { return plazasTotales; }
    public int getPlazasDisponibles() { return plazasDisponibles; }
    public Monitor getMonitor() { return monitor; }
}
