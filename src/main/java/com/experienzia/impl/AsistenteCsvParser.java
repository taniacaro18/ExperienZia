package com.experienzia.impl;

import com.experienzia.dto.FilaAsistenteCargaDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Convierto el CSV que sube el organizador en filas para la carga masiva de invitados.
@Component
public class AsistenteCsvParser {

    public List<FilaAsistenteCargaDTO> parseCsv(String contenidoCsv) {
        if (contenidoCsv == null || contenidoCsv.isBlank()) {
            throw new IllegalArgumentException("El archivo CSV está vacío.");
        }
        // Excel a veces mete BOM al inicio; si no lo quito la primera columna sale rara.
        String texto = contenidoCsv.charAt(0) == '\uFEFF' ? contenidoCsv.substring(1) : contenidoCsv;
        String[] lineas = texto.split("\\r?\\n");
        List<FilaAsistenteCargaDTO> filas = new ArrayList<>();

        Integer idxNombre = null, idxEmail = null, idxTel = null, idxTipoDoc = null, idxNumDoc = null;
        boolean hayCabecera = false;

        for (String rawLinea : lineas) {
            String linea = rawLinea.trim();
            if (linea.isEmpty()) {
                continue;
            }
            String[] partes = linea.split("\\s*,\\s*", -1);
            if (!hayCabecera && esCabecera(partes)) {
                idxNombre = indiceColumna(partes, "nombre");
                idxEmail = indiceColumna(partes, "email", "correo");
                idxTel = indiceColumna(partes, "telefono", "teléfono", "tel");
                idxTipoDoc = indiceColumna(partes, "tipodocumento", "tipo documento", "tipo_documento");
                idxNumDoc = indiceColumna(partes, "numerodocumento", "numero documento", "numero_documento", "documento");
                if (idxNombre == null || idxEmail == null || idxTipoDoc == null || idxNumDoc == null) {
                    throw new IllegalArgumentException(
                            "CSV con cabecera: se requieren columnas nombre, email, tipo de documento y número de documento.");
                }
                hayCabecera = true;
                continue;
            }
            FilaAsistenteCargaDTO f = new FilaAsistenteCargaDTO();
            if (hayCabecera) {
                f.setNombre(getCelda(partes, idxNombre));
                f.setEmail(getCelda(partes, idxEmail));
                if (idxTel != null) {
                    f.setTelefono(getCelda(partes, idxTel));
                }
                f.setTipoDocumento(getCelda(partes, idxTipoDoc));
                f.setNumeroDocumento(getCelda(partes, idxNumDoc));
            } else {
                if (partes.length < 5) {
                    throw new IllegalArgumentException(
                            "Sin cabecera use 5 columnas: nombre,email,telefono,tipoDocumento,numeroDocumento");
                }
                f.setNombre(partes[0]);
                f.setEmail(partes[1]);
                f.setTelefono(partes[2]);
                f.setTipoDocumento(partes[3]);
                f.setNumeroDocumento(partes[4]);
            }
            filas.add(f);
        }
        if (filas.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron filas de datos en el CSV.");
        }
        return filas;
    }

    private static String getCelda(String[] partes, int idx) {
        if (idx < 0 || idx >= partes.length) {
            return "";
        }
        return partes[idx].trim();
    }

    private static boolean esCabecera(String[] partes) {
        String uno = String.join(",", partes).toLowerCase(Locale.ROOT);
        return uno.contains("email") && uno.contains("nombre");
    }

    // Normalizo títulos de columna (minúsculas, sin espacios ni _) para matchear variantes del Excel.
    private static String normCol(String raw) {
        return raw.trim().replace("\"", "").toLowerCase(Locale.ROOT).replaceAll("[\\s_]+", "");
    }

    private static Integer indiceColumna(String[] partes, String... titulos) {
        for (int i = 0; i < partes.length; i++) {
            String c = normCol(partes[i]);
            for (String t : titulos) {
                if (c.equals(normCol(t))) {
                    return i;
                }
            }
        }
        return null;
    }
}
