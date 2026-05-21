package com.experienzia;

import com.experienzia.entity.Estado;
import com.experienzia.entity.Rol;
import com.experienzia.entity.Usuario;
import com.experienzia.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase principal de la app ExperienZia.
 * Aquí arranca Spring Boot cuando ejecutas el proyecto.
 */
@SpringBootApplication // le dice a Spring que escanee componentes y configure todo
@EnableScheduling // activa las tareas programadas (cron) del scheduler
public class Application {

	/**
	 * Punto de entrada: método main que Java ejecuta al iniciar.
	 */
	public static void main(String[] args) {
		// args son los argumentos de la línea de comandos (a veces no se usan)
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Este Bean se ejecuta una vez que la aplicación ha iniciado.
	 * Crea un usuario administrador por defecto si todavía no existe,
	 * para poder iniciar sesión y administrar la plataforma desde el primer arranque.
	 */
	@Bean // Spring registra esto como un componente que corre al arrancar
	CommandLineRunner inicializarAdmin(UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder) {
		// CommandLineRunner = código que corre después de que la app ya levantó
		return args -> {
			// email fijo del admin por defecto (lo usamos para buscar si ya existe)
			final String emailAdmin = "admin@experienzia.com";

			// si no hay nadie con ese email, lo creamos
			if (usuarioRepository.findByEmail(emailAdmin).isEmpty()) {
				System.out.println("Creando administrador por defecto: " + emailAdmin);

				Usuario admin = new Usuario();
				admin.setNombre("Administrador ExperienZia");
				admin.setEmail(emailAdmin);
				// la contraseña va encriptada, nunca en texto plano en la BD
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setTipoDocumento("CC");
				admin.setNumeroDocumento("0000000000");
				admin.setTelefono("3000000000");
				admin.setRol(Rol.ADMIN); // rol de administrador del sistema
				admin.setEstado(Estado.ACTIVO); // usuario habilitado para entrar

				usuarioRepository.save(admin);

				System.out.println("Administrador creado. Credenciales -> "
						+ emailAdmin + " / admin123");
			} else {
				// ya había admin, no hacemos nada para no duplicar
				System.out.println("El administrador por defecto (" + emailAdmin + ") ya existe.");
			}
		};
	}
}
