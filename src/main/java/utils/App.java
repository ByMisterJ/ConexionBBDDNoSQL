package utils;

import modelos.Equipo;
import modelos.Piloto;

import java.time.LocalDate;
import java.util.Arrays;

public class App {
    public static void main(String[] args) {
        OperacionesDynamoDB operacionesDynamoDB = new OperacionesDynamoDB();

        // Crear la tabla "EquiposF1"
        operacionesDynamoDB.crearTabla("EquiposF1");

        // Poblar la tabla con datos reales
        operacionesDynamoDB.poblarEquiposDesdeJson("src/main/resources/equipos.json");

        // Mostrar todos los equipos
        operacionesDynamoDB.imprimirTodosLosEquipos();

        // Crear un equipo y pilotos de prueba
        Piloto piloto1 = new Piloto();
        piloto1.setNombre("Isack Hadjar");
        piloto1.setNumero(98);
        piloto1.setFechaNacimiento(String.valueOf(LocalDate.of(2004, 9, 28)));
        piloto1.setNacionalidad("France");

        Piloto piloto2 = new Piloto();
        piloto2.setNombre("Pepe Martí");
        piloto2.setNumero(99);
        piloto2.setFechaNacimiento(String.valueOf(LocalDate.of(2005, 6, 13)));
        piloto2.setNacionalidad("Spain");

        Equipo equipo = new Equipo();
        equipo.setNombre("Campos Racing");
        equipo.setNacionalidad("Spain");
        equipo.setPilotos(Arrays.asList(piloto1, piloto2));

        // Insertar el equipo
        operacionesDynamoDB.insertarEquipo(equipo);

        // Obtener el equipo
        Equipo equipoObtenido = operacionesDynamoDB.obtenerEquipo("Campos Racing");
        System.out.println("Equipo obtenido: " + equipoObtenido);

        // Actualizar equipo
        equipo.setNacionalidad("Andorra");
        operacionesDynamoDB.actualizarEquipo(equipo);

        // Obtener el equipo
        equipoObtenido = operacionesDynamoDB.obtenerEquipo("Campos Racing");
        System.out.println("Equipo obtenido: " + equipoObtenido);

        // Borrar equipo
        operacionesDynamoDB.borrarEquipo("Campos Racing");

        // Borrar la tabla al finalizar
        operacionesDynamoDB.borrarTabla("EquiposF1");
    }
}
