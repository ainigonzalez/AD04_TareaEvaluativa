-- AD04 Activa | MySQL 8.0.16+ / 8.4 | Ejecutar con permisos de administracion.
-- Prepara la BD, las tablas y los datos iniciales. No contiene DROP ni borra datos existentes.
CREATE DATABASE IF NOT EXISTS dbactiva
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE dbactiva;

-- Crear las cuatro tablas con sus claves y restricciones en una BD vacia.
-- Si ya existen estas tablas, el error evita sobrescribir trabajo existente.
USE dbactiva;

CREATE TABLE monitor (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  especialidad VARCHAR(100) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE actividad (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  localidad VARCHAR(100) NOT NULL,
  fecha_hora DATETIME(6) NOT NULL,
  plazas_totales INT NOT NULL,
  plazas_disponibles INT NOT NULL,
  monitor_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_actividad_monitor FOREIGN KEY (monitor_id) REFERENCES monitor(id),
  CONSTRAINT chk_actividad_totales CHECK (plazas_totales > 0),
  CONSTRAINT chk_actividad_disponibles CHECK (plazas_disponibles >= 0 AND plazas_disponibles <= plazas_totales)
) ENGINE=InnoDB;

CREATE TABLE cliente (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_cliente_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE reserva (
  id BIGINT NOT NULL AUTO_INCREMENT,
  fecha_reserva DATETIME(6) NOT NULL,
  numero_plazas INT NOT NULL,
  actividad_id BIGINT NOT NULL,
  cliente_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_reserva_actividad FOREIGN KEY (actividad_id) REFERENCES actividad(id),
  CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
  CONSTRAINT chk_reserva_plazas CHECK (numero_plazas > 0)
) ENGINE=InnoDB;

-- Datos ficticios y fechas fijas para hacer reproducibles las pruebas.
-- Ejecutar UNA VEZ tras crear las tablas vacias. No borra ni reemplaza datos.
USE dbactiva;
START TRANSACTION;
INSERT INTO monitor (id, nombre, especialidad) VALUES
  (1, 'Leire', 'Senderismo'), (2, 'Unai', 'Fotografia'), (3, 'Nora', 'Cultura');
INSERT INTO cliente (id, nombre, email) VALUES
  (1, 'Ane Garcia', 'ane@example.com'), (2, 'Mikel Ruiz', 'mikel@example.com');
INSERT INTO actividad (id, nombre, localidad, fecha_hora, plazas_totales, plazas_disponibles, monitor_id) VALUES
  (1, 'Ruta por el bosque', 'Bilbao', '2026-11-15 10:00:00', 5, 5, 1),
  (2, 'Fotografia urbana', 'Bilbao', '2026-11-15 17:00:00', 4, 4, 2),
  (3, 'Visita cultural', 'Vitoria-Gasteiz', '2026-11-15 11:00:00', 6, 6, 3),
  (4, 'Ruta del parque', 'Bilbao', '2026-11-16 10:00:00', 3, 3, 1);
COMMIT;
-- La tabla reserva comienza vacia. No se presuponen IDs consecutivos tras un rollback.


SHOW TABLES;

SELECT * FROM monitor; 

SELECT * FROM actividad;














