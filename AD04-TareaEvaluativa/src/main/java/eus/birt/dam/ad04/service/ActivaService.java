package eus.birt.dam.ad04.service;

import eus.birt.dam.ad04.domain.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ActivaService {

    private final EntityManagerFactory emf;

    public ActivaService(EntityManagerFactory emf) {
        this.emf = Objects.requireNonNull(emf);
    }

    /** Resultado de reservar, para mostrarlo solo tras el commit. */
    public record ResultadoReserva(Long reservaId, int plazasRestantes) { }

    /** Resultado de cancelar. */
    public record ResultadoCancelacion(int plazasRecuperadas, int plazasDisponibles) { }

    // ---------- 1. Crear monitor ----------
    public Long crearMonitor(String nombre, String especialidad) {
        String n = textoObligatorio(nombre, "El nombre");
        String e = textoObligatorio(especialidad, "La especialidad");
        Monitor monitor = new Monitor(n, e);
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(monitor);
                tx.commit();
                return monitor.getId();
            } catch (RuntimeException ex) {
                deshacer(tx, ex);
                throw ex;
            }
        }
    }

    // ---------- 2. Crear actividad ----------
    public Long crearActividad(String nombre, String localidad, LocalDateTime fechaHora,
                               int plazasTotales, Long monitorId) {
        String n = textoObligatorio(nombre, "El nombre");
        String l = textoObligatorio(localidad, "La localidad");
        Objects.requireNonNull(fechaHora, "Fecha obligatoria");
        if (plazasTotales <= 0) {
            throw new IllegalArgumentException("Las plazas totales deben ser un entero positivo.");
        }
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Monitor monitor = em.find(Monitor.class, monitorId);
                if (monitor == null) {
                    throw new IllegalArgumentException("No existe el monitor " + monitorId + ".");
                }
                Actividad actividad = new Actividad(n, l, fechaHora, plazasTotales, monitor);
                monitor.addActividad(actividad); // mantiene el lado inverso
                em.persist(actividad);
                tx.commit();
                return actividad.getId();
            } catch (RuntimeException ex) {
                deshacer(tx, ex);
                throw ex;
            }
        }
    }

    // ---------- 3. Buscar actividades disponibles ----------
    public List<Actividad> buscarDisponibles(String localidad, LocalDate dia, int plazas) {
        String l = textoObligatorio(localidad, "La localidad");
        if (plazas <= 0) {
            throw new IllegalArgumentException("Las plazas deben ser una cantidad positiva.");
        }
        LocalDateTime desde = dia.atStartOfDay();
        LocalDateTime hasta = dia.plusDays(1).atStartOfDay();
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("""
                    SELECT a FROM Actividad a JOIN FETCH a.monitor
                    WHERE LOWER(a.localidad) = LOWER(:localidad)
                      AND a.fechaHora >= :desde AND a.fechaHora < :hasta
                      AND a.plazasDisponibles >= :plazas
                    ORDER BY a.fechaHora, a.id
                    """, Actividad.class)
                    .setParameter("localidad", l)
                    .setParameter("desde", desde)
                    .setParameter("hasta", hasta)
                    .setParameter("plazas", plazas)
                    .getResultList();
        }
    }

    // ---------- 4. Crear cliente ----------
    public Long crearCliente(String nombre, String email) {
        String n = textoObligatorio(nombre, "El nombre");
        String correo = Objects.requireNonNullElse(email, "").trim().toLowerCase(Locale.ROOT);
        if (correo.length() > 150 || !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El correo no tiene un formato valido.");
        }
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Long existentes = em.createQuery(
                        "SELECT COUNT(c) FROM Cliente c WHERE LOWER(c.email) = :email", Long.class)
                        .setParameter("email", correo)
                        .getSingleResult();
                if (existentes > 0) {
                    throw new IllegalArgumentException("Ya existe un cliente con ese correo.");
                }
                Cliente cliente = new Cliente(n, correo);
                em.persist(cliente);
                tx.commit();
                return cliente.getId();
            } catch (RuntimeException ex) {
                deshacer(tx, ex);
                throw ex;
            }
        }
    }

    // ---------- 5. Crear reserva (transaccional) ----------
    public ResultadoReserva crearReserva(Long clienteId, Long actividadId, int plazas) {
        if (plazas <= 0) {
            throw new IllegalArgumentException("El numero de plazas debe ser positivo.");
        }
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Cliente cliente = em.find(Cliente.class, clienteId);
                if (cliente == null) {
                    throw new IllegalArgumentException("No existe el cliente " + clienteId + ".");
                }
                Actividad actividad = em.find(Actividad.class, actividadId);
                if (actividad == null) {
                    throw new IllegalArgumentException("No existe la actividad " + actividadId + ".");
                }
                actividad.reservarPlazas(plazas); // comprueba y descuenta
                Reserva reserva = new Reserva(cliente, actividad, plazas, LocalDateTime.now());
                em.persist(reserva);
                tx.commit(); // reserva y plazas se confirman juntas
                return new ResultadoReserva(reserva.getId(), actividad.getPlazasDisponibles());
            } catch (RuntimeException ex) {
                deshacer(tx, ex);
                throw ex;
            }
        }
    }

    // ---------- 6. Cancelar reserva (transaccional) ----------
    public ResultadoCancelacion cancelarReserva(Long reservaId) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Reserva reserva = em.find(Reserva.class, reservaId);
                if (reserva == null) {
                    throw new IllegalArgumentException("No existe la reserva " + reservaId + ".");
                }
                Actividad actividad = reserva.getActividad();
                int plazas = reserva.getNumeroPlazas();
                actividad.liberarPlazas(plazas);
                em.remove(reserva); // solo la reserva; sin cascadas
                tx.commit();
                return new ResultadoCancelacion(plazas, actividad.getPlazasDisponibles());
            } catch (RuntimeException ex) {
                deshacer(tx, ex);
                throw ex;
            }
        }
    }

    // ---------- 7. Listar actividades ----------
    public List<Actividad> listarActividades() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("""
                    SELECT a FROM Actividad a JOIN FETCH a.monitor
                    ORDER BY a.fechaHora, a.id
                    """, Actividad.class).getResultList();
        }
    }

    
    private static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacio.");
        }
        return valor.trim();
    }

    private static void deshacer(EntityTransaction tx, RuntimeException original) {
        if (tx.isActive()) {
            try {
                tx.rollback();
            } catch (RuntimeException errorRollback) {
                original.addSuppressed(errorRollback);
            }
        }
    }
}