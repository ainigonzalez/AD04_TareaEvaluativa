package eus.birt.dam.ad04.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monitor")
public class Monitor {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String especialidad;

    @OneToMany(mappedBy = "monitor")
    private List<Actividad> actividades = new ArrayList<>();

    protected Monitor() { }

    public Monitor(String nombre, String especialidad) {
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    public void addActividad(Actividad actividad) {
        if (actividad.getMonitor() != this) {
            throw new IllegalArgumentException("La actividad pertenece a otro monitor.");
        }
        if (!actividades.contains(actividad)) {
            actividades.add(actividad);
        }
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEspecialidad() { return especialidad; }
    public List<Actividad> getActividades() { return actividades; }
}
