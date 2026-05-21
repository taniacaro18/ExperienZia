package com.experienzia.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Arregla esquemas viejos de PostgreSQL al arrancar la app.
 * A veces la tabla eventos tenía un CHECK sin el estado FINALIZADO
 * y eso rompía al guardar eventos ya terminados.
 */
@Component // Spring lo detecta y lo ejecuta al iniciar
@Order(0) // corre muy pronto, antes que otros ApplicationRunner
public class PostgresEventoEstadoCheckFix implements ApplicationRunner {

	// logger para escribir en consola/archivo sin usar System.out
	private static final Logger log = LoggerFactory.getLogger(PostgresEventoEstadoCheckFix.class);

	private final Environment environment; // lee application.properties
	private final JdbcTemplate jdbcTemplate; // ejecuta SQL directo

	public PostgresEventoEstadoCheckFix(Environment environment, JdbcTemplate jdbcTemplate) {
		this.environment = environment;
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Se ejecuta una vez cuando la aplicación ya arrancó.
	 */
	@Override
	public void run(ApplicationArguments args) {
		// propiedad para poder desactivar el fix si hace falta (por defecto true)
		if (!Boolean.parseBoolean(environment.getProperty("experienzia.db.fix-eventos-estado-check", "true"))) {
			return;
		}
		String url = environment.getProperty("spring.datasource.url", "");
		// solo tiene sentido en PostgreSQL, no en H2 u otra BD
		if (!url.contains("jdbc:postgresql")) {
			return;
		}
		try {
			// quitamos el constraint viejo y lo recreamos con todos los estados del enum
			jdbcTemplate.execute("ALTER TABLE eventos DROP CONSTRAINT IF EXISTS eventos_estado_check");
			jdbcTemplate.execute(
					"ALTER TABLE eventos ADD CONSTRAINT eventos_estado_check CHECK (estado IN ("
							+ "'PENDIENTE','APROBADO','RECHAZADO','ACTIVO','FINALIZADO','CANCELADO',"
							+ "'PENDIENTE_REVISION','PENDIENTE_SUPLEMENTO','PENDIENTE_CANCELACION'))");
			log.info("Constraint eventos_estado_check alineado con EstadoEvento (incluye flujos de edición/cancelación).");
		} catch (Exception e) {
			log.warn("No se pudo actualizar eventos_estado_check: {}", e.getMessage());
		}
		try {
			// ensanchamos la columna por si los nombres de estado son largos
			jdbcTemplate.execute("ALTER TABLE eventos ALTER COLUMN estado TYPE VARCHAR(40)");
		} catch (Exception e) {
			log.debug("ALTER COLUMN estado (ancho): {}", e.getMessage());
		}
		try {
			// el comprobante puede quedar vacío hasta que suban el archivo
			jdbcTemplate.execute("ALTER TABLE pagos ALTER COLUMN comprobante_url DROP NOT NULL");
			log.info("Columna pagos.comprobante_url permite NULL (comprobante pendiente de subir).");
		} catch (Exception e) {
			log.warn("No se pudo alinear pagos.comprobante_url: {}", e.getMessage());
		}
		alinearForeignKeysEventoNovedades();
	}

	/**
	 * La tabla usada por la app es evento_novedades (no eventos_novedades).
	 * Sin @ManyToOne Hibernate a veces no creaba FK y el ERD mostraba la tabla suelta.
	 */
	private void alinearForeignKeysEventoNovedades() {
		try {
			// miramos si la tabla ya existe en public
			Integer existe = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM information_schema.tables "
							+ "WHERE table_schema = 'public' AND table_name = 'evento_novedades'",
					Integer.class);
			if (existe == null || existe == 0) {
				log.debug("Tabla evento_novedades aún no existe; Hibernate la creará con ddl-auto=update.");
				return;
			}
			// FK hacia eventos (si borras evento, borra novedades en cascada)
			jdbcTemplate.execute(
					"ALTER TABLE evento_novedades DROP CONSTRAINT IF EXISTS fk_evento_novedad_evento");
			jdbcTemplate.execute(
					"ALTER TABLE evento_novedades ADD CONSTRAINT fk_evento_novedad_evento "
							+ "FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE");
			// FK hacia usuarios (quien solicitó la novedad)
			jdbcTemplate.execute(
					"ALTER TABLE evento_novedades DROP CONSTRAINT IF EXISTS fk_evento_novedad_usuario");
			jdbcTemplate.execute(
					"ALTER TABLE evento_novedades ADD CONSTRAINT fk_evento_novedad_usuario "
							+ "FOREIGN KEY (usuario_solicitante_id) REFERENCES usuarios(id)");
			log.info("FK evento_novedades → eventos y usuarios alineadas.");
		} catch (Exception e) {
			log.warn("No se pudieron crear FK de evento_novedades: {}", e.getMessage());
		}
	}
}
