package eus.birt.dam.ad04.main;

import eus.birt.dam.ad04.config.JpaUtil;
import eus.birt.dam.ad04.domain.Actividad;
import eus.birt.dam.ad04.service.ActivaService;
import eus.birt.dam.ad04.service.ActivaService.ResultadoCancelacion;
import eus.birt.dam.ad04.service.ActivaService.ResultadoReserva;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Scanner;

public class MainApp {

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        try (EntityManagerFactory emf = JpaUtil.crearFactory()) {
            ActivaService servicio = new ActivaService(emf);
            boolean salir = false;
            while (!salir) {
                mostrarMenu();
                String opcion = SC.nextLine().trim();
                try {
                    switch (opcion) {
                        case "1" -> crearMonitor(servicio);
                        case "2" -> crearActividad(servicio);
                        case "3" -> buscarDisponibles(servicio);
                        case "4" -> crearCliente(servicio);
                        case "5" -> crearReserva(servicio);
                        case "6" -> cancelarReserva(servicio);
                        case "7" -> listarActividades(servicio);
                        case "8" -> {
                            salir = true;
                            System.out.println("Hasta pronto.");
                        }
                        default -> System.out.println("Opcion no valida.");
                    }
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    // Errores de entrada o de reglas de negocio
                    System.out.println("No se ha podido completar la operacion: " + ex.getMessage());
                } catch (RuntimeException ex) {
                    // Fallos de persistencia u otros
                    System.out.println("Error inesperado al guardar. No se han aplicado cambios.");
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("=== ACTIVA · Gestión de actividades ===");
        System.out.println("1. Crear monitor");
        System.out.println("2. Crear actividad");
        System.out.println("3. Buscar actividades disponibles");
        System.out.println("4. Crear cliente");
        System.out.println("5. Crear reserva");
        System.out.println("6. Cancelar reserva");
        System.out.println("7. Listar actividades");
        System.out.println("8. Salir");
        System.out.print("Opcion: ");
    }

    // ---------- Opciones ----------
    private static void crearMonitor(ActivaService s) {
        String nombre = pedirTexto("Nombre: ");
        String especialidad = pedirTexto("Especialidad: ");
        Long id = s.crearMonitor(nombre, especialidad);
        System.out.println("Monitor creado con ID " + id + ".");
    }

    private static void crearActividad(ActivaService s) {
        String nombre = pedirTexto("Nombre: ");
        String localidad = pedirTexto("Localidad: ");
        System.out.println("Formato de fecha y hora: dd/MM/yyyy HH:mm (por ejemplo 15/11/2026 10:00)");
        LocalDateTime fechaHora = pedirFechaHora("Fecha y hora: ");
        int plazas = pedirEntero("Plazas totales: ");
        Long monitorId = pedirLong("ID del monitor: ");
        Long id = s.crearActividad(nombre, localidad, fechaHora, plazas, monitorId);
        System.out.println("Actividad creada con ID " + id + ".");
    }

    private static void buscarDisponibles(ActivaService s) {
        String localidad = pedirTexto("Localidad: ");
        System.out.println("Formato de fecha: dd/MM/yyyy (por ejemplo 15/11/2026)");
        LocalDate dia = pedirFecha("Dia: ");
        int plazas = pedirEntero("Plazas necesarias: ");
        List<Actividad> resultado = s.buscarDisponibles(localidad, dia, plazas);
        if (resultado.isEmpty()) {
            System.out.println("No hay actividades disponibles con esos criterios.");
            return;
        }
        System.out.println("ID | Nombre | Fecha y hora | Monitor | Plazas disponibles");
        for (Actividad a : resultado) {
            System.out.printf("%d | %s | %s | %s | %d%n",
                    a.getId(), a.getNombre(), a.getFechaHora().format(FORMATO_FECHA_HORA),
                    a.getMonitor().getNombre(), a.getPlazasDisponibles());
        }
    }

    private static void crearCliente(ActivaService s) {
        String nombre = pedirTexto("Nombre: ");
        String email = pedirTexto("Email: ");
        Long id = s.crearCliente(nombre, email);
        System.out.println("Cliente creado con ID " + id + ".");
    }

    private static void crearReserva(ActivaService s) {
        Long clienteId = pedirLong("ID del cliente: ");
        Long actividadId = pedirLong("ID de la actividad: ");
        int plazas = pedirEntero("Numero de plazas: ");
        ResultadoReserva r = s.crearReserva(clienteId, actividadId, plazas);
        System.out.println("Reserva creada con ID " + r.reservaId()
                + ". Plazas restantes: " + r.plazasRestantes() + ".");
    }

    private static void cancelarReserva(ActivaService s) {
        Long reservaId = pedirLong("ID de la reserva: ");
        ResultadoCancelacion r = s.cancelarReserva(reservaId);
        System.out.println("Reserva cancelada. Plazas recuperadas: " + r.plazasRecuperadas()
                + ". Plazas disponibles: " + r.plazasDisponibles() + ".");
    }

    private static void listarActividades(ActivaService s) {
        List<Actividad> lista = s.listarActividades();
        if (lista.isEmpty()) {
            System.out.println("No hay actividades.");
            return;
        }
        System.out.println("ID | Nombre | Localidad | Fecha y hora | Monitor | Totales | Disponibles");
        for (Actividad a : lista) {
            System.out.printf("%d | %s | %s | %s | %s | %d | %d%n",
                    a.getId(), a.getNombre(), a.getLocalidad(),
                    a.getFechaHora().format(FORMATO_FECHA_HORA), a.getMonitor().getNombre(),
                    a.getPlazasTotales(), a.getPlazasDisponibles());
        }
    }

    // ---------- Lectura de datos con validación ----------
    private static String pedirTexto(String mensaje) {
        System.out.print(mensaje);
        return SC.nextLine();
    }

    private static int pedirEntero(String mensaje) {
        System.out.print(mensaje);
        try {
            return Integer.parseInt(SC.nextLine().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Debes introducir un numero entero.");
        }
    }

    private static Long pedirLong(String mensaje) {
        System.out.print(mensaje);
        try {
            return Long.parseLong(SC.nextLine().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Debes introducir un identificador numerico.");
        }
    }

    private static LocalDateTime pedirFechaHora(String mensaje) {
        System.out.print(mensaje);
        try {
            return LocalDateTime.parse(SC.nextLine().trim(), FORMATO_FECHA_HORA);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Fecha y hora invalidas. Usa dd/MM/yyyy HH:mm.");
        }
    }

    private static LocalDate pedirFecha(String mensaje) {
        System.out.print(mensaje);
        try {
            return LocalDate.parse(SC.nextLine().trim(), FORMATO_FECHA);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Fecha invalida. Usa dd/MM/yyyy.");
        }
    }
}