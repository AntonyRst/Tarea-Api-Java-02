package com.biblioteca.api;

import org.springframework.stereotype.Service;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibroService {
    // 5 libros de ejemplo cargados inicialmente
    private final List<Libro> libros = new ArrayList<>(List.of(
            new Libro(1, "1984", "George Orwell", 1949, true),
            new Libro(2, "Cien años de soledad", "Gabriel García Márquez", 1967, true),
            new Libro(3, "El principito", "Antoine de Saint-Exupéry", 1943, false),
            new Libro(4, "Ficciones", "Jorge Luis Borges", 1944, true),
            new Libro(5, "Don Quijote de la Mancha", "Miguel de Cervantes", 1605, false)
    ));

    private int siguienteId = 6;

    public List<Libro> listar(String autor) {
        if (autor == null || autor.isBlank()) {
            return libros; // Retorna todos si no hay filtro
        }
        String autorBusqueda = autor.toLowerCase();
        return libros.stream()
                .filter(l -> l.autor().toLowerCase().contains(autorBusqueda))
                .collect(Collectors.toList());
    }

    public Optional<Libro> buscar(int id) {
        return libros.stream().filter(l -> l.id() == id).findFirst();
    }

    public Libro crear(Libro datos) {
        validar(datos);
        // Regla: Un libro nuevo siempre empieza disponible (true)
        Libro nuevo = new Libro(siguienteId++, datos.titulo().trim(), datos.autor().trim(), datos.anio(), true);
        libros.add(nuevo);
        return nuevo;
    }

    public Optional<Libro> actualizar(int id, Libro datos) {
        validar(datos);
        for (int i = 0; i < libros.size(); i++) {
            Libro actual = libros.get(i);
            if (actual.id() == id) {
                // Mantiene el estado actual de "disponible" pero actualiza el resto
                Libro modificado = new Libro(id, datos.titulo().trim(), datos.autor().trim(), datos.anio(), actual.disponible());
                libros.set(i, modificado);
                return Optional.of(modificado);
            }
        }
        return Optional.empty();
    }

    public boolean eliminar(int id) {
        return libros.removeIf(l -> l.id() == id);
    }

    public Optional<Libro> prestar(int id) {
        for (int i = 0; i < libros.size(); i++) {
            Libro actual = libros.get(i);
            if (actual.id() == id) {
                if (!actual.disponible()) {
                    throw new ConflictoException("El libro ya está prestado"); // Dispara el 409
                }
                Libro modificado = new Libro(id, actual.titulo(), actual.autor(), actual.anio(), false);
                libros.set(i, modificado);
                return Optional.of(modificado);
            }
        }
        return Optional.empty();
    }

    public Optional<Libro> devolver(int id) {
        for (int i = 0; i < libros.size(); i++) {
            Libro actual = libros.get(i);
            if (actual.id() == id) {
                if (actual.disponible()) {
                    throw new ConflictoException("El libro ya está disponible"); // Dispara el 409
                }
                Libro modificado = new Libro(id, actual.titulo(), actual.autor(), actual.anio(), true);
                libros.set(i, modificado);
                return Optional.of(modificado);
            }
        }
        return Optional.empty();
    }

    private void validar(Libro datos) {
        if (datos.titulo() == null || datos.titulo().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        if (datos.autor() == null || datos.autor().isBlank()) {
            throw new IllegalArgumentException("El autor es obligatorio");
        }
        if (datos.anio() > Year.now().getValue()) {
            throw new IllegalArgumentException("El año no puede ser mayor al actual");
        }
    }
}
