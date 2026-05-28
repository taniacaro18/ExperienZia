package com.experienzia.service;

import com.experienzia.exceptions.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// Guardo y borro comprobantes de pago en disco; el front luego pide la URL /uploads/...
@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads/comprobantes/";

    // Recibo el archivo del front, valido tipo y lo dejo en uploads; devuelvo la ruta pública
    public String guardarComprobante(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new CustomException("El archivo está vacío.", HttpStatus.BAD_REQUEST);
        }
        String contentType = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase();
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase();
        boolean tipoOk =
                contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp")
                        || contentType.equals("application/pdf");
        boolean nombreOk =
                nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")
                        || nombre.endsWith(".png") || nombre.endsWith(".webp")
                        || nombre.endsWith(".pdf");
        if (!tipoOk && !nombreOk) {
            throw new CustomException(
                    "Solo se permiten comprobantes en formato JPG, PNG, WEBP o PDF.",
                    HttpStatus.BAD_REQUEST);
        }
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String originalName = archivo.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String uniqueName = UUID.randomUUID() + extension;
            Path filePath = uploadPath.resolve(uniqueName);
            Files.copy(archivo.getInputStream(), filePath);
            // La ruta relativa es la que el front usa para mostrar o descargar el comprobante
            return "/uploads/comprobantes/" + uniqueName;
        } catch (IOException e) {
            throw new CustomException("Error al guardar el archivo comprobante.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Borro el archivo del disco cuando cambian o rechazan un pago (si no existe no freno el flujo)
    public void borrarComprobantePublico(String urlRelativa) {
        if (urlRelativa == null || urlRelativa.isBlank()) {
            return;
        }
        try {
            String rel = urlRelativa.startsWith("/") ? urlRelativa.substring(1) : urlRelativa;
            if (!rel.startsWith(UPLOAD_DIR)) {
                return;
            }
            Path filePath = Paths.get(rel);
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Si ya no está el archivo sigo igual, no le rompo la vuelta al front
        }
    }
}
