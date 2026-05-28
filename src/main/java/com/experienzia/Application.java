package com.experienzia;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.experienzia.entity.Estado;
import com.experienzia.entity.Rol;
import com.experienzia.entity.Usuario;
import com.experienzia.repository.UsuarioRepository;

// Yo levanto acá todo el backend de ExperienZia cuando corro el main
@SpringBootApplication 
// Activo el cron que finaliza eventos vencidos (EventoFinalizacionScheduler)
@EnableScheduling 
public class Application {

	// Arranco el servidor Spring desde acá
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// Si en la BD no hay admin, me invento uno para poder entrar la primera vez
	@Bean 
	CommandLineRunner inicializarAdmin(UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder) {
		
		return args -> {
			
			final String emailAdmin = "admin@experienzia.com";

			if (usuarioRepository.findByEmail(emailAdmin).isEmpty()) {
				System.out.println("Creando administrador por defecto: " + emailAdmin);

				Usuario admin = new Usuario();
				admin.setNombre("Administrador ExperienZia");
				admin.setEmail(emailAdmin);
				
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setTipoDocumento("CC");
				admin.setNumeroDocumento("0000000000");
				admin.setTelefono("3000000000");
				admin.setRol(Rol.ADMIN); 
				admin.setEstado(Estado.ACTIVO); 

				usuarioRepository.save(admin);

				// Ojo: admin123 es solo dev; en prod habría que cambiar la clave ya
				System.out.println("Administrador creado. Credenciales -> "
						+ emailAdmin + " / admin123");
			} else {
				
				System.out.println("El administrador por defecto (" + emailAdmin + ") ya existe.");
			}
		};
	}
}
