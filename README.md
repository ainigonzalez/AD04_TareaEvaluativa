# AD04 · Activa: reservas de actividades guiadas

Aplicación Java para gestionar monitores, actividades, clientes y
reservas, con persistencia en MySQL mediante **JPA con Hibernate como proveedor**.

- **Ruta elegida:** JPA (`EntityManagerFactory`, `EntityManager`, `META-INF/persistence.xml`).

## Versiones

| Componente | Versión |
|---|---|
| Java (JDK) | 21 (Temurin 21.0.6) |
| Maven | 3.9.11 |
| Jakarta Persistence | 3.2.0 |
| Hibernate ORM | 7.4.12.Final |
| MySQL Connector/J | 9.7.0 |
| MySQL Server | 8.0.x |

## Estructura del proyecto

```
sql/dbactiva.sql                      Script de la base de datos (tablas y datos iniciales)
src/main/resources/META-INF/          persistence.xml (hbm2ddl.auto=validate)
src/main/java/eus/birt/dam/ad04/
    config/    JpaUtil (crea la EntityManagerFactory)
    domain/    Monitor, Actividad, Cliente, Reserva (entidades JPA)
    service/   ActivaService (consultas, reglas de negocio y transacciones)
    main/      MainApp (menú de consola)
```

## Preparación de la base de datos

1. Arranca MySQL y ejecuta **una sola vez** `sql/dbactiva.sql` con un usuario con
   permisos de administración (por ejemplo, desde MySQL Workbench).
2. El script crea el esquema `dbactiva`, sus cuatro tablas y los datos iniciales
   (3 monitores, 4 actividades, 2 clientes; las reservas empiezan vacías).
3. Hibernate funciona con `hibernate.hbm2ddl.auto=validate`: **comprueba** las
   tablas pero nunca las crea ni las modifica.

## Configuración de la conexión

Por defecto se conecta a `jdbc:mysql://localhost:3306/dbactiva` con el usuario
`root`. **La contraseña no está en el repositorio**: se lee de una variable de
entorno.

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_PASSWORD` | Sí | Contraseña del usuario MySQL |
| `DB_USER` | No | Usuario MySQL (sustituye al de `persistence.xml`) |
| `DB_URL` | No | URL JDBC (sustituye a la de `persistence.xml`) |

## Ejecución

1. **File > Import > Maven > Existing Maven Projects** y selecciona la carpeta.
2. **Run > Run Configurations > Java Application > MainApp > pestaña Environment**
   y añade `DB_PASSWORD` con tu contraseña local.
3. Ejecuta `MainApp`.