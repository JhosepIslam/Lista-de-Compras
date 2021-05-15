package com.example.lista_compra;

public class Producto {
    public int id;
    public String nombre;

    public Producto(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return id+"- "+nombre;
    }
}
